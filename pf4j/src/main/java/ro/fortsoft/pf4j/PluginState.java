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

/**
 * @author Decebal Suiu
 */
public class PluginState {

	public static final PluginState CREATED = new PluginState("CREATED");
	public static final PluginState INITIALIZED = new PluginState("INITIALIZED");	
	public static final PluginState STARTED = new PluginState("STARTED");
	public static final PluginState STOPPED = new PluginState("STOPPED");
	public static final PluginState DESTROYED = new PluginState("DESTROYED");
	public static final PluginState FAILED = new PluginState("FAILED");

	private String status;
	
	private PluginState(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}

}
