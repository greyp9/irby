package io.github.greyp9.irby.core.depend.test;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.value.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DependencyTest {
    private final Logger logger = Logger.getLogger(getClass().getName());

    static Stream<Arguments> arguments() {
        final String[] artifactsPruneKube = new String[] {
                "com.google.code.findbugs:jsr305",
                "com.google.errorprone:error_prone_annotations",
                "io.github.greyp9:arwo-app",
                "io.github.greyp9:arwo-core",
                "io.prometheus:simpleclient_tracer_otel",
                "io.prometheus:simpleclient_tracer_otel_agent",
                "io.swagger:swagger-annotations",
                "javax.annotation:javax.annotation-api",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
                "org.json:json",
        };

        final URI uriLocal = new File(SystemU.userHome(), ".m2/repository").toURI();
/*
        final URI uriRemote = URI.create("https://repo.maven.apache.org/maven2/");
                new File(SystemU.userHome(), ".m2/repository").toURI();
*/
        final Arguments[] argumentsArray = {
                Arguments.arguments(uriLocal, "io/github/greyp9/irby/core/depend/arwo-kube.json",
                        Arrays.asList(artifactsPruneKube), "target/lib/kube"),
        };
        return Arrays.stream(argumentsArray);
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void testFetchDependencies(final URI uriBase,
                               final String resourceDependencies,
                               final Collection<String> artifactsPrune,
                               final String pathOutput) throws IOException {
        final URL resource = ResourceU.resolve(resourceDependencies);
        Assertions.assertNotNull(resource);
        final byte[] bytes = StreamU.read(resource);

        final Collection<URI> resourcesToFetch = new TreeSet<>();
        final JSONObject dependency = new JSONObject(UTF8Codec.toString(bytes));
        resolveDependency(uriBase, dependency, artifactsPrune, resourcesToFetch);

        logger.info(String.format("RESOURCES: %d %s", resourcesToFetch.size(), pathOutput));
        FileU.ensureFolder(new File(pathOutput));
        for (URI resourceToFetch : resourcesToFetch) {
            final File targetFile = new File(pathOutput, new File(resourceToFetch).getName());
            if (!targetFile.exists()) {
                FileU.copy(new File(resourceToFetch), targetFile);
            }
        }
    }

    private void resolveDependency(final URI uriBase,
                                   final JSONObject dependency,
                                   final Collection<String> artifactsPrune,
                                   final Collection<URI> targetResources) {
        final String groupId = dependency.getString("groupId");
        final String artifactId = dependency.getString("artifactId");
        final String version = dependency.getString("version");
        final String type = dependency.getString("type");
        final boolean prune = artifactsPrune.contains(String.format("%s:%s", groupId, artifactId));
        if (!prune) {
            targetResources.add(toURI(uriBase, groupId, artifactId, version, type));
            resolveChildren(uriBase, dependency, artifactsPrune, targetResources);
        }
    }


    private void resolveChildren(final URI uriBase,
                                 final JSONObject dependency,
                                 final Collection<String> artifactsPrune,
                                 final Collection<URI> targetResources) {
        final JSONArray children = Value.asOptional(
                new JSONPointer("/children").queryFrom(dependency), JSONArray.class).orElse(new JSONArray());
        for (Object object : children) {
            final JSONObject child = Value.asOptional(object, JSONObject.class).orElseThrow(RuntimeException::new);
            resolveDependency(uriBase, child, artifactsPrune, targetResources);
        }
    }

    public URI toURI(final URI uriBase,
                     final String groupId,
                     final String artifactId,
                     final String version,
                     final String type) {
        final String filename = String.format("%s-%s.%s", artifactId, version, type);
        return URI.create(String.format("%s/%s/%s/%s/%s", uriBase,
                groupId.replaceAll("\\.", "/"), artifactId, version, filename));
    }
}
