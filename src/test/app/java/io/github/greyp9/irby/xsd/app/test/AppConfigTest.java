package io.github.greyp9.irby.xsd.app.test;

import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.xsd.data.DataType;
import io.github.greyp9.arwo.core.xsd.instance.TypeInstance;
import io.github.greyp9.arwo.core.xsd.model.XsdTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

@SuppressWarnings("checkstyle:magicnumber")
public class AppConfigTest {

    @BeforeEach
    public void setUp() throws Exception {
        //io.github.greyp9.arwo.core.logging.LoggerU.adjust(java.util.logging.Logger.getLogger(""));
    }

    @Test
    public void testXSD() throws Exception {
        final URL urlInitial = ResourceU.resolve(Const.XSD);
        final String initialURL = URLCodec.toExternalForm(urlInitial);
        final byte[] xsd = StreamU.read(urlInitial);
        Assertions.assertNotNull(xsd);
        Assertions.assertEquals(12_954, xsd.length);
        Assertions.assertEquals("200ba589", CRCU.crc32String(xsd));
        final URL urlAugmentXSLT = new URL(initialURL.replace(".xsd", ".xslt"));
        final byte[] xslt = StreamU.readSafe(urlAugmentXSLT);
        Assertions.assertNotNull(xslt);
        Assertions.assertEquals(2_031, xslt.length);
        Assertions.assertEquals("cc7ee8ec", CRCU.crc32String(xslt));
    }

    @Test
    public void testHideInTable() throws Exception {
        final URL urlInitial = ResourceU.resolve(Const.XSD);
        final XsdTypes xsdTypes = new XsdTypes(urlInitial);
        Assertions.assertNotNull(xsdTypes);

        final TypeInstance typeInstanceApp = xsdTypes.getElementType("{urn:irby:app}application");
        Assertions.assertNotNull(typeInstanceApp);
        final DataType dataTypeApp = typeInstanceApp.getDataType();
        Assertions.assertNotNull(dataTypeApp);

        final TypeInstance typeInstanceRealm = dataTypeApp.getInstance("realm");
        Assertions.assertNotNull(typeInstanceRealm);
        final DataType dataTypeRealm = typeInstanceRealm.getDataType();
        Assertions.assertNotNull(dataTypeRealm);

        final TypeInstance typeInstancePrincipal = dataTypeRealm.getInstance("principal");
        Assertions.assertNotNull(typeInstancePrincipal);
        final DataType dataTypePrincipal = typeInstancePrincipal.getDataType();
        Assertions.assertNotNull(dataTypePrincipal);

        final TypeInstance typeInstanceCredential = dataTypePrincipal.getInstance("credential");
        Assertions.assertNotNull(typeInstanceCredential);
        final DataType dataTypeCredential = typeInstanceCredential.getDataType();
        Assertions.assertNotNull(dataTypeCredential);

        final String directiveHideInTable = typeInstanceCredential.getDirective("hideInTable");
        Assertions.assertNotNull(directiveHideInTable);
        Assertions.assertTrue(Boolean.parseBoolean(directiveHideInTable));
    }

    private static class Const {
        private static final String XSD = "io/github/greyp9/irby/xsd/app/app.xsd";
    }
}
