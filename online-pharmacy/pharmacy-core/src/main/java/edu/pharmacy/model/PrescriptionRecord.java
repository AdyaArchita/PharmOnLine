package edu.pharmacy.model;

import java.time.LocalDateTime;

/** Prescription submission metadata used by pharmacist review screens. */
public final class PrescriptionRecord {
    private final long id;
    private final long customerId;
    private final long medicineId;
    private final String fileName;
    private final String status;
    private final LocalDateTime uploadedAt;

    public PrescriptionRecord(long id, long customerId, long medicineId, String fileName,
                              String status, LocalDateTime uploadedAt) {
        this.id = id;
        this.customerId = customerId;
        this.medicineId = medicineId;
        this.fileName = fileName;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
