package com.goodsoup.fx_rate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "file_upload",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_file_upload_uuid", columnNames = {"file_upload_uuid"})
        },
        indexes = {
                @Index(name = "idx_file_upload_uuid", columnList = "file_upload_uuid"),
                @Index(name = "idx_file_upload_status", columnList = "status"),
                @Index(name = "idx_file_upload_pair_id", columnList = "pair_id")
        }
)
public class FileUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_upload_uuid", nullable = false, updatable = false)
    private UUID fileUploadUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FileUploadStatus status = FileUploadStatus.TO_PROCESS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id")
    private PairEntity pair;

    @Column(name = "rows_loaded")
    private Integer rowsLoaded;

    @Column(name = "rows_skipped")
    private Integer rowsSkipped;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (fileUploadUuid == null) {
            fileUploadUuid = UUID.randomUUID();
        }
        if (status == null) {
            status = FileUploadStatus.TO_PROCESS;
        }
    }

    @PreUpdate
    void onUpdate() {
        if (status == null) {
            status = FileUploadStatus.TO_PROCESS;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getFileUploadUuid() {
        return fileUploadUuid;
    }

    public void setFileUploadUuid(UUID fileUploadUuid) {
        this.fileUploadUuid = fileUploadUuid;
    }

    public FileUploadStatus getStatus() {
        return status;
    }

    public void setStatus(FileUploadStatus status) {
        this.status = status;
    }

    public PairEntity getPair() {
        return pair;
    }

    public void setPair(PairEntity pair) {
        this.pair = pair;
    }

    public Integer getRowsLoaded() {
        return rowsLoaded;
    }

    public void setRowsLoaded(Integer rowsLoaded) {
        this.rowsLoaded = rowsLoaded;
    }

    public Integer getRowsSkipped() {
        return rowsSkipped;
    }

    public void setRowsSkipped(Integer rowsSkipped) {
        this.rowsSkipped = rowsSkipped;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

