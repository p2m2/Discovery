import sbt.Keys.scalacOptions
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

import scala.reflect.io.File

val circeVersion = "0.14.0-M1"
val monocleVersion = "2.2.0-M1"//"1.5.1-cats"

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

lazy val reactVerion = "16.13.1"
lazy val udashJqueryVersion = "3.0.4"
lazy val scalaJsReactVersion = "1.7.7"
lazy val scalaJsDOMVersion = "1.1.0"
lazy val playJsonVersion = "2.9.1"
lazy val autowireVersion = "0.3.2"
lazy val upickleVersion  = "1.2.2"
lazy val boopickleVersion = "1.3.3"
lazy val playMongoVersion = "0.20.13-play27"
lazy val mongoScalaDriverVersion = "2.9.0"
lazy val airframeLogVersion = "20.11.0"
lazy val scalaJsScriptsVersion = "1.1.4"
lazy val jwtVersion = "4.2.0"
lazy val seleniumVersion = "3.141.59"
lazy val commonsIoVersion = "2.6"
lazy val jenaVersion = "3.14.0" //"3.16.0"
lazy val utestVersion = "0.7.5"
lazy val scalaParserCombinatorVersion = "1.1.2"
lazy val scalaJHttpVersion = "2.4.2"
lazy val scalaStubVersion = "1.0.0"
lazy val scribeVersion = "2.7.13"
lazy val log4jVersion = "2.14.0"
lazy val scalatagVersion = "0.9.2"
lazy val scalaReflectVersion = "1.0.0"
lazy val RosHttpVersion = "3.0.0"
lazy val scalaReflectPortableVersion = "1.0.0"
lazy val testcontainersScalaVersion="0.38.7"

def sharedSetting(pName: String) = Seq(
  name := pName,
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.13.4",
  organization := "Inrae"
)

def frontEndSharedSetting = Seq(
  resolvers += Resolver.bintrayRepo("scalajs-react-interface", "maven"),
  libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % scalaJsReactVersion,
    "com.github.japgolly.scalajs-react" %%% "extra" % scalaJsReactVersion,
    "com.github.japgolly.scalajs-react" %%% "ext-monocle" % scalaJsReactVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDOMVersion,
    "io.udash" %%% "udash-jquery" % udashJqueryVersion
  ),
  jsDependencies ++= Seq(
  "org.webjars.npm" % "react" % reactVerion / "umd/react.development.js" minified "umd/react.production.min.js" commonJSName "React",
  "org.webjars.npm" % "react-dom" % reactVerion / "umd/react-dom.development.js" minified  "umd/react-dom.production.min.js" dependsOn "umd/react.development.js" commonJSName "ReactDOM",
  "org.webjars.npm" % "react-dom" % reactVerion / "umd/react-dom-server.browser.development.js" minified  "umd/react-dom-server.browser.production.min.js" dependsOn "umd/react-dom.development.js" commonJSName "ReactDOMServer"),
)

lazy val appFrontEnd = (project in file("app-front-end"))
  .settings(
    sharedSetting("app-front-end"),
    scalaJSUseMainModuleInitializer := true,
  )
  .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
  .dependsOn(frontEndShared)

lazy val loginFrontEnd = (project in file("login-front-end"))
  .settings(
    sharedSetting("login-front-end"),
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
  .dependsOn(frontEndShared)

lazy val frontEndShared = (project in file("front-end-shared"))
  .settings(sharedSetting("front-end-shared"))
  .settings(frontEndSharedSetting)
  .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin)
  .dependsOn(sharedJS)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(sharedSetting("shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %%% "play-json" % playJsonVersion,
      "com.lihaoyi" %%% "autowire" % autowireVersion,
      "io.suzaku" %%% "boopickle" % boopickleVersion,
      "org.reactivemongo" %% "play2-reactivemongo" % playMongoVersion,
      "org.mongodb.scala" %% "mongo-scala-driver" % mongoScalaDriverVersion,
      "org.wvlet.airframe" %%% "airframe-log" % airframeLogVersion,
      "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
      "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
      "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test"
    ),
    //scalacOptions ++= Seq("-deprecation", "-feature"),
    libraryDependencies ++= Seq("io.circe" %% "circe-core", "io.circe" %% "circe-generic", "io.circe" %% "circe-parser").map(_ % circeVersion),
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)
  .dependsOn(discovery)

lazy val discovery =crossProject(JSPlatform, JVMPlatform).in(file("discovery"))
  .settings(sharedSetting("discovery"))
  .settings(
    resolvers += Resolver.bintrayRepo("hmil", "maven"),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %%% "play-json" % playJsonVersion,
      "com.lihaoyi" %%% "utest" % utestVersion % "test",
      "com.lihaoyi" %%% "upickle" % upickleVersion,
      "org.wvlet.airframe" %%% "airframe-log" % airframeLogVersion,
      "org.scala-lang.modules" %%% "scala-parser-combinators" % scalaParserCombinatorVersion,
      "org.portable-scala" %%% "portable-scala-reflect" % scalaReflectVersion,
      "fr.hmil" %%% "roshttp" % RosHttpVersion
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.AllLibraryJars,
    coverageMinimum := 40,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
  )
  .jsSettings(
  //  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDOMVersion
    ),
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % scalaStubVersion % "provided",
      "org.apache.jena" % "apache-jena" % jenaVersion pomOnly()
    ))
  //.enablePlugins(ScalaJSBundlerPlugin)

lazy val backEnd = (project in file("back-end"))
  .settings(sharedSetting("backEnd"))
  .settings(
    scalaJSProjects := Seq(appFrontEnd, loginFrontEnd),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      "com.vmunier" %% "scalajs-scripts" % scalaJsScriptsVersion,
      guice,
      specs2 % Test,
      "com.pauldijou" %% "jwt-play" % jwtVersion,
      "com.pauldijou" %% "jwt-core" % jwtVersion,
      "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
      "org.seleniumhq.selenium" % "selenium-remote-driver" % seleniumVersion,
      "org.seleniumhq.selenium" % "selenium-chrome-driver" % seleniumVersion,
      "commons-io" % "commons-io" % commonsIoVersion
    )
  )
  .enablePlugins(PlayScala)
  .dependsOn(sharedJVM)


// Applications static

lazy val table = (project in file("examples-discovery/html/table"))
                .settings(
                  sharedSetting("table"),
                  name := "table",
                  version := "0.1",
                  scalaJSUseMainModuleInitializer := true,
                  mainClass in Compile := Some("inrae.application.TableApp"),
                  libraryDependencies ++= Seq(
                    "com.lihaoyi" %%% "scalatags" % scalatagVersion,
                    "org.portable-scala" %%% "portable-scala-reflect" % scalaReflectPortableVersion
                  )
                )
                .dependsOn(discovery.js)
                .enablePlugins(ScalaJSPlugin)



// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project backEnd" :: s}
Global / onChangedBuildSource := ReloadOnSourceChanges
