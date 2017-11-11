package io.github.greyp9.irby.core.logging;

import io.github.greyp9.irby.core.http11.access.AccessLogger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class AccessFileHandler extends FileHandler {

    public AccessFileHandler() throws IOException, SecurityException {
        super();
        super.setFilter(new AccessFilter());
    }

    private static class AccessFilter implements Filter {

        @Override
        public boolean isLoggable(LogRecord record) {
            return record.getSourceClassName().equals(AccessLogger.class.getName());
        }
    }
}
