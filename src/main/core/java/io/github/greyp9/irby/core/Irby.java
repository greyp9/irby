package io.github.greyp9.irby.core;

import javax.xml.namespace.QName;

public final class Irby {

    public static class App {
        public static final String XSD = "io/github/greyp9/irby/xsd/app/app.xsd";
        public static final String URI = "urn:irby:app";
        public static final QName QNAME = new QName(URI, "application");
    }

    public static class FS {
        public static final String CONF = "conf";
    }
}
