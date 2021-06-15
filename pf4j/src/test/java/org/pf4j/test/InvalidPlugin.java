package org.pf4j.test;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * A simple {@link Plugin} which simulates being on a wrong operating system
 *
 * @author Wolfram Haussig
 */
public class InvalidPlugin extends Plugin {

    public InvalidPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    /**
     * In reality, one would check requirements like the correct OS, certain installed applications, ... here
     */
    @Override
    public boolean validate() {
        return false;
    }
}
