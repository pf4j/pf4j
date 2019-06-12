#!/bin/sh

# create artifacts using Maven
mvn clean package -DskipTests

# create "dist" directory
rm -fr dist
mkdir -p dist/plugins

# copy plugins to "dist" directory
cp plugins/*/target/*-all.jar dist/plugins/
cp plugins/enabled.txt dist/plugins/
cp plugins/disabled.txt dist/plugins/

cd dist

# unzip app to "dist" directory
jar xf ../app/target/*.zip

# run app
java -jar *.jar

cd -
