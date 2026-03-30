package com.goodsoup.fx_rate.mq;

import static org.mockito.Mockito.verify;

import com.goodsoup.fx_rate.service.FileUploadService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FxImportListenerTest {

    @Test
    void onMessage_callsFileUploadService() {
        FileUploadService fileUploadService = Mockito.mock(FileUploadService.class);
        FxImportListener listener = new FxImportListener(fileUploadService);

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        listener.onMessage(new FxImportMessage(uuid));

        verify(fileUploadService).processUpload(uuid);
    }
}

