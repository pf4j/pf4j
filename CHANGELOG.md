## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

#### Fixed

#### Changed
- [#512]: Relax Plugin construction (remove dependency on PluginWrapper)

#### Added

#### Removed

### [3.8.0] - 2022-10-27

#### Fixed
- [#492]: Loading extensions crashes kotlin application
- [#508]: Not create extensions.idx if no extensions exist
 
#### Changed
- Make ServiceProviderExtensionFinder optional in demo (commented code)

#### Added
- Add support for reading plugin descriptor from zip
- Use logger instead of System.out.println for demo

#### Removed

### [3.7.0] - 2022-06-28

#### Fixed
- [#435]: Fix the path separator used in the SingletonExtensionFactoryTest.java
- [#451]: Fix Dependency version check fails even if required is '*'
- [#490]: Fix memory leak in SingletonExtensionFactory

#### Changed
- Update mockito version from 2.24.0 to 3.8.0
- [#426]: Update module-info.java
- [#455]: Do not rely on version from Manifest
- Update Log4j version to 2.17.1

#### Added
- [#430]: Add a unit test in AbstractExtensionFinderTest to reproduce #428
- [#450]: Add wrapper to plugin manager (SecurePluginManagerWrapper)
- Add code analyses via Sonar
- Add support for reading plugin descriptor from zip

#### Removed

### [3.6.0] - 2021-01-16

#### Fixed
- [#394]: `DependencyResolver` lost dependent info after plugin stop

#### Changed

#### Added
- [#415]: Externalize some useful classes from testing

#### Removed

### [3.5.0] - 2020-11-08

#### Fixed
- [#378]: Wrong log message
- [#396]: `WrongDependencyVersionException` logs do not contain any info

#### Changed
- [#395]: Make ClassLoadingStrategy's constructor public
- [#398]: Make `DefaultPluginDescriptor#addDependency` usable
- [#402]: Bucketed caching in `SingletonExtensionFactory`

#### Added
- [#400]: Add support for JPMS (`module-info.java`) 
- [#404]: Support multiple plugin root directories 

#### Removed

### [3.4.1] - 2020-08-14

#### Fixed
- [#371]: `ClosedFileSystemException` when I run demo (Windows)
- [#391]: Incorrect enum selection in `ClassLoadingStrategy.ADP`

#### Changed

#### Added

#### Removed

### [3.3.0] - 2020-04-21

#### Fixed
- [#355]: Cannot remove plugin jar file after unloading
- [#359]: Calls to `File.mkdirs` in unzip logic may silently fail
- [#363]: StackOverflow in `AbstractExtensionFinder.findExtensionAnnotation`
- [#366]: Check proper list for debug logging

#### Changed
- [#364]: Failed plugin state added. When plugin failed to start previous state was kept
- [#370]: Improve annotation processor error messages

#### Added
- [#352]: Add `equals`/`hashCode` to some data classes
- [#365]: `PluginClassLoader` does not resolve classpath resources from plugin dependencies

#### Removed

### [3.2.0] - 2019-11-29

#### Fixed
- Fix flaky test `DefaultPluginRepositoryTest.testGetPluginArchive`
- [#349]: Fix Gradle demo

#### Changed
- Add more defense in `ExtensionAnnotationProcessor` (error message if something is wrong)
- Add more tests in `ExtensionAnnotationProcessorTest`

#### Added
- [#348]: Found extensions when using decorated annotations
- [#350]: Support any interface as an `ExtensionPoint`

#### Removed

### [3.1.0] - 2019-09-08

#### Fixed
- [#335]: `DefaultPluginStatusProvider.enablePlugin` function seems to be wrong

#### Changed
- [#328]: Modified `PluginState` to an enum
- [#330]: Make `AbstractPluginManager` fields protected

#### Added
- [#323]: Add IDEA classpath for Development mode
- [#337]: Implement `PluginClassLoader.getResources`

#### Removed

### [3.0.0] - 2019-06-12

#### Fixed
- [#297]: Loading extensions may shutdown the application
- Fix the plugin directory will be deleted anyway in unzip `extract` method
- [#309]: Can't delete/remove unloaded plugin
- [#311]: Wrong file delete on `JarPluginRepository.deletePluginPath`
- [#322]: Fix `FileSystemException` in Windows on plugin delete

#### Changed
- Improve `run-demo` scripts
- [#294]: Configure compound classes to use JAR plugins first
- [#296]: Return extension classes with wildcard type arguments
- [#298]: update ASM library to version 7.1
- Migrate to JUnit 5
- Improve `PluginZip` (used in tests)
- Improve readability of `PropertiesPluginDescriptorFinderTest`
- Convert `PluginException` in `PluginRuntimeException` and use unchecked exceptions in PF4J

#### Added
- [#278]: Make the project build on Java 11
- [#287]: Check no plugin with same `pluginId` is loaded
- [#288]: Document thread safety of `PluginManager`s
- [#292]: Communicate errors with Exceptions where appropriate
- [#306]: Provide an Archetype for new PF4J based projects
- [#307]: Add `JarPluginManager` and `ZipPluginManager`
- Add constants for manifest's attributes names
- Add constants for properties names
- Add `PluginJar` (used in tests)
- Add `AbstractPluginManagerTest`
- Add `DirectedGraphTest`
- Add `JarPluginManagerTest`
- Extract constants for some system property names

#### Removed
- Remove deprecated constructor in `DefaultPluginManager`
 
### [2.6.0] - 2019-02-01

#### Fixed
- [#273]: `ServiceProviderExtensionFinder` should scan the whole classpath
- [#276]: The plugins in the demo did not load successfully
- [#277]: Configures plugin manager to load "HowdyGreeting" using the services

#### Changed
- Improve run demo scripts
- [#248]: Load extensions from plugin libraries
- [#271]: Update `slf4j` dependency to version 1.7.25

#### Added
- [#265]: Explicitly configure extension points for an extension
- [#270]: Optional plugin dependencies
- [#275]: Add automatic module name to `pf4j.jar`

#### Removed

### [2.5.0] - 2018-12-12

#### Fixed
- [#248]: Plugin upgrade version order not guaranteed
- [#250]: Method `DependencyResolver.resolve` removes dependents in check version block 
- [#252]: `java.nio.file.FileSystemNotFoundException`

#### Changed
- [#209]: Why "plugin.properties" is required?

#### Added
- [#242]: Add delete hook method in `Plugin`
- [#256]: Adds ability to configure plugin directory

#### Removed

### [2.4.0] - 2018-08-01

#### Fixed
- [#222]: Correct the class passed to `DefaultPluginFactory's` logger
- [#223]: A disabled plugin is automatically started by `pluginManager.startPlugins()`
- [#229]: Can't find `plugin.properties` file inside `.jar`
- Fix error in `FileUtils.getFileSystem`

#### Changed

#### Added
- [#229]: Add `SingletonExtensionFactory`
- [#229]: Allow a way to query all extension classes for a given plugin

#### Removed

### [2.3.0] - 2018-06-04

#### Fixed
- [#202]: Spaces in name produce 'Illegal character in opaque part at index'
- [#203]: File lock on plugin jar not released

#### Changed
- [#171]: Change the copyright text from the head of files
- [#218]: It should not return null after it detects that the plugin has been loaded
- [#219]: Improve support for Gradle and Kotlin

#### Added
- [#199]: Make optional the plugin class attribute of plugin manifest
- [#206]: Support multiple plugin directories
- Add aliases to the runtime modes (`dev` for `development` and `prod` for `deployment`)

#### Removed

### [2.2.0] - 2018-02-11

#### Fixed
- [#197]: Close `JarFile` stream after `getManifest` in `ManifestPluginDescriptorFinder.find(Path pluginPath)`
- Before start a plugin check if the plugin is resolved
- [#200]: Compiling with Maven under Java9 breaks project

#### Changed
- [#194]: Changing packaging from ZIP to JAR for demo
- Improve `VersionManager` (prepare a new `pf4j-update` release)

#### Added
- [#166]: Simplify main `README` (move the content to http://www.pf4j.org)
- [#190]: Add methods to just get extension classes
- Add `LoggingPluginStateListener` as listener in `DefaultPluginManager` (only for `development` mode)
- Add new `RESOLVED` as plugin state
- Add support for PARENT FIRST loading strategy

#### Removed

### [2.1.0] - 2018-01-10

#### Fixed
- [#177]: Fix Gradle demo
- [#178]: `@Override` should not change method signature
- [#184]: Bug in FileUtils while creating URI on Windows

#### Changed
- [#180]: Refactoring to make `PluginDescriptor` more usable

#### Added

#### Removed

### [2.0.0] - 2017-10-17

#### Fixed
- [#156]: `FileSystemException` when I call `deletePlugin` after `getExtensions`
- Fix Maven warnings

#### Changed
- [#149]: Updated gradle demo dependencies and switched from System.out.println to slf4j log
- Update some code to Java 7
- [#168]: Change root package from `ro.fortsoft.pf4j` to `org.pf4j`
- Open a new extension (protected method) point in `PropertiesPluginDescriptorFinder`

#### Added
- [#146]: Kotlin plugin example added and README updated for Kotlin
- [#150]: Enforce dependencies versions
- [#155]: Add VersionManager abstractization (breaking change)
- [#172]: Add `CompoundPluginDescriptorFinder`
- Add `CompoundPluginLoader`

#### Removed
- Remove `JarPluginManager` (the logic is included in `DefaultPluginManager` via `CompoundXYZ` concept)

### [1.3.0] - 2017-05-03

#### Fixed
- [#129]: Properties Descriptor finder bug fixes and a test
- [#131]: Fix bug in `loadJars()`, did not add `/lib` to classloader
- [#134]: `getVersion()` use wrong class for calculating PF4J version 
- [#135]: `deletePlugin()` failed to delete plugin folder with contents 
- [#137]: The requires Expression does not print well
- [#140]: Unzip plugin zip file in `loadPluginFromPath()`

#### Changed
- [#130]: Refactor validation of PluginDescriptors
- [#138]: Refactor of requires in PluginDescriptor (breaking change) 

#### Added
- [#133]: Support for adding license information to the plugins 
- [#136]: Delete plugin zip on uninstall
- [#139]: Ability to get `pluginsRoot` from PluginManager
- Add constructors with varargs in PluginException

#### Removed

### [1.2.0] - 2017-03-03

#### Fixed
- [#125]: Fix possible NPE

#### Changed
- [#116]: Updated PF4J to newest version in Gradle demo
- Reactivate protection against the issues similar with [#97]

#### Added
- [#128]: Add `JarPluginManager`, `PluginLoader`, `AbstractPluginManager`

#### Removed

### [1.1.1] - 2016-11-17

#### Fixed
- [#116]: Default/System extensions are duplicated

#### Changed

#### Added
- [#111]: Add inheritance support on Extension annotation

#### Removed

### [1.1.0] - 2016-08-22

#### Fixed

#### Changed
- [#107]: PluginDescriptor can't be extended

#### Added
- [#108]: Return a list of all extensions from a plugin and optional for an extension point

#### Removed

### [1.0.0] - 2016-07-07

#### Fixed
- [#99]: NPE in `DefaultPluginManager.stopPlugin()`
- [#100]: Gradle build in demo_gradle is broken
- [#103]: Gradle demos don't build zip with libs
- Fix logging issue in demo

#### Changed
- Rework defense against [#97]
- Eliminate duplicate log messages from demo
- Improve debugging for "no extensions found"

#### Added

#### Removed

### [0.13.1] - 2016-04-01

#### Fixed
- [#98]: WARN ro.fortsoft.pf4j.AbstractExtensionFinder (too many log lines)

### [0.13.0] - 2016-03-28

#### Fixed
- Fix issue with listing files from the jar file in `readPluginsStorages()`
- [#89]: Fix "URI is not hierarchical" issue
- [#91]: Using project lombok with pf4j causes javax.annotation.processing.FilerException

#### Changed
- Log with trace level on PluginClassLoader

#### Added
- Add `distributionManagement` section in `pom.xml`
- Add defense to [#97]
- Add helper `DefaultExtensionFinder.addServiceProviderExtensionFinder()`

#### Removed
- Disable `ServiceProviderExtensionFinder` from `DefaultExtensionFinder`

### [0.12.0] - 2016-01-29

#### Fixed
- [#83]: `stopPlugin()` throws NPE for dependents check
- In development mode hide `plugins/target` folder (it' is not a plugin)

#### Changed
- Add constructor with vararg and make `addFileFilter()` fluent in `AndFileFilter`
- [#84]: remove warn from `DefaultPluginManager.whichPlugin()`
- Pull method `DefaultPluginManager.whichPlugin()` to PluginManager
- Add `getExtensionFactory()` in PluginManager interface

#### Added
- Add constructor with vararg and make addFileFilter method fluent in `AndFileFilter`
- Add `NameFileFilter` and `OrFileFilter`
- [#85]: ExtensionStorage based on Java Service Provider (META-INf/services)

#### Removed

### [0.11.0] - 2015-11-19

#### Fixed
- [#78]: `PluginManager.disablePlugin()` throws UnsupportedOperationExeption

#### Changed
- Make more fields protected in DefaultPluginManager
- [#70]: Improve PluginDescriptorFinder implementations
- Make PluginManager available in Plugin via PluginWrapper

#### Added
- [#66]: Add possibility to overwrite DefaultPluginManager (to create a JarPluginManager)
- Added one more fail test to DefaultPluginFactory
- Added one more fail test to DefaultExtensionFactory
- Added ManifestPluginDescriptorFinder tests

#### Removed

### [0.10.0] - 2015-08-11

#### Fixed
- [#39]: Fix build on JDK 1.8
- [42]: Stop Plugin issue
- [60]: Failed tests

#### Changed
- Improve logging for DefaultExtensionFinder
- Add defense for [#21]: (not find META-INF/extensions.idx)
- [#44]: Replace `Version` class with `semver` lib
- [#55]: Stop plugin leafs first
- [63]: Extended pf4j to allow custom class loaders to be created

#### Added
- [#33]: Add demo build configuration with Gradle
- [#40]: Add Plugin status provider
- [#41]: Added plugin archive source abstraction
- Added test for DefaultPluginRepository

#### Removed

[unreleased]: https://github.com/decebals/pf4j/compare/release-3.8.0...HEAD
[3.8.0]: https://github.com/decebals/pf4j/compare/release-3.7.0...release-3.8.0
[3.7.0]: https://github.com/decebals/pf4j/compare/release-3.6.0...release-3.7.0
[3.6.0]: https://github.com/decebals/pf4j/compare/release-3.5.0...release-3.6.0
[3.5.0]: https://github.com/decebals/pf4j/compare/release-3.4.1...release-3.5.0
[3.4.1]: https://github.com/decebals/pf4j/compare/release-3.4.0...release-3.4.1
[3.4.1]: https://github.com/decebals/pf4j/compare/release-3.3.0...release-3.4.0
[3.3.0]: https://github.com/decebals/pf4j/compare/release-3.2.0...release-3.3.0
[3.2.0]: https://github.com/decebals/pf4j/compare/release-3.1.0...release-3.2.0
[3.1.0]: https://github.com/decebals/pf4j/compare/release-3.0.0...release-3.1.0
[3.0.0]: https://github.com/decebals/pf4j/compare/release-2.6.0...release-3.0.0
[2.6.0]: https://github.com/decebals/pf4j/compare/release-2.5.0...release-2.6.0
[2.5.0]: https://github.com/decebals/pf4j/compare/release-2.4.0...release-2.5.0
[2.4.0]: https://github.com/decebals/pf4j/compare/release-2.3.0...release-2.4.0
[2.3.0]: https://github.com/decebals/pf4j/compare/release-2.2.0...release-2.3.0
[2.2.0]: https://github.com/decebals/pf4j/compare/release-2.1.0...release-2.2.0
[2.1.0]: https://github.com/decebals/pf4j/compare/release-2.0.0...release-2.1.0
[2.0.0]: https://github.com/decebals/pf4j/compare/release-1.3.0...release-2.0.0
[1.3.0]: https://github.com/decebals/pf4j/compare/release-1.2.0...release-1.3.0
[1.2.0]: https://github.com/decebals/pf4j/compare/release-1.1.1...release-1.2.0
[1.1.1]: https://github.com/decebals/pf4j/compare/release-1.1.0...release-1.1.1
[1.1.0]: https://github.com/decebals/pf4j/compare/release-1.0.0...release-1.1.0
[1.0.0]: https://github.com/decebals/pf4j/compare/release-0.13.1...release-1.0.0
[0.13.1]: https://github.com/decebals/pf4j/compare/release-0.13.0...release-0.13.1
[0.13.0]: https://github.com/decebals/pf4j/compare/release-0.12.0...release-0.13.0
[0.12.0]: https://github.com/decebals/pf4j/compare/release-0.11.0...release-0.12.0
[0.11.0]: https://github.com/decebals/pf4j/compare/release-0.10.0...release-0.11.0
[0.10.0]: https://github.com/decebals/pf4j/compare/release-0.9.0...release-0.10.0

[#508]: https://github.com/pf4j/pf4j/pull/512
[#508]: https://github.com/pf4j/pf4j/issues/508
[#492]: https://github.com/pf4j/pf4j/issues/492
[#490]: https://github.com/pf4j/pf4j/pull/490
[#455]: https://github.com/pf4j/pf4j/pull/455
[#451]: https://github.com/pf4j/pf4j/pull/451
[#450]: https://github.com/pf4j/pf4j/pull/450
[#435]: https://github.com/pf4j/pf4j/pull/435
[#430]: https://github.com/pf4j/pf4j/pull/430
[#426]: https://github.com/pf4j/pf4j/pull/426
[#415]: https://github.com/pf4j/pf4j/pull/415
[#404]: https://github.com/pf4j/pf4j/pull/404
[#402]: https://github.com/pf4j/pf4j/pull/402
[#400]: https://github.com/pf4j/pf4j/issues/400
[#398]: https://github.com/pf4j/pf4j/pull/398
[#396]: https://github.com/pf4j/pf4j/issues/396
[#395]: https://github.com/pf4j/pf4j/issues/395
[#394]: https://github.com/pf4j/pf4j/issues/394
[#378]: https://github.com/pf4j/pf4j/issues/378
[#391]: https://github.com/pf4j/pf4j/issues/391
[#371]: https://github.com/pf4j/pf4j/issues/371
[#370]: https://github.com/pf4j/pf4j/pull/370
[#366]: https://github.com/pf4j/pf4j/pull/366
[#365]: https://github.com/pf4j/pf4j/pull/365
[#364]: https://github.com/pf4j/pf4j/pull/364
[#363]: https://github.com/pf4j/pf4j/issues/363
[#359]: https://github.com/pf4j/pf4j/issues/359
[#355]: https://github.com/pf4j/pf4j/issues/355
[#352]: https://github.com/pf4j/pf4j/pull/352
[#350]: https://github.com/pf4j/pf4j/pull/350
[#349]: https://github.com/pf4j/pf4j/pull/349
[#348]: https://github.com/pf4j/pf4j/pull/348
[#337]: https://github.com/pf4j/pf4j/pull/337
[#335]: https://github.com/pf4j/pf4j/issues/335
[#330]: https://github.com/pf4j/pf4j/pull/330
[#328]: https://github.com/pf4j/pf4j/pull/328
[#323]: https://github.com/pf4j/pf4j/pull/323
[#322]: https://github.com/pf4j/pf4j/pull/322
[#311]: https://github.com/pf4j/pf4j/issues/311
[#309]: https://github.com/pf4j/pf4j/issues/309
[#307]: https://github.com/pf4j/pf4j/issues/307
[#306]: https://github.com/pf4j/pf4j/issues/306
[#298]: https://github.com/pf4j/pf4j/pull/298
[#297]: https://github.com/pf4j/pf4j/issues/297
[#296]: https://github.com/pf4j/pf4j/issues/296
[#294]: https://github.com/pf4j/pf4j/issues/294
[#292]: https://github.com/pf4j/pf4j/issues/292
[#288]: https://github.com/pf4j/pf4j/pull/288
[#287]: https://github.com/pf4j/pf4j/pull/287
[#278]: https://github.com/pf4j/pf4j/pull/278
[#277]: https://github.com/pf4j/pf4j/pull/277
[#276]: https://github.com/pf4j/pf4j/pull/276
[#275]: https://github.com/pf4j/pf4j/pull/275
[#273]: https://github.com/pf4j/pf4j/pull/273
[#271]: https://github.com/pf4j/pf4j/pull/271
[#270]: https://github.com/pf4j/pf4j/pull/270
[#265]: https://github.com/pf4j/pf4j/pull/265
[#262]: https://github.com/pf4j/pf4j/pull/262
[#256]: https://github.com/pf4j/pf4j/pull/256
[#252]: https://github.com/pf4j/pf4j/issues/252
[#250]: https://github.com/pf4j/pf4j/issues/250
[#248]: https://github.com/pf4j/pf4j/issues/248
[#242]: https://github.com/pf4j/pf4j/issues/242
[#233]: https://github.com/pf4j/pf4j/pull/233
[#232]: https://github.com/pf4j/pf4j/issues/232
[#229]: https://github.com/pf4j/pf4j/issues/229
[#223]: https://github.com/pf4j/pf4j/issues/223
[#222]: https://github.com/pf4j/pf4j/pull/222
[#219]: https://github.com/pf4j/pf4j/pull/219
[#218]: https://github.com/pf4j/pf4j/issues/218
[#209]: https://github.com/pf4j/pf4j/issues/209
[#206]: https://github.com/pf4j/pf4j/issues/206
[#203]: https://github.com/pf4j/pf4j/issues/203
[#202]: https://github.com/pf4j/pf4j/issues/202
[#200]: https://github.com/decebals/pf4j/issues/200
[#199]: https://github.com/pf4j/pf4j/issues/199
[#197]: https://github.com/decebals/pf4j/pull/197
[#194]: https://github.com/decebals/pf4j/pull/194
[#190]: https://github.com/decebals/pf4j/issues/190
[#184]: https://github.com/decebals/pf4j/issues/184
[#180]: https://github.com/decebals/pf4j/pull/180
[#178]: https://github.com/decebals/pf4j/pull/178
[#177]: https://github.com/decebals/pf4j/pull/177
[#172]: https://github.com/decebals/pf4j/pull/172
[#171]: https://github.com/pf4j/pf4j/issues/171
[#168]: https://github.com/decebals/pf4j/pull/168
[#166]: https://github.com/decebals/pf4j/issues/166
[#156]: https://github.com/decebals/pf4j/issues/156
[#155]: https://github.com/decebals/pf4j/pull/155
[#150]: https://github.com/decebals/pf4j/pull/150
[#149]: https://github.com/decebals/pf4j/pull/149
[#146]: https://github.com/decebals/pf4j/pull/146
[#140]: https://github.com/decebals/pf4j/pull/140
[#139]: https://github.com/decebals/pf4j/pull/139
[#138]: https://github.com/decebals/pf4j/pull/138
[#137]: https://github.com/decebals/pf4j/pull/137
[#136]: https://github.com/decebals/pf4j/pull/136
[#135]: https://github.com/decebals/pf4j/pull/135
[#134]: https://github.com/decebals/pf4j/pull/134
[#133]: https://github.com/decebals/pf4j/pull/133
[#131]: https://github.com/decebals/pf4j/pull/131
[#130]: https://github.com/decebals/pf4j/pull/130
[#129]: https://github.com/decebals/pf4j/pull/129
[#128]: https://github.com/decebals/pf4j/pull/128
[#125]: https://github.com/decebals/pf4j/pull/125
[#122]: https://github.com/decebals/pf4j/pull/122
[#116]: https://github.com/decebals/pf4j/issues/116
[#111]: https://github.com/decebals/pf4j/pull/111
[#108]: https://github.com/decebals/pf4j/pull/108
[#107]: https://github.com/decebals/pf4j/issues/107
[#103]: https://github.com/decebals/pf4j/issues/103
[#100]: https://github.com/decebals/pf4j/issues/100
[#99]: https://github.com/decebals/pf4j/issues/99
[#98]: https://github.com/decebals/pf4j/issues/98
[#97]: https://github.com/decebals/pf4j/issues/97
[#91]: https://github.com/decebals/pf4j/issues/91
[#89]: https://github.com/decebals/pf4j/pull/89
[#85]: https://github.com/decebals/pf4j/issues/85
[#84]: https://github.com/decebals/pf4j/issues/84
[#83]: https://github.com/decebals/pf4j/issues/83
[#78]: https://github.com/decebals/pf4j/issues/78
[#70]: https://github.com/decebals/pf4j/issues/70
[#66]: https://github.com/decebals/pf4j/issues/66
[#63]: https://github.com/decebals/pf4j/issues/63
[#60]: https://github.com/decebals/pf4j/issues/60
[#55]: https://github.com/decebals/pf4j/pull/55
[#44]: https://github.com/decebals/pf4j/pull/44
[#42]: https://github.com/decebals/pf4j/pull/42
[#41]: https://github.com/decebals/pf4j/pull/41
[#40]: https://github.com/decebals/pf4j/pull/40
[#39]: https://github.com/decebals/pf4j/pull/39
[#33]: https://github.com/decebals/pf4j/pull/33
[#21]: https://github.com/decebals/pf4j/issues/21
