#!/bin/sh

#
# This script creates and run the pf4j demo.
#

# create artifacts using maven
mvn clean package

# create demo-dist folder
rm -fr demo-dist
mkdir -p demo-dist/plugins

# copy artifacts to demo-dist folder
cp -r app/target/pf4j-demo-app-*-jar-with-dependencies.jar demo-dist/
cp  plugins/plugin*/target/*-jar-with-dependencies.jar demo-dist/plugins/

# run demo
cd demo-dist
java -jar pf4j-demo-app-*.jar
cd -

