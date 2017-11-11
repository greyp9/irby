package io.github.greyp9.irby.core.logging;

import io.github.greyp9.irby.core.http11.access.AccessLogger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class AppFileHandler extends FileHandler {

    public AppFileHandler() throws IOException, SecurityException {
        super();
        super.setFilter(new AppFilter());
    }

    private static class AppFilter implements Filter {

        @Override
        public boolean isLoggable(LogRecord record) {
            return (!record.getSourceClassName().equals(AccessLogger.class.getName()));
        }
    }
}
