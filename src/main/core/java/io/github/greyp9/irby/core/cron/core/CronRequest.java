package io.github.greyp9.irby.core.cron.core;

import io.github.greyp9.arwo.core.http.servlet.ServletHttpRequest;
import io.github.greyp9.arwo.core.resource.Pather;

public class CronRequest {
    private final ServletHttpRequest httpRequest;
    private final String submitID;
    private final String cronTab;
    private final String cronJob;
    private final String jobDate;
    private final String jobStream;

    public CronRequest(final ServletHttpRequest servletHttpRequest, final String submitID) {
        final Pather patherTab = new Pather(servletHttpRequest.getPathInfo());
        final Pather patherName = new Pather(patherTab.getRight());
        final Pather patherDate = new Pather(patherName.getRight());
        final Pather patherStream = new Pather(patherDate.getRight());

        this.httpRequest = servletHttpRequest;
        this.submitID = submitID;
        this.cronTab = patherTab.getLeftToken();
        this.cronJob = patherName.getLeftToken();
        this.jobDate = patherDate.getLeftToken();
        this.jobStream = patherStream.getLeftToken();
    }

    public ServletHttpRequest getHttpRequest() {
        return httpRequest;
    }

    public String getSubmitID() {
        return submitID;
    }

    public String getCronTab() {
        return cronTab;
    }

    public String getCronJob() {
        return cronJob;
    }

    public String getJobDate() {
        return jobDate;
    }

    public String getJobStream() {
        return jobStream;
    }
}
