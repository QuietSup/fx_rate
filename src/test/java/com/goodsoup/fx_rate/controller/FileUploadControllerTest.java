package com.goodsoup.fx_rate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.FileUploadStatus;
import com.goodsoup.fx_rate.security.SecurityConfig;
import com.goodsoup.fx_rate.service.FileUploadService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = FileUploadController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@Import(FileUploadControllerTest.TestSecurityConfig.class)
class FileUploadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FileUploadService fileUploadService;

    @Test
    void upload_returnsResponse() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        FileUploadEntity entity = new FileUploadEntity();
        entity.setId(7L);
        entity.setFileUploadUuid(uuid);
        entity.setStatus(FileUploadStatus.TO_PROCESS);
        entity.setRowsLoaded(null);
        entity.setRowsSkipped(null);
        entity.setErrorMessage(null);

        when(fileUploadService.enqueueUpload(any(), eq("EUR"), eq("USD"))).thenReturn(entity);

        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "Date,Open,High,Low,Close\n01/01/2026,1.0,1.1,0.9,1.05\n".getBytes()
        );

        mockMvc.perform(multipart("/api/file-uploads")
                        .file(csv)
                        .param("base", "EUR")
                        .param("quote", "USD")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpBasic("admin", "pw")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUploadUuid").value(uuid.toString()))
                .andExpect(jsonPath("$.status").value("TO_PROCESS"))
                .andExpect(jsonPath("$.rowsLoaded").doesNotExist())
                .andExpect(jsonPath("$.rowsSkipped").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void upload_returns400_whenBaseInvalid() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "Date,Open,High,Low,Close\n01/01/2026,1.0,1.1,0.9,1.05\n".getBytes()
        );

        mockMvc.perform(multipart("/api/file-uploads")
                        .file(csv)
                        .param("base", "EURO") // invalid: must be 3 uppercase letters
                        .param("quote", "USD")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpBasic("admin", "pw")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_returns400_whenQuoteInvalid() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "Date,Open,High,Low,Close\n01/01/2026,1.0,1.1,0.9,1.05\n".getBytes()
        );

        mockMvc.perform(multipart("/api/file-uploads")
                        .file(csv)
                        .param("base", "EUR")
                        .param("quote", "usd") // invalid: must be uppercase
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpBasic("admin", "pw")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_returns401_whenAnonymous() throws Exception {
        MockMultipartFile csv = new MockMultipartFile(
                "file",
                "data.csv",
                "text/csv",
                "Date,Open,High,Low,Close\n01/01/2026,1.0,1.1,0.9,1.05\n".getBytes()
        );

        mockMvc.perform(multipart("/api/file-uploads")
                        .file(csv)
                        .param("base", "EUR")
                        .param("quote", "USD")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_returnsResponse() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        FileUploadEntity entity = new FileUploadEntity();
        entity.setId(7L);
        entity.setFileUploadUuid(uuid);
        entity.setStatus(FileUploadStatus.FINISHED);
        entity.setRowsLoaded(12);
        entity.setRowsSkipped(3);
        entity.setErrorMessage(null);

        when(fileUploadService.getByUuid(uuid)).thenReturn(entity);

        mockMvc.perform(get("/api/file-uploads/{uuid}", uuid)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(httpBasic("admin", "pw")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUploadUuid").value(uuid.toString()))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.rowsLoaded").value(12))
                .andExpect(jsonPath("$.rowsSkipped").value(3))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void get_returns401_whenAnonymous() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        mockMvc.perform(get("/api/file-uploads/{uuid}", uuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(basic -> {})
                    .build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return NoOpPasswordEncoder.getInstance();
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> switch (username) {
                case "admin" -> User.withUsername("admin").password("pw").roles("ADMIN").build();
                case "standard" -> User.withUsername("standard").password("pw").roles("STANDARD").build();
                default -> throw new UsernameNotFoundException(username);
            };
        }
    }
}

