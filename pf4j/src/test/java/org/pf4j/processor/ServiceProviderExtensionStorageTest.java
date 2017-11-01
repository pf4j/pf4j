package org.pf4j.processor;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

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

}
