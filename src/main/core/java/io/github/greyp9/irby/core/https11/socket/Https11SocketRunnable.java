package io.github.greyp9.irby.core.https11.socket;

import io.github.greyp9.irby.core.http11.dispatch.Http11Dispatcher;
import io.github.greyp9.irby.core.https11.config.Https11Config;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.DoNotUseThreads")
public class Https11SocketRunnable implements Runnable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Http11Dispatcher dispatcher;
    private final Socket socket;

    public Https11SocketRunnable(final Http11Dispatcher dispatcher, final Socket socket) {
        this.dispatcher = dispatcher;
        this.socket = socket;
    }

    @Override
    public final void run() {
        try {
            dispatcher.doSocket(augmentSSLSocket(dispatcher, socket));
        } catch (SSLHandshakeException e) {
            final String message = String.format("%s/%s", socket.getInetAddress().getHostAddress(), e.getMessage());
            logger.log(Level.INFO, message);
        } catch (SocketException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Socket augmentSSLSocket(final Http11Dispatcher dispatcher, final Socket socket) {
        final Https11Config config = (Https11Config) dispatcher.getConfig();
        if (config.isNeedClientAuth()) {
            final SSLSocket sslSocket = (SSLSocket) socket;
            sslSocket.setNeedClientAuth(true);
        }
        return socket;
    }
}
