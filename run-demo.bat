REM
REM This script creates and run the pf4j demo.
REM

REM create artifacts using maven
call mvn clean package

REM create demo-dist folder
rmdir demo-dist /s /q
mkdir demo-dist
mkdir demo-dist\plugins

REM copy artifacts to demo-dist folder
xcopy demo\app\target\pf4j-demo-app-*.zip demo-dist /s /i
xcopy demo\plugins\plugin1\target\pf4j-demo-plugin1-*.zip demo-dist\plugins /s
xcopy demo\plugins\plugin2\target\pf4j-demo-plugin2-*.zip demo-dist\plugins /s

cd demo-dist

REM unzip app
unzip pf4j-demo-app-*.zip
rm pf4j-demo-app-*.zip

REM run demo
java -jar pf4j-demo-app-*-SNAPSHOT.jar
cd ..
