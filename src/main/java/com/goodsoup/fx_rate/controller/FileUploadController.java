package com.goodsoup.fx_rate.controller;

import com.goodsoup.fx_rate.dto.FileUploadResponse;
import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.service.FileUploadService;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file-uploads")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("base") String base,
            @RequestParam("quote") String quote
    ) {
        FileUploadEntity e = fileUploadService.enqueueUpload(file, base, quote);
        return toResponse(e);
    }

    @GetMapping("/{uuid}")
    public FileUploadResponse get(@PathVariable UUID uuid) {
        return toResponse(fileUploadService.getByUuid(uuid));
    }

    private static FileUploadResponse toResponse(FileUploadEntity e) {
        return new FileUploadResponse(
                e.getFileUploadUuid(),
                e.getStatus(),
                e.getRowsLoaded(),
                e.getRowsSkipped(),
                e.getErrorMessage()
        );
    }
}

