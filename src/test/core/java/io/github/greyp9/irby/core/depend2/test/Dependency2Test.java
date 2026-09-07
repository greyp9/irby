package io.github.greyp9.irby.core.depend2.test;

import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.file.find.WildcardFileFilter;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.irby.core.app.config.ApplicationConfig;
import io.github.greyp9.irby.core.depend2.ApplicationResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class Dependency2Test {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private static final String RESOURCE_TEST_APP_CONFIG = "io/github/greyp9/irby/core/depend2/app.test.xml";
    private static final File TARGET_FOLDER = FileU.ensureFolder(
            new File(SystemU.tempDir(), Dependency2Test.class.getSimpleName()));

    @Test
    void test_0_EnsureClean() {
        final Collection<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(FileU.listFiles(new File(TARGET_FOLDER, "jdbc"), new WildcardFileFilter("*.jar"))));
        files.addAll(Arrays.asList(FileU.listFiles(new File(TARGET_FOLDER, "kube"), new WildcardFileFilter("*.jar"))));
        files.addAll(Arrays.asList(FileU.listFiles(new File(TARGET_FOLDER, "s3"), new WildcardFileFilter("*.jar"))));
        for (File file : files) {
            FileU.delete(file);
        }
    }

    /// `mvn dependency:tree -pl :arwo-kube -Dscope=runtime -DoutputType=json -DoutputFile=dep.arwo-kube.json`
    /// `mvn dependency:tree -pl :arwo-s3 -Dscope=runtime -DoutputType=json -DoutputFile=dep.arwo-s3.json`
    @Test
    void test_1_ResolveDependencies() throws IOException {
        final URL urlAppConfig = ResourceU.resolve(RESOURCE_TEST_APP_CONFIG);
        Assertions.assertNotNull(urlAppConfig);
        logger.finest(urlAppConfig.toExternalForm());
        final ApplicationConfig applicationConfig = new ApplicationConfig(urlAppConfig);
        final ApplicationResolver applicationResolver = new ApplicationResolver(applicationConfig, TARGET_FOLDER);
        applicationResolver.resolve();
    }

    @Test
    void test_2_VerifyKube() {
        final File[] filesKube = FileU.listFiles(new File(TARGET_FOLDER, "kube"), new WildcardFileFilter("*.jar"));
        final int filesExpected = 30;
        Assertions.assertEquals(filesExpected, filesKube.length);  // 2026-09T
        Assertions.assertTrue(Arrays.stream(filesKube).map(File::getName)
                .collect(Collectors.toList()).contains("arwo-kube-0.3.0-SNAPSHOT.jar"));
    }

    @Test
    void test_2_VerifyS3() {
        final File[] filesS3 = FileU.listFiles(new File(TARGET_FOLDER, "s3"), new WildcardFileFilter("*.jar"));
        final int filesExpected = 48;
        Assertions.assertEquals(filesExpected, filesS3.length);  // 2026-09T
        Assertions.assertTrue(Arrays.stream(filesS3).map(File::getName)
                .collect(Collectors.toList()).contains("arwo-s3-0.3.0-SNAPSHOT.jar"));
    }

    @Test
    void test_2_VerifyJDBC() {
        final File[] filesJDBC = FileU.listFiles(new File(TARGET_FOLDER, "jdbc"), new WildcardFileFilter("*.jar"));
        Assertions.assertEquals(1, filesJDBC.length);  // 2026-09T
        final File file = Arrays.asList(filesJDBC).iterator().next();
        Assertions.assertTrue(Pattern.compile("postgresql-(.*?).jar").matcher(file.getName()).matches());
    }
}
