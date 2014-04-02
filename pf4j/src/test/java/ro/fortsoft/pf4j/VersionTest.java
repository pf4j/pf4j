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

import org.junit.Test;

public class VersionTest extends org.junit.Assert {

	@Test
	public void testStandardVersionParsing() {
		PluginVersion v = PluginVersion.createVersion("4.0.0.123");
		assertEquals("4.0.0.123", v.toString());

		PluginVersion v1 = PluginVersion.createVersion("4.1.0");
		assertEquals("4.1.0", v1.toString());
		assertEquals(1, v.compareTo(v1));

		PluginVersion v2 = PluginVersion.createVersion("4.0.32");
		assertEquals("4.0.32", v2.toString());
		assertEquals(-1, v1.compareTo(v2));
	}

	@Test
	public void testMavenVersionParsing() {
		PluginVersion v1 = PluginVersion.createVersion("4-SNAPSHOT");
		assertEquals("4-SNAPSHOT", v1.toString());

		PluginVersion v2 = PluginVersion.createVersion("4.1.0.0-SNAPSHOT");
		assertEquals("4.1.0.0-SNAPSHOT", v2.toString());
		assertEquals(1, v1.compareTo(v2));

		PluginVersion v3 = PluginVersion.createVersion("4.1-SNAPSHOT");
		assertEquals("4.1-SNAPSHOT", v3.toString());
		assertEquals(0, v2.compareTo(v3));
		assertEquals(1, v1.compareTo(v3));

		PluginVersion v4 = PluginVersion.createVersion("4.1.0.123-SNAPSHOT");
		assertEquals("4.1.0.123-SNAPSHOT", v4.toString());
		assertEquals(1, v1.compareTo(v4));
		assertEquals(1, v2.compareTo(v4));
		assertEquals(1, v3.compareTo(v4));
	}
}
