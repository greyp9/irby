package io.github.greyp9.irby.core.depend2;

import java.net.URI;
import java.util.Objects;

public final class Artifact {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public Artifact(final String groupId, final String artifactId, final String version, final String type) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
    }

    public String getName() {
        return String.format("%s-%s.%s", artifactId, version, type);
    }

    public URI toURI(final String base) {
        return URI.create(String.format("%s/%s/%s/%s/%s", base,
                groupId.replaceAll("\\.", "/"), artifactId, version, getName()));
    }

    @Override
    public String toString() {
        return String.format("[%s][%s]", groupId, artifactId);
    }

    @Override
    public boolean equals(final Object object) {
        return (object != null)
                && (getClass().equals(object.getClass()))
                && (toString().equals(object.toString()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }
}
