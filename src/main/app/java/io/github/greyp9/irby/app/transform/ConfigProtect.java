package io.github.greyp9.irby.app.transform;

import io.github.greyp9.arwo.core.envsec.EnvironmentSecret;
import io.github.greyp9.arwo.core.jce.AES;
import io.github.greyp9.arwo.core.jce.KeyX;
import io.github.greyp9.arwo.core.value.Value;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class ConfigProtect {

    private ConfigProtect() {
    }

    public static void main(final String[] args) throws Exception {
        // console will be used to retrieve user input...
        final Console console = System.console();
        int i = 0;
        // get filesystem path to EnvironmentSecret
        final String secretPath = (args.length > ++i) ? args[i] : console.readLine(PROMPT_SECRET_PATH);
        Value.require(new File(secretPath).exists(), () -> new IOException(String.format("Not found: %s", secretPath)));
        final byte[] secret = new EnvironmentSecret(secretPath, null).recover();
        System.out.println("Secret recovered.");
        final SecretKey key = new SecretKeySpec(secret, AES.Const.ALGORITHM);
        final KeyX keyX = new KeyX(key, KeyX.Const.TRANSFORM_GCM, KeyX.Const.PARAM_SPEC_GCM);
        // protect input value
        final char[] value = (args.length > ++i) ? args[i].toCharArray() : console.readPassword(PROMPT_VALUE_IN);
        System.out.printf("Value protected = %s%n", keyX.protect(new String(value)));
        System.exit(0);
    }

    private static final String PROMPT_SECRET_PATH = "Enter path to environment secret expression: ";
    private static final String PROMPT_VALUE_IN = "Enter value to protect: ";
}
