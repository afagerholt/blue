package com.visma.blue.events;

public class MetadataEvent {

    public enum UpdateStatus {
        STARTED_UPDATE(0),
        FINISHED_UPDATE(1),
        UPDATE_ERROR(2);

        private final int status;

        UpdateStatus(int status) {
            this.status = status;

        }

        public int getValue() {
            return status;
        }
    }

    private int mStatus;
    private int mErrorMessageId;

    public MetadataEvent(int status) {
        mStatus = status;
    }

    public MetadataEvent(int status, int errorMessageId) {
        mStatus = status;
        mErrorMessageId = errorMessageId;
    }

    public int getStatus() {
        return mStatus;
    }

    public int getErrorMessageId() {
        return mErrorMessageId;
    }
}
