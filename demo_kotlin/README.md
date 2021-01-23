# PF4J Gradle Demo

This demo assumes that you know the basics of Gradle (Please look at [gradle](https://gradle.org/) for more info)

### Setup/Build

1. Clone the repo
2. Go to demo_gradle `cd demo_kotlin` 
3. run `./gradlew build`

* This will produce:
    * one jar, named app-plugin-demo-uberjar.jar, located in the `app/build/libs/` directory 
    * three plugins zips located in `build/plugins` directory.
* The plugins are:
  * `plugin-hello-plugin-0.0.1.zip`
  * `plugin-welcome-plugin-0.0.1.zip`

### Run the demo

1. Run 

```
 ./gradlew app:run
```

2. The demo's output should look similar to: (Please see `Boot#main()` for more details)
```
2021-01-23 21:20:06,426 INFO org.pf4j.kotlindemo.Boot - ########################################
2021-01-23 21:20:06,427 INFO org.pf4j.kotlindemo.Boot -                PF4J-DEMO                
2021-01-23 21:20:06,427 INFO org.pf4j.kotlindemo.Boot - ########################################
2021-01-23 21:20:06,427 INFO org.pf4j.kotlindemo.Boot - Plugin directory: <Path to the plugin directory>
2021-01-23 21:20:06,455 INFO org.pf4j.DefaultPluginStatusProvider - Enabled plugins: []
2021-01-23 21:20:06,455 INFO org.pf4j.DefaultPluginStatusProvider - Disabled plugins: []
2021-01-23 21:20:06,460 INFO org.pf4j.DefaultPluginManager - PF4J version 3.6.0 in 'deployment' mode
2021-01-23 21:20:06,486 INFO org.pf4j.AbstractPluginManager - Plugin 'welcome-plugin@0.0.1' resolved
2021-01-23 21:20:06,487 INFO org.pf4j.AbstractPluginManager - Plugin 'hello-plugin@0.0.1' resolved
2021-01-23 21:20:06,487 INFO org.pf4j.AbstractPluginManager - Start plugin 'welcome-plugin@0.0.1'
2021-01-23 21:20:06,492 INFO org.pf4j.demo.welcome.WelcomePlugin - WelcomePlugin.start()
2021-01-23 21:20:06,495 INFO org.pf4j.demo.welcome.WelcomePlugin - WELCOMEPLUGIN
2021-01-23 21:20:06,495 INFO org.pf4j.AbstractPluginManager - Start plugin 'hello-plugin@0.0.1'
2021-01-23 21:20:06,497 INFO org.pf4j.demo.hello.HelloPlugin - HelloPlugin.start()
2021-01-23 21:20:06,500 INFO org.pf4j.demo.hello.HelloPlugin - HELLOPLUGIN
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - Found 2 extensions for extension point 'org.pf4j.kotlindemo.api.Greeting'
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - >>> Welcome
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Welcome Alice
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Welcome Bob
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Welcome Trudy
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - >>> Hello
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Hello Alice
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Hello Bob
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - 	>>> Hello Trudy
2021-01-23 21:20:06,515 INFO org.pf4j.kotlindemo.Boot - Extensions added by plugin 'welcome-plugin':
2021-01-23 21:20:06,516 INFO org.pf4j.kotlindemo.Boot - Extensions added by plugin 'hello-plugin':
2021-01-23 21:20:06,516 INFO org.pf4j.AbstractPluginManager - Stop plugin 'hello-plugin@0.0.1'
2021-01-23 21:20:06,516 INFO org.pf4j.demo.hello.HelloPlugin - HelloPlugin.stop()
2021-01-23 21:20:06,516 INFO org.pf4j.AbstractPluginManager - Stop plugin 'welcome-plugin@0.0.1'
2021-01-23 21:20:06,516 INFO org.pf4j.demo.welcome.WelcomePlugin - WelcomePlugin.stop()
```

### Running the JAR

Note that for manually running the uber jar in `demo_kotlin/app/build/libs/app-plugin-demo-uber.jar` the property
pf4j.pluginsDir for example using the parameter `-Dpf4j.pluginsDir=<path to plugins dir>` when running Java.
