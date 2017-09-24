# Demo 2 Application

This is an application to demonstrate the use of pf4j.
This is a derived work of `../demo` project with following changes:

+ The pom.xml is unbelievingly simpler. No more complicated maven-plugin configurations.
+ No other XMLs except `pom.xml`. Not even `assembly.xml`
+ Uses plugin.properties
+ Everything is a .jar and the application runs without extracting the content.
  Trying to avoid .zip and extractions since it requires write permissions at the runtime which maybe unavailable
  to some users (especially many users share the same installation of application).


Run `./run-demo.sh` from this directory for a demo.