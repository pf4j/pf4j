/*
 * Copyright (C) 2012-present the original author or authors.
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
package org.pf4j;

import java.util.Arrays;
import java.util.List;

/**
 * {@link ClassLoadingStrategy} will be used to configure {@link PluginClassLoader} loading order
 * and contains all possible options supported by {@link PluginClassLoader} where:
 * {@code
 * A = Application Source (load classes from parent classLoader)
 * P = Plugin Source (load classes from this classloader)
 * D = Dependencies (load classes from dependencies)
 * }
 */
public class ClassLoadingStrategy {

    /**
     * application(parent) -&gt; plugin -&gt; dependencies
     */
    public static final ClassLoadingStrategy APD = new ClassLoadingStrategy(Arrays.asList(Source.APPLICATION, Source.PLUGIN, Source.DEPENDENCIES));

    /**
     * application(parent) -&gt; dependencies -&gt; plugin
     */
    public static final ClassLoadingStrategy ADP = new ClassLoadingStrategy(Arrays.asList(Source.APPLICATION, Source.DEPENDENCIES, Source.PLUGIN));

    /**
     * plugin -&gt; application(parent) -&gt; dependencies
     */
    public static final ClassLoadingStrategy PAD = new ClassLoadingStrategy(Arrays.asList(Source.PLUGIN, Source.APPLICATION, Source.DEPENDENCIES));

    /**
     * dependencies -&gt; application(parent) -&gt; plugin
     */
    public static final ClassLoadingStrategy DAP = new ClassLoadingStrategy(Arrays.asList(Source.DEPENDENCIES, Source.APPLICATION, Source.PLUGIN));

    /**
     * dependencies -&gt; plugin -&gt; application(parent)
     */
    public static final ClassLoadingStrategy DPA = new ClassLoadingStrategy(Arrays.asList(Source.DEPENDENCIES, Source.PLUGIN, Source.APPLICATION));

    /**
     * plugin -&gt; dependencies -&gt; application(parent)
     */
    public static final ClassLoadingStrategy PDA = new ClassLoadingStrategy(Arrays.asList(Source.PLUGIN, Source.DEPENDENCIES, Source.APPLICATION));

    private final List<Source> sources;

    public ClassLoadingStrategy(List<Source> sources) {
        this.sources = sources;
    }

    public List<Source> getSources() {
        return sources;
    }

    public enum Source {
        PLUGIN, APPLICATION, DEPENDENCIES;
    }

}
