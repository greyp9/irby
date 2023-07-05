package io.github.greyp9.irby.xsd.app.transform;

import io.github.greyp9.arwo.core.envsec.EnvironmentSecret;
import io.github.greyp9.arwo.core.jce.AES;
import io.github.greyp9.arwo.core.jce.KeyX;
import io.github.greyp9.arwo.core.lang.SystemU;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

public class TransformOneTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    @Ignore
    public void testTransformOne() throws GeneralSecurityException, IOException {
        final File folderApp = new File(SystemU.userHome(), ".irby");
        Assert.assertTrue(folderApp.exists());
        logger.finest(folderApp.getPath());
        final File fileExpression = new File(folderApp, "environment.txt");
        final EnvironmentSecret environmentSecret = new EnvironmentSecret(fileExpression.getPath(), null);
        final byte[] secret = environmentSecret.recover();

        final String plaintext = "plaintext";
        final SecretKey keyApp = new SecretKeySpec(secret, AES.Const.ALGORITHM);
        // src/main/app/resources/irby-jar/io/github/greyp9/irby/xsd/app/app.xslt
        final KeyX key = new KeyX(keyApp, KeyX.Const.TRANSFORM_GCM, KeyX.Const.PARAM_SPEC_GCM);
        final String ciphertext = key.protect(plaintext);
        logger.info(ciphertext);
        final String plaintextRecover = key.unprotect(ciphertext);
        Assert.assertEquals(plaintext, plaintextRecover);
    }
}
