package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateResponseDTO {

    private int totalReceived;
    private int totalSaved;
    private int totalFailed;
    private List<FailedEntry> failed;

    // ==================== INNER CLASS ====================

    public static class FailedEntry {
        private int index;
        private String reason;

        public FailedEntry(int index, String reason) {
            this.index = index;
            this.reason = reason;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}