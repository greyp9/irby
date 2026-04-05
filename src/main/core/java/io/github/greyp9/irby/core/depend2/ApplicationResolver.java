package io.github.greyp9.irby.core.depend2;

import io.github.greyp9.arwo.core.charset.UTF8Codec;
import io.github.greyp9.arwo.core.file.FileU;
import io.github.greyp9.arwo.core.io.StreamU;
import io.github.greyp9.arwo.core.lang.SystemU;
import io.github.greyp9.arwo.core.res.ResourceU;
import io.github.greyp9.arwo.core.value.Value;
import io.github.greyp9.irby.core.app.config.ApplicationConfig;
import io.github.greyp9.irby.core.cl.config.ClassLoaderConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public final class ApplicationResolver {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Collection<ClassLoaderConfig> classLoaderConfigs;
    private final URI uriBase;

    public ApplicationResolver(final ApplicationConfig appConfig) {
        this.classLoaderConfigs = appConfig.getClassLoaderConfigs();
        this.uriBase = new File(SystemU.userHome(), PATH_REPOSITORY).toURI();
    }

    public void resolve() throws IOException {
        final byte[] bytesJson = StreamU.read(ResourceU.resolve(RESOURCE_JSON));
        final JSONObject dependencies = new JSONObject(UTF8Codec.toString(bytesJson));
        for (final ClassLoaderConfig classLoaderConfig : classLoaderConfigs) {
            resolve(classLoaderConfig, dependencies);
        }
    }

    private void resolve(final ClassLoaderConfig classLoaderConfig,
                         final JSONObject dependencies) throws IOException {
        final String name = classLoaderConfig.getName();
        final String target = classLoaderConfig.getResources();
        final JSONObject classloader = Value.asOptional(
                new JSONPointer("/" + name).queryFrom(dependencies), JSONObject.class).orElse(null);
        if (classloader != null) {
            resolve(classloader, target);
        }
    }

    private void resolve(final JSONObject classloader,
                         final String target) throws IOException {
        final String dependencies = classloader.getString(DEPENDENCIES);  // library dependencies
        final JSONArray prune = Value.asOptional(
                new JSONPointer(PATH_PRUNE).queryFrom(classloader), JSONArray.class).orElse(new JSONArray());
        final List<Artifact> exclusions = resolveExclusions(prune);
        final List<Artifact> artifacts = resolveArtifacts(dependencies, exclusions);
        ensureArtifacts(artifacts, uriBase, target);
    }

    private List<Artifact> resolveExclusions(final JSONArray prune) {
        final List<Artifact> dependencyExclusions = new ArrayList<>();
        for (Object o : prune) {
            final JSONObject pruneObject = Value.as(o, JSONObject.class);
            final String groupId = pruneObject.getString(GROUP_ID);
            final String artifactId = pruneObject.getString(ARTIFACT_ID);
            dependencyExclusions.add(new Artifact(groupId, artifactId, null, null));
        }
        return dependencyExclusions;
    }

    private List<Artifact> resolveArtifacts(final String resource,
                                            final List<Artifact> exclusions) throws IOException {
        final byte[] bytesJson = StreamU.read(ResourceU.resolve(resource));
        final JSONObject node = new JSONObject(UTF8Codec.toString(bytesJson));
        final List<Artifact> artifacts = new ArrayList<>();
        resolveArtifact(node, artifacts, exclusions);
        artifacts.sort(Comparator.comparing(Artifact::toString));
        return artifacts;
    }

    private void resolveArtifacts(final JSONObject node,
                                  final Collection<Artifact> artifacts,
                                  final Collection<Artifact> exclusions) throws IOException {
        final JSONArray children = Value.asOptional(
                new JSONPointer(PATH_CHILDREN).queryFrom(node), JSONArray.class).orElse(new JSONArray());
        for (Object object : children) {
            final JSONObject child = Value.asOptional(object, JSONObject.class).orElseThrow(RuntimeException::new);
            resolveArtifact(child, artifacts, exclusions);
        }
    }

    private void resolveArtifact(final JSONObject node,
                                 final Collection<Artifact> artifacts,
                                 final Collection<Artifact> exclusions) throws IOException {
        final String groupId = node.getString(GROUP_ID);
        final String artifactId = node.getString(ARTIFACT_ID);
        final String version = node.getString(VERSION);
        final String type = node.getString(TYPE);
        final Artifact artifact = new Artifact(groupId, artifactId, version, type);
        if (!exclusions.contains(artifact)) {
            artifacts.add(artifact);
            resolveArtifacts(node, artifacts, exclusions);
        }
    }

    private void ensureArtifacts(final List<Artifact> artifacts,
                                 final URI source,
                                 final String location) throws IOException {
        final File targetFolder = new File(location).getParentFile();
        logger.fine(String.format("ARTIFACTS:[%d][%s][%s]",
                artifacts.size(), source.toString(), targetFolder.getAbsolutePath()));
        for (Artifact artifact : artifacts) {
            logger.fine(artifact.toURI(source.toString()).toString());
            ensureArtifactLocal(artifact, source, targetFolder);
        }
    }

    private void ensureArtifactLocal(final Artifact artifact,
                                     final URI source,
                                     final File targetFolder) throws IOException {
        final File fileSource = new File(artifact.toURI(source.toString()));
        final File fileTarget = new File(targetFolder, artifact.getName());
        if (targetFolder.exists() && !fileTarget.exists()) {
            FileU.copy(fileSource, fileTarget);
        }
    }

    // metadata about plugin artifacts
    private static final String RESOURCE_JSON = "io/github/greyp9/irby/core/depend2/dependencies.json";
    private static final String DEPENDENCIES = "dependencies";

    private static final String PATH_REPOSITORY = ".m2/repository";

    private static final String PATH_CHILDREN = "/children";
    private static final String PATH_PRUNE = "/prune";

    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String VERSION = "version";
    private static final String TYPE = "type";
}
