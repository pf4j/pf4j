#!/bin/sh

#
# This script creates and run the pf4j demo.
#

# create artifacts using maven
mvn clean package

# create demo-dist folder
rm -fr demo-dist
mkdir demo-dist
mkdir demo-dist/plugins

# copy artifacts to demo-dist folder
cp -r demo/app/target/pf4j-demo-*/* demo-dist/
cp demo/plugin1/target/pf4j-demo-plugin1-*.zip demo-dist/plugins/
cp demo/plugin2/target/pf4j-demo-plugin2-*.zip demo-dist/plugins/

# run demo
cd demo-dist
java -jar pf4j-demo-app-*-SNAPSHOT.jar
cd -

