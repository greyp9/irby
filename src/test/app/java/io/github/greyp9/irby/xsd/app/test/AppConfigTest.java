package io.github.greyp9.irby.xsd.app.test;

import io.github.greyp9.arwo.core.hash.CRCU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.url.URLCodec;
import io.github.greyp9.arwo.core.xsd.data.DataType;
import io.github.greyp9.arwo.core.xsd.instance.TypeInstance;
import io.github.greyp9.arwo.core.xsd.model.XsdTypes;
import junit.framework.TestCase;
import org.junit.Assert;

import java.net.URL;

public class AppConfigTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //io.github.greyp9.arwo.core.logging.LoggerU.adjust(java.util.logging.Logger.getLogger(""));
    }

    public void testXSD() throws Exception {
        final URL urlInitial = ResourceU.resolve(Const.XSD);
        final String initialURL = URLCodec.toExternalForm(urlInitial);
        final byte[] xsd = StreamU.read(urlInitial);
        Assert.assertNotNull(xsd);
        Assert.assertEquals(7563, xsd.length);
        Assert.assertEquals("425829fd", CRCU.crc32String(xsd));
        final URL urlAugmentXSLT = new URL(initialURL.replace(".xsd", ".xslt"));
        final byte[] xslt = StreamU.readSafe(urlAugmentXSLT);
        Assert.assertNotNull(xslt);
        Assert.assertEquals(1156, xslt.length);
        Assert.assertEquals("e0183594", CRCU.crc32String(xslt));
    }

    public void testHideInTable() throws Exception {
        final URL urlInitial = ResourceU.resolve(Const.XSD);
        final XsdTypes xsdTypes = new XsdTypes(urlInitial);
        Assert.assertNotNull(xsdTypes);

        final TypeInstance typeInstanceApp = xsdTypes.getElementType("{urn:irby:app}application");
        Assert.assertNotNull(typeInstanceApp);
        final DataType dataTypeApp = typeInstanceApp.getDataType();
        Assert.assertNotNull(dataTypeApp);

        final TypeInstance typeInstanceRealm = dataTypeApp.getInstance("realm");
        Assert.assertNotNull(typeInstanceRealm);
        final DataType dataTypeRealm = typeInstanceRealm.getDataType();
        Assert.assertNotNull(dataTypeRealm);

        final TypeInstance typeInstancePrincipal = dataTypeRealm.getInstance("principal");
        Assert.assertNotNull(typeInstancePrincipal);
        final DataType dataTypePrincipal = typeInstancePrincipal.getDataType();
        Assert.assertNotNull(dataTypePrincipal);

        final TypeInstance typeInstanceCredential = dataTypePrincipal.getInstance("credential");
        Assert.assertNotNull(typeInstanceCredential);
        final DataType dataTypeCredential = typeInstanceCredential.getDataType();
        Assert.assertNotNull(dataTypeCredential);

        final String directiveHideInTable = typeInstanceCredential.getDirective("hideInTable");
        Assert.assertNotNull(directiveHideInTable);
        Assert.assertTrue(Boolean.parseBoolean(directiveHideInTable));
    }

    private static class Const {
        private static final String XSD = "io/github/greyp9/irby/xsd/app/app.xsd";
    }
}
