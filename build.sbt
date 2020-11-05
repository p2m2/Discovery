import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.jsEnv
import sbt.Keys.testFrameworks

ThisBuild / organization := "org.inrae"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version      := "0.1"
ThisBuild / name         := "Easy Sparql"

val playJson   = "com.typesafe.play" %% "play-json" % "2.8.1"
val jena       = "org.apache.jena" % "apache-jena" % "3.14.0" pomOnly()

lazy val root = (project in file("."))
  .aggregate(es.js, es.jvm)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true,
    scalacOptions ++= Seq("-deprecation", "-feature")
  )
//
// cross-project , documentation : https://github.com/portable-scala/sbt-crossproject
lazy val es =
// select supported platforms
  crossProject(JSPlatform, JVMPlatform).in(file("."))
    //.jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
    .settings(
      libraryDependencies ++= Seq(playJson),
      libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.8.5",
      libraryDependencies += "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
      libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.2.0",
      libraryDependencies += "com.outr" %%% "scribe" % "2.7.13",  /* logging */
      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .jsSettings(
      //scalaJSUseMainModuleInitializer := true,
      //requireJsDomEnv in Test := true,
      //npmDependencies in Compile += "jsdom" -> "16.4.0",
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
      libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2",
      jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
      ) // defined in sbt-scalajs-crossproject
    .jvmSettings(
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
      //libraryDependencies += "ch.qos.logback"          %  "logback-classic" % "1.2.3",
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
      libraryDependencies ++= Seq(jena)
    )
