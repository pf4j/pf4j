# PF4J demos

Two demo applications, one built with Maven and one with Gradle. They are not two
packagings of the same thing: each covers a different set of features, and between
them they show both plugin archive formats.

For a starting point for your own application, generate a project from the
`pf4j-quickstart` archetype instead, see
[Quickstart](https://pf4j.org/dev/quickstart.html). The demos are a catalogue of
what the framework can do, so they turn on more than a new project needs.

## Layout

```
demo/
  maven/       a module of the root pom
    api/         Greeting extension point, plugin base class, plugin context
    app/         the application, its own extensions, a custom plugin manager
    plugins/     welcome-plugin and hello-plugin, packaged as fat jars
  gradle/      a separate build
    api/         Greeting extension point
    app/         the application and its own extension
    plugins/     welcome-plugin, hello-plugin and KotlinPlugin, packaged as zips
```

## Maven or Gradle

|  | maven | gradle |
| --- | --- | --- |
| plugin archive | fat jar, built with `jar-with-dependencies` | zip, with `classes/` and `lib/` |
| plugin manager | `DemoPluginManager`, a named subclass | anonymous subclass inside `Boot` |
| extensions in the application | `WhazzupGreeting`, and `HowdyGreeting` when enabled | `WhazzupGreeting` |
| annotation processing | default javac processing, `<proc>full</proc>` on JDK 21 and later | `annotationProcessor` for Java, `kapt` for Kotlin |

## Running the demos

Maven, from the repository root: `./run-demo.sh`. It packages the project,
assembles `demo-dist/` and starts the application.

Gradle: see [gradle/README.md](gradle/README.md).

## What each piece demonstrates

Most classes in the demo exist because a feature needs a place where it is really
used. Paths are relative to `demo/`.

| Feature | Where |
| --- | --- |
| plugin context in the constructor instead of a `PluginWrapper`, built by a custom factory (#512) | `maven/api/PluginContext`, `maven/api/DemoPlugin`, `maven/app/DemoPluginFactory`, `maven/app/DemoPluginManager` |
| custom parent delegation in the plugin class loader (#633) | `maven/app/DemoPluginLoader`, `maven/app/DemoPluginClassLoader` |
| extension declared in the application rather than in a plugin, supported since 0.9 | `maven/app/WhazzupGreeting` |
| extension found through `META-INF/services` instead of `@Extension` | `maven/app/HowdyGreeting` |
| extension ordering with `@Extension(ordinal = 1)` | `HelloGreeting` in `plugin2`, both demos |
| plugin written in Kotlin, annotation processor run by `kapt` | `gradle/plugins/plugin3` |

`Boot` calls five `PluginManager` methods in a row, `getExtensions(Class)`,
`getExtensions(Class, String)`, `getExtensions(String)`, `getExtensionClasses(Class)`
and `getExtensionClassNames(String)`, so you can compare what each returns. That is
why its output reads as repetitive.

## Switches and deliberate settings

These read as leftovers. Each one is there on purpose, and most are switches you
can flip.

* `addServiceProviderExtensionFinder()`, commented out in `DemoPluginManager`.
  Uncomment it to try discovery through `ServiceLoader`, which is what brings in
  `HowdyGreeting`.
* `maven/plugins/enabled.txt` and `maven/plugins/disabled.txt`, with every entry
  commented out. Uncomment a plugin id to filter what gets loaded.
* `RuntimeMode.DEVELOPMENT` in `WelcomePlugin.start()`, one extra log line. It
  exercises the development mode, where plugins are read from the build output
  instead of an archive.
* different commons-lang3 versions in `maven/app` and `maven/plugins/plugin1`.
  The mismatch shows a plugin getting its own copy of a library through its own
  class loader. Do not align them.
