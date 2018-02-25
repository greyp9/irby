package io.github.greyp9.irby.core.http11.socket;

import io.github.greyp9.irby.core.http11.dispatch.Http11Dispatcher;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class Http11SocketRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11Dispatcher dispatcher;
    private final Socket socket;

    public Http11SocketRunnable(final Http11Dispatcher dispatcher, final Socket socket) {
        this.dispatcher = dispatcher;
        this.socket = socket;
    }

    @Override
    public final void run() {
        try {
            dispatcher.doSocket(socket);
        } catch (SocketException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
