package com.goodsoup.fx_rate.it;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.FileUploadStatus;
import com.goodsoup.fx_rate.entity.UserEntity;
import com.goodsoup.fx_rate.entity.UserRole;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import com.goodsoup.fx_rate.repo.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FileUploadRestIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    FileUploadRepository fileUploadRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void getFileUploadByUuid_returnsJsonFromRealDb() {
        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("pw"));
        admin.setRole(UserRole.ADMIN);
        userRepository.saveAndFlush(admin);

        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        FileUploadEntity e = new FileUploadEntity();
        e.setFileUploadUuid(uuid);
        e.setStatus(FileUploadStatus.FINISHED);
        e.setRowsLoaded(12);
        e.setRowsSkipped(3);
        e.setErrorMessage(null);
        fileUploadRepository.saveAndFlush(e);

        ResponseEntity<String> res = restTemplate
                .withBasicAuth("admin", "pw")
                .getForEntity("/api/file-uploads/{uuid}", String.class, uuid);

        org.assertj.core.api.Assertions.assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        org.assertj.core.api.Assertions.assertThat(res.getBody())
                .contains("\"fileUploadUuid\":\"" + uuid + "\"")
                .contains("\"status\":\"FINISHED\"")
                .contains("\"rowsLoaded\":12")
                .contains("\"rowsSkipped\":3");
    }
}

