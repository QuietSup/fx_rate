package com.goodsoup.fx_rate.graphql;

import static org.mockito.Mockito.when;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.FileUploadStatus;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(FileUploadController.class)
@Import(GraphqlConfig.class)
class FileUploadControllerGraphQlTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    FileUploadRepository fileUploadRepository;

    @Test
    void fileUploadByUuid_returnsEntity() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        FileUploadEntity entity = new FileUploadEntity();
        entity.setId(7L);
        entity.setFileUploadUuid(uuid);
        entity.setStatus(FileUploadStatus.FINISHED);

        when(fileUploadRepository.findByFileUploadUuid(uuid)).thenReturn(Optional.of(entity));

        graphQlTester
                .document("""
                        query($uuid: ID!) {
                          fileUploadByUuid(uuid: $uuid) {
                            id
                            fileUploadUuid
                            status
                          }
                        }
                        """)
                .variable("uuid", uuid.toString())
                .execute()
                .path("fileUploadByUuid.id").entity(String.class).isEqualTo("7")
                .path("fileUploadByUuid.fileUploadUuid").entity(String.class).isEqualTo(uuid.toString())
                .path("fileUploadByUuid.status").entity(String.class).isEqualTo("FINISHED");
    }

    @Test
    void fileUploads_returnsList() {
        FileUploadEntity first = new FileUploadEntity();
        first.setId(7L);
        first.setFileUploadUuid(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        first.setStatus(FileUploadStatus.FINISHED);

        FileUploadEntity second = new FileUploadEntity();
        second.setId(8L);
        second.setFileUploadUuid(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        second.setStatus(FileUploadStatus.FAILED);

        when(fileUploadRepository.findAll()).thenReturn(List.of(first, second));

        graphQlTester
                .document("""
                        query {
                          fileUploads {
                            id
                            fileUploadUuid
                            status
                          }
                        }
                        """)
                .execute()
                .path("fileUploads[0].id").entity(String.class).isEqualTo("7")
                .path("fileUploads[0].fileUploadUuid")
                .entity(String.class)
                .isEqualTo(first.getFileUploadUuid().toString())
                .path("fileUploads[0].status").entity(String.class).isEqualTo("FINISHED")
                .path("fileUploads[1].id").entity(String.class).isEqualTo("8")
                .path("fileUploads[1].status").entity(String.class).isEqualTo("FAILED");
    }

    @Test
    void fileUpload_returnsEntity() {
        long id = 7L;
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        FileUploadEntity entity = new FileUploadEntity();
        entity.setId(id);
        entity.setFileUploadUuid(uuid);
        entity.setStatus(FileUploadStatus.FINISHED);

        when(fileUploadRepository.findById(id)).thenReturn(Optional.of(entity));

        graphQlTester
                .document("""
                        query($id: ID!) {
                          fileUpload(id: $id) {
                            id
                            fileUploadUuid
                            status
                          }
                        }
                        """)
                .variable("id", id)
                .execute()
                .path("fileUpload.id").entity(String.class).isEqualTo("7")
                .path("fileUpload.fileUploadUuid").entity(String.class).isEqualTo(uuid.toString())
                .path("fileUpload.status").entity(String.class).isEqualTo("FINISHED");
    }

    @Test
    void fileUpload_returnsNullWhenNotFound() {
        when(fileUploadRepository.findById(7L)).thenReturn(Optional.empty());

        graphQlTester
                .document("""
                        query($id: ID!) {
                          fileUpload(id: $id) {
                            id
                          }
                        }
                        """)
                .variable("id", 7L)
                .execute()
                .path("fileUpload").valueIsNull();
    }

    @Test
    void fileUploadByUuid_returnsNullWhenNotFound() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(fileUploadRepository.findByFileUploadUuid(uuid)).thenReturn(Optional.empty());

        graphQlTester
                .document("""
                        query($uuid: ID!) {
                          fileUploadByUuid(uuid: $uuid) {
                            id
                          }
                        }
                        """)
                .variable("uuid", uuid.toString())
                .execute()
                .path("fileUploadByUuid").valueIsNull();
    }
}

