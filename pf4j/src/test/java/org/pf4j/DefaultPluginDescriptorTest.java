package org.pf4j;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultPluginDescriptorTest {

    @Test
    void addDependency() {
        // Given a descriptor with empty dependencies
        DefaultPluginDescriptor descriptor = new DefaultPluginDescriptor();
        descriptor.setDependencies("");
        PluginDependency newDependency = new PluginDependency("test");

        // When I add a dependency
        descriptor.addDependency(newDependency);

        // Then the dependency is added
        List<PluginDependency> expected = new ArrayList<>();
        expected.add(newDependency);
        assertEquals(expected, descriptor.getDependencies());
    }
}
