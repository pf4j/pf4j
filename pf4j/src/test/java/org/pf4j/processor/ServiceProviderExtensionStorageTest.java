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

package org.pf4j.processor;

import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Josiah Haswell
 */
public class ServiceProviderExtensionStorageTest {

    @Test
    public void ensureServiceProviderExtensionStorageReadWorks() throws IOException {
        final StringReader file = new StringReader("#hello\n    World");
        final Set<String> entries = new HashSet<>();
        ServiceProviderExtensionStorage.read(file, entries);

        assertThat(entries.size(), is(1));
        assertThat(entries.contains("World"), is(true));
    }

    @Test
    public void ensureReadingExtensionsProducesCorrectListOfExtensions() {
        final StringReader file = new StringReader("#hello\n    World");
        final ExtensionAnnotationProcessor processor = mock(ExtensionAnnotationProcessor.class);
        final Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put("hello", Collections.singleton("world"));

        given(processor.getExtensions()).willReturn(extensions);
        ServiceProviderExtensionStorage extensionStorage = new ServiceProviderExtensionStorage(processor) {

            @Override
            protected Filer getFiler() {
                try {
                    Filer filer = mock(Filer.class);
                    FileObject fileObject = mock(FileObject.class);
                    given(fileObject.openReader(true)).willReturn(file);
                    given(filer.getResource(
                        any(StandardLocation.class),
                        any(String.class),
                        any(String.class)
                    )).willReturn(fileObject);
                    return filer;
                } catch(IOException ex) {
                    throw new IllegalStateException("Shouldn't have gotten here");
                }
            }

        };

        Map<String, Set<String>> read = extensionStorage.read();
        assertThat(read.containsKey("hello"), is(true));
        assertThat(read.get("hello"), is(Collections.singleton("World")));
    }

}
