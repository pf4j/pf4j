/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.fortsoft.pf4j;

/**
 * @author Decebal Suiu
 */
public class PluginState {

	public static final PluginState CREATED = new PluginState("CREATED");
    public static final PluginState DISABLED = new PluginState("DISABLED");
	public static final PluginState STARTED = new PluginState("STARTED");
	public static final PluginState STOPPED = new PluginState("STOPPED");

	private String status;

	private PluginState(String status) {
		this.status = status;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginState that = (PluginState) o;

        if (!status.equals(that.status)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    @Override
	public String toString() {
		return status;
	}

}
