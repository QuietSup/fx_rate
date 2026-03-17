package com.goodsoup.fx_rate.service;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.FileUploadStatus;
import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.entity.PairEntity;
import com.goodsoup.fx_rate.mq.FxImportMessage;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import com.goodsoup.fx_rate.repo.PairRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    private final PairRepository pairRepository;
    private final HistoricalRepository historicalRepository;
    private final FileUploadRepository fileUploadRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String fxImportQueue;
    private final Path uploadsDir;

    public FileUploadService(
            PairRepository pairRepository,
            HistoricalRepository historicalRepository,
            FileUploadRepository fileUploadRepository,
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbit.fx-import-queue}") String fxImportQueue,
            @Value("${app.uploads.dir}") String uploadsDir
    ) {
        this.pairRepository = pairRepository;
        this.historicalRepository = historicalRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.fxImportQueue = fxImportQueue;
        this.uploadsDir = Path.of(uploadsDir);
    }

    @Transactional
    public FileUploadEntity enqueueUpload(MultipartFile file, String base, String quote) {
        PairEntity pair = pairRepository.findByBaseAndQuote(base, quote)
                .orElseGet(() -> {
                    PairEntity p = new PairEntity();
                    p.setBase(base);
                    p.setQuote(quote);
                    return pairRepository.save(p);
                });

        FileUploadEntity upload = new FileUploadEntity();
        upload.setPair(pair);
        upload.setStatus(FileUploadStatus.TO_PROCESS);

        upload = fileUploadRepository.save(upload); // generates fileUploadUuid via @PrePersist

        storeFile(file, upload.getFileUploadUuid());

        rabbitTemplate.convertAndSend(fxImportQueue, new FxImportMessage(upload.getFileUploadUuid()));
        return upload;
    }

    @Transactional
    public void processUpload(UUID fileUploadUuid) {
        FileUploadEntity upload = fileUploadRepository.findByFileUploadUuid(fileUploadUuid)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + fileUploadUuid));

        if (upload.getStatus() == FileUploadStatus.FINISHED) {
            return;
        }

        Path filePath = uploadsDir.resolve(fileUploadUuid + ".csv");
        if (!Files.exists(filePath)) {
            upload.setStatus(FileUploadStatus.FAILED);
            upload.setErrorMessage("Missing file for upload " + fileUploadUuid);
            fileUploadRepository.save(upload);
            return;
        }

        upload.setStatus(FileUploadStatus.PROCESSING);
        fileUploadRepository.save(upload);

        Counts counts = new Counts();

        try (InputStream is = Files.newInputStream(filePath)) {
            parseAndPersistCsv(is, upload.getPair(), counts);
        } catch (Exception e) {
            upload.setRowsLoaded(counts.loaded);
            upload.setRowsSkipped(counts.skipped);
            upload.setStatus(FileUploadStatus.FAILED);
            upload.setErrorMessage(truncate(e.getMessage(), 2000));
            fileUploadRepository.save(upload);
            return;
        }

        upload.setRowsLoaded(counts.loaded);
        upload.setRowsSkipped(counts.skipped);
        upload.setStatus(FileUploadStatus.FINISHED);
        upload.setErrorMessage(null);
        fileUploadRepository.save(upload);
    }

    @Transactional(readOnly = true)
    public FileUploadEntity getByUuid(UUID uuid) {
        return fileUploadRepository.findByFileUploadUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uuid));
    }

    private Path storeFile(MultipartFile file, UUID uuid) {
        try {
            Files.createDirectories(uploadsDir);
            String safeName = (uuid + ".csv");
            Path target = uploadsDir.resolve(safeName).toAbsolutePath().normalize();
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store upload", e);
        }
    }

    private void parseAndPersistCsv(InputStream inputStream, PairEntity pair, Counts counts) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            CsvParserSettings settings = new CsvParserSettings();
            settings.setLineSeparatorDetectionEnabled(true);
            settings.getFormat().setDelimiter(',');
            settings.setSkipEmptyLines(true);
            settings.setIgnoreLeadingWhitespaces(true);
            settings.setIgnoreTrailingWhitespaces(true);
            settings.setEmptyValue(null);
            settings.setNullValue(null);
            settings.setHeaderExtractionEnabled(true);
            settings.setNumberOfRowsToSkip(1);

            CsvParser parser = new CsvParser(settings);
            for (Record record : parser.iterateRecords(br)) {
                String[] row = record.getValues();
                if (row == null || row.length == 0) continue;

                persistRow(row, pair, counts);
            }
        }
    }


    private void persistRow(String[] parts, PairEntity pair, Counts counts) {
        if (parts.length < 5) {
            System.err.println("SKIP: Not enough columns. Got " + parts.length + " columns: " + String.join("|", parts));
            counts.skipped++;
            return;
        }

        try {
            int idx = 0;
            String dateStr = parts[idx++];
            // Strip time if present: "03/13/2026 00:00" -> "03/13/2026"
            if (dateStr.contains(" ")) {
                dateStr = dateStr.substring(0, dateStr.indexOf(' '));
            }
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FMT);

            BigDecimal open = parseDecimal(parts, idx++);
            BigDecimal high = parseDecimal(parts, idx++);
            BigDecimal low = parseDecimal(parts, idx++);
            BigDecimal close = parseDecimal(parts, idx++);
            BigDecimal changePips = (idx < parts.length) ? tryParseDecimal(parts[idx++]) : null;
            BigDecimal changePct = (idx < parts.length) ? tryParseDecimal(parts[idx]) : null;

            HistoricalEntity h = new HistoricalEntity();
            h.setPair(pair);
            h.setDate(date);
            h.setHigh(high);
            h.setLow(low);
            h.setClose(close);
            h.setChangePips(changePips);
            h.setChangePct(changePct);

            historicalRepository.save(h);
            counts.loaded++;
            System.out.println("LOADED: " + date + " H:" + high + " L:" + low + " C:" + close);
        } catch (DataIntegrityViolationException dup) {
            System.err.println("SKIP: Duplicate entry for date: " + dup.getMessage());
            counts.skipped++;
        } catch (Exception e) {
            System.err.println("SKIP: Error parsing row: " + e.getMessage() + " - " + String.join("|", parts));
            counts.skipped++;
        }
    }

    private static BigDecimal parseDecimal(String[] parts, int idx) {
        BigDecimal v = tryParseDecimal(parts[idx]);
        if (v == null) {
            throw new IllegalArgumentException("Invalid number: " + parts[idx]);
        }
        return v;
    }

    private static BigDecimal tryParseDecimal(String token) {
        if (token == null) return null;
        String t = token.trim();
        if (t.isBlank()) return null;
        t = t.replace("%", "");
        return new BigDecimal(t);
    }

    private static final class Counts {
        int loaded;
        int skipped;
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }
}

