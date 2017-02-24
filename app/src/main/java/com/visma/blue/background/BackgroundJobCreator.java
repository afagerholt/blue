package com.visma.blue.background;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class BackgroundJobCreator implements JobCreator {

    @Override
    public Job create(String tag) {
        switch (tag) {
            case MetadataUploadJob.TAG:
                return new MetadataUploadJob();
            default:
                return null;
        }
    }
}
