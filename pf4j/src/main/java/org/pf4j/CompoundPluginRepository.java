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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class CompoundPluginRepository implements PluginRepository {

    private static final Logger log = LoggerFactory.getLogger(CompoundPluginRepository.class);

    private List<PluginRepository> repositories = new ArrayList<>();

    public CompoundPluginRepository add(PluginRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("null not allowed");
        }

        repositories.add(repository);

        return this;
    }

    /**
     * Add a {@link PluginRepository} only if the {@code condition} is satisfied.
     *
     * @param repository
     * @param condition
     * @return
     */
    public CompoundPluginRepository add(PluginRepository repository, BooleanSupplier condition) {
        if (condition.getAsBoolean()) {
            return add(repository);
        }

        return this;
    }

    @Override
    public List<Path> getPluginPaths() {
        Set<Path> paths = new LinkedHashSet<>();
        for (PluginRepository repository : repositories) {
            List<Path> pluginPaths = repository.getPluginPaths();
            log.debug("{} -> {}", repository.getName(), pluginPaths);
            paths.addAll(pluginPaths);
        }

        return new ArrayList<>(paths);
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        for (PluginRepository repository : repositories) {
            if (repository.deletePluginPath(pluginPath)) {
                log.debug("Delete plugin '{}' from '{}'", pluginPath, repository.getName());
                return true;
            }
        }

        return false;
    }

}
