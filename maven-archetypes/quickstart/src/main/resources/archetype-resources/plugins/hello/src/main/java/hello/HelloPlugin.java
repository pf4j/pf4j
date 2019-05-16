package ${package}.hello;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import ${package}.Greeting;

/**
 * A very simple plugin.
 */
public class HelloPlugin extends Plugin {

    public HelloPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("HelloPlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("HelloPlugin.stop()");
    }

    @Extension(ordinal=1)
    public static class HelloGreeting implements Greeting {

        @Override
        public String getGreeting() {
            return "Hello";
        }

    }

}
