package org.pf4j;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.pf4j.util.StringUtils;

/**
 * Implementation for {@link VersionManager}.
 * This implementation uses Maven . Before using it you have to include the optional dependency maven-artifact
 *
 * @author Wolfram Haussig
 */
public class MavenVersionManager implements VersionManager {

    /**
     * parses a version with the current parser type
     * @param version
     */
    protected DefaultArtifactVersion parseVersion(String version) {
        if (version == null)
            return new DefaultArtifactVersion("");
        return new DefaultArtifactVersion(version);
    }

    /**
     * Checks if a version satisfies the specified SemVer {@link Expression} string.
     * If the constraint is empty or null then the method returns true.
     * Constraint examples: {@code >2.0.0} (simple), {@code "1.1.1 || 1.2.3 - 2.0.0"} (range).
     * See https://github.com/vdurmont/semver4j#requirements for more info.
     *
     * @param version
     * @param constraint
     * @return
     */
    @Override
    public boolean checkVersionConstraint(String version, String constraint) {
        if (StringUtils.isNullOrEmpty(constraint) || "*".equals(constraint)) {
            return true;
        }
        try {
            return VersionRange.createFromVersionSpec(constraint).containsVersion(parseVersion(version));
        } catch (org.apache.maven.artifact.versioning.InvalidVersionSpecificationException e) {
            //throw custom InvalidVersionSpecificationException as the interface does not declare an exception to be thrown
            //so we need a RuntimeException here
            throw new InvalidVersionSpecificationException("failed to parse constraint as maven version range: " + constraint, e);
        }
    }

    @Override
    public int compareVersions(String v1, String v2) {
        return parseVersion(v1).compareTo(parseVersion(v2));
    }

    @Override
    public boolean isStable(String version) {
        DefaultArtifactVersion av = parseVersion(version);
        return av.getQualifier() == null || !"SNAPSHOT".equals(av.getQualifier());
    }
    
    public static class InvalidVersionSpecificationException extends RuntimeException
    {
        
        private static final long serialVersionUID = 8636081416771885576L;

        public InvalidVersionSpecificationException( String message, Throwable cause )
        {
            super( message, cause);
        }
    }
}
