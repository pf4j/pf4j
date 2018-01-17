#!/bin/sh

#
# This script creates and run the pf4j demo.
#

# create artifacts using maven
mvn clean package -DskipTests

# create demo-dist folder
rm -fr demo-dist
mkdir -p demo-dist/plugins

# copy artifacts to demo-dist folder
cp -r demo/app/target/pf4j-demo-*/* demo-dist/
cp demo/plugins/*/target/*plugin.jar demo-dist/plugins/

# run demo
cd demo-dist
java -jar pf4j-demo-app-*-SNAPSHOT.jar
cd -

