import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.jsEnv
import sbt.Keys.testFrameworks

ThisBuild / organization := "org.inrae"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.1"
ThisBuild / name         := "Easy Sparql"

val playJson   = "com.typesafe.play" %% "play-json" % "2.8.1"
val pprint     = "com.lihaoyi" %% "pprint" % "0.5.6"

lazy val root = (project in file("."))
  .aggregate(es.js, es.jvm)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true
  )

// cross-project , documentation : https://github.com/portable-scala/sbt-crossproject
lazy val es =
// select supported platforms
  crossProject(JSPlatform, JVMPlatform).in(file("."))
    .settings(
      libraryDependencies ++= Seq(playJson,pprint),
      libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.8.5",
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
      libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2",
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .jsSettings(
      scalaJSUseMainModuleInitializer := true,
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
      ) // defined in sbt-scalajs-crossproject
    .jvmSettings(
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
      libraryDependencies += "org.dispatchhttp" %% "dispatch-core" % "1.2.0"
    )