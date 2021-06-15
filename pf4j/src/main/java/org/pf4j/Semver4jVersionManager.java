package org.pf4j;

import org.pf4j.util.StringUtils;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

/**
 * Implementation for {@link VersionManager}.
 * This implementation uses semver4j (a Java implementation of the SemVer Specification). Before using it you have to include the optional
 * dependency semver4j
 *
 * @author Wolfram Haussig
 */
public class Semver4jVersionManager implements VersionManager {

    /**
     * the parser type of the version - see https://github.com/vdurmont/semver4j#the-semver-object for details
     */
    private SemverType type;

    /**
     * creates a version manager with the given parser type
     * @param type
     */
    public Semver4jVersionManager(SemverType type) {
        this.type = type;
    }

    /**
     * creates a version manager with the NPM parser type which supports ranges
     */
    public Semver4jVersionManager() {
        this(SemverType.NPM);
    }

    /**
     * parses a version with the current parser type
     * @param version
     */
    protected Semver parseVersion(String version) {
        if (version == null)
            return new Semver("", type);
        return new Semver(version, type);
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
        return StringUtils.isNullOrEmpty(constraint) || "*".equals(constraint) || parseVersion(version).satisfies(constraint);
    }

    @Override
    public int compareVersions(String v1, String v2) {
        return parseVersion(v1).compareTo(parseVersion(v2));
    }

    @Override
    public boolean isStable(String version) {
        return parseVersion(version).isStable();
    }
}
