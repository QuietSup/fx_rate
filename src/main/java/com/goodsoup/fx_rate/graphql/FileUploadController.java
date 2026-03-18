package com.goodsoup.fx_rate.graphql;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class FileUploadController {

    private final FileUploadRepository fileUploadRepository;

    public FileUploadController(FileUploadRepository fileUploadRepository) {
        this.fileUploadRepository = fileUploadRepository;
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<FileUploadEntity> fileUploads() {
        return fileUploadRepository.findAll();
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public FileUploadEntity fileUpload(@Argument Long id) {
        return fileUploadRepository.findById(id).orElse(null);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public FileUploadEntity fileUploadByUuid(@Argument String uuid) {
        UUID parsed = UUID.fromString(uuid);
        return fileUploadRepository.findByFileUploadUuid(parsed).orElse(null);
    }
}

