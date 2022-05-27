package io.github.greyp9.irby.core.cron.service;

import java.util.ArrayList;
import java.util.Collection;

public class CronServices extends ArrayList<CronService> {
    private static final long serialVersionUID = -3435818155997490315L;

    public CronServices(final Collection<? extends CronService> cronServices) {
        super(cronServices);
    }
}
