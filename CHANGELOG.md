## Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

### [Unreleased][unreleased]

#### Fixed

#### Changed

#### Added

#### Removed

### [0.13.0] - 2016-03-28

#### Fixed
- Fix issue with listing files from the jar file in `readPluginsStorages()`
- [#89]: Fix "URI is not hierarchical" issue
- [#91]: Using project lombok with pf4j causes javax.annotation.processing.FilerException

#### Changed
- Log with trace level on PluginClassLoader

#### Added
- Add `distributionManagement` section in `pom.xml`
- Add defense to [#97] (Create ExtensionPoint in plugin and access that in a different one)
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
 
[unreleased]: https://github.com/decebals/pf4j/compare/release-0.13.0...HEAD
[0.13.0]: https://github.com/decebals/pf4j/compare/release-0.12.0...release-0.13.0
[0.12.0]: https://github.com/decebals/pf4j/compare/release-0.11.0...release-0.12.0
[0.11.0]: https://github.com/decebals/pf4j/compare/release-0.10.0...release-0.11.0
[0.10.0]: https://github.com/decebals/pf4j/compare/release-0.9.0...release-0.10.0

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
