package com.goodsoup.fx_rate.mq;

import com.goodsoup.fx_rate.service.FileUploadService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FxImportListener {

    private final FileUploadService fileUploadService;

    public FxImportListener(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @RabbitListener(queues = "${app.rabbit.fx-import-queue}")
    public void onMessage(FxImportMessage msg) {
        fileUploadService.processUpload(msg.fileUploadUuid());
    }
}
