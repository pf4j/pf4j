/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents the version of a Plugin and allows versions to be compared.
 * Version identifiers have five components.
 *
 *  1. Major version. A non-negative integer.
 *  2. Minor version. A non-negative integer.
 *  3. Release version. A non-negative integer.
 *  4. Build version. A non-negative integer.
 *  5. Qualifier. A text string.
 *
 * This class is immutable.
 *
 * @author Decebal Suiu
 */
public class PluginVersion implements Comparable<PluginVersion> {

	private Integer major;
	private Integer minor;
	private Integer release;
	private Integer build;
	private String qualifier;

    private PluginVersion() {
    }

	public PluginVersion(int major, int minor, int release) {
		this.major = major;
		this.minor = minor;
		this.release = release;
	}

	public PluginVersion(int major, int minor, int release, int build) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.build = build;
	}

	public PluginVersion(int major, int minor, int release, int build, String qualifier) {
		this.major = major;
		this.minor = minor;
		this.release = release;
		this.build = build;
		this.qualifier = qualifier;
	}

	public static PluginVersion createVersion(String version) {
        if (version == null) {
            return new PluginVersion();
        }

		PluginVersion v = new PluginVersion();

		StringTokenizer st = new StringTokenizer(version, ".");
		List<String> tmp = new ArrayList<String>();
		for (int i = 0; st.hasMoreTokens() && i < 4; i++) {
			tmp.add(st.nextToken());
		}

		int n = tmp.size();
		switch (n) {
			case 0 :
				break;
			case 1 :
				v.major = extractInt(tmp.get(0));
				v.qualifier = extractQualifier(tmp.get(0));
				break;
			case 2 :
				v.major = extractInt(tmp.get(0));
				v.minor = extractInt(tmp.get(1));
				v.qualifier = extractQualifier(tmp.get(1));
				break;
			case 3 :
				v.major = extractInt(tmp.get(0));
				v.minor = extractInt(tmp.get(1));
				v.release = extractInt(tmp.get(2));
				v.qualifier = extractQualifier(tmp.get(2));
				break;
			case 4 :
				v.major = extractInt(tmp.get(0));
				v.minor = extractInt(tmp.get(1));
				v.release = extractInt(tmp.get(2));
				v.build = extractInt(tmp.get(3));
				v.qualifier = extractQualifier(tmp.get(3));
				break;
		}

		return v;
	}

	private static int extractInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException n) {
			int boundary;
			for (boundary = 0; boundary < value.length(); boundary++) {
				char ch = value.charAt(boundary);
				if (!Character.isDigit(ch)) {
					break;
				}
			}
			return Integer.parseInt(value.substring(0, boundary));
		}
	}

	private static String extractQualifier(String value) {
		try {
			Integer.parseInt(value);
			return null;
		} catch (NumberFormatException n) {
			int boundary;
			for (boundary = 0; boundary < value.length(); boundary++) {
				char ch = value.charAt(boundary);
				if (!Character.isDigit(ch)) {
					break;
				}
			}
			return value.substring(boundary);
		}
	}

	public int getMajor() {
		return major == null ? 0 : this.major;
	}

	public int getMinor() {
		return minor == null ? 0 : this.minor;
	}

	public int getRelease() {
		return release == null ? 0 : this.release;
	}

    public int getBuild() {
    	return build == null ? 0 : this.build;
    }

    public String getQualifier() {
		return qualifier;
	}

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append(major);
        if (minor != null) {
        	sb.append('.');
        	sb.append(minor);
        }
        if (release != null) {
        	sb.append('.');
        	sb.append(release);
        }
        if (build != null) {
        	sb.append('.');
        	sb.append(build);
        }
        if (qualifier != null) {
        	sb.append(qualifier);
        }

        return sb.toString();
    }

    @Override
	public int compareTo(PluginVersion version) {
        if (version.getMajor() > getMajor()) {
            return 1;
        } else if (version.getMajor() < getMajor()) {
            return -1;
        }

        if (version.getMinor() > getMinor()) {
            return 1;
        } else if (version.getMinor() < getMinor()) {
            return -1;
        }

        if (version.getRelease() > getRelease()) {
            return 1;
        } else if (version.getRelease() < getRelease()) {
            return -1;
        }

        if (version.getBuild() > getBuild()) {
            return 1;
        } else if (version.getBuild() < getBuild()) {
            return -1;
        }

        return 0;
    }

    /*
    private String extractQualifier(String token) {
    	StringTokenizer st = new StringTokenizer(token, "-");
    	if (st.countTokens() == 2) {
    		return st.
    	}
    }
    */

    // for test only
    public static void main(String[] args) {
        PluginVersion v = PluginVersion.createVersion("4.0.0.123");
        System.out.println(v.toString());
//        v = PluginVersion.createVersion("4.0.0.123-alpha");
//        System.out.println(v.toString());
        PluginVersion v1 = PluginVersion.createVersion("4.1.0");
        System.out.println(v1.toString());
        PluginVersion v2  = PluginVersion.createVersion("4.0.32");
        System.out.println(v2.toString());
        System.out.println(v1.compareTo(v2));
    }

}
