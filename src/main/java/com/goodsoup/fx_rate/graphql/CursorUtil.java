package com.goodsoup.fx_rate.graphql;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;

final class CursorUtil {

    private static final String PREFIX = "historical:";
    private static final String FILE_UPLOAD_PREFIX = "fileUpload:";

    private CursorUtil() {}

    static String encodeHistoricalCursor(LocalDate date, long id) {
        String raw = PREFIX + date + ":" + id;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static String encodeFileUploadCursor(OffsetDateTime createdAt, long id) {
        String raw = FILE_UPLOAD_PREFIX + createdAt + ":" + id;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static DecodedHistoricalCursor decodeHistoricalCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!raw.startsWith(PREFIX)) {
                throw new IllegalArgumentException("Cursor prefix mismatch");
            }
            String payload = raw.substring(PREFIX.length());
            int sep = payload.lastIndexOf(':');
            if (sep <= 0 || sep == payload.length() - 1) {
                throw new IllegalArgumentException("Cursor format invalid");
            }
            LocalDate date = LocalDate.parse(payload.substring(0, sep));
            long id = Long.parseLong(payload.substring(sep + 1));
            return new DecodedHistoricalCursor(date, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    static DecodedFileUploadCursor decodeFileUploadCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!raw.startsWith(FILE_UPLOAD_PREFIX)) {
                throw new IllegalArgumentException("Cursor prefix mismatch");
            }
            String payload = raw.substring(FILE_UPLOAD_PREFIX.length());
            int sep = payload.lastIndexOf(':');
            if (sep <= 0 || sep == payload.length() - 1) {
                throw new IllegalArgumentException("Cursor format invalid");
            }
            OffsetDateTime createdAt = OffsetDateTime.parse(payload.substring(0, sep));
            long id = Long.parseLong(payload.substring(sep + 1));
            return new DecodedFileUploadCursor(createdAt, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor");
        }
    }

    record DecodedHistoricalCursor(LocalDate date, long id) {}

    record DecodedFileUploadCursor(OffsetDateTime createdAt, long id) {}
}

