package ${package};

import org.apache.commons.lang.StringUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.util.List;
import java.util.Set;

/**
 * A boot class that start the application.
 */
public class Boot {

    public static void main(String[] args) {
        // create the plugin manager
        PluginManager pluginManager = new DefaultPluginManager();

        // load the plugins
        pluginManager.loadPlugins();

        // enable a disabled plugin
//        pluginManager.enablePlugin("welcome-plugin");

        // start (active/resolved) the plugins
        pluginManager.startPlugins();

        // retrieves the extensions for Greeting extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println(String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }

        // print extensions from classpath (non plugin)
        System.out.println("Extensions added by classpath:");
        Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
        for (String extension : extensionClassNames) {
            System.out.println("   " + extension);
        }

        System.out.println("Extension classes by classpath:");
        List<Class<? extends Greeting>> greetingsClasses = pluginManager.getExtensionClasses(Greeting.class);
        for (Class<? extends Greeting> greeting : greetingsClasses) {
            System.out.println("   Class: " + greeting.getCanonicalName());
        }

        // print extensions ids for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions added by plugin '%s':", pluginId));
            extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
            for (String extension : extensionClassNames) {
                System.out.println("   " + extension);
            }
        }

        // print extensions instances for Greeting extension point for each started plugin
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions instances added by plugin '%s' for extension point '%s':", pluginId, Greeting.class.getName()));
            List<Greeting> extensions = pluginManager.getExtensions(Greeting.class, pluginId);
            for (Object extension : extensions) {
                System.out.println("   " + extension);
            }
        }

        // print extensions instances from classpath (non plugin)
        System.out.println("Extensions instances added by classpath:");
        List extensions = pluginManager.getExtensions((String) null);
        for (Object extension : extensions) {
            System.out.println("   " + extension);
        }

        // print extensions instances for each started plugin
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions instances added by plugin '%s':", pluginId));
            extensions = pluginManager.getExtensions(pluginId);
            for (Object extension : extensions) {
                System.out.println("   " + extension);
            }
        }

        // stop the plugins
        pluginManager.stopPlugins();
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                pluginManager.stopPlugins();
            }

        });
        */
    }

}
