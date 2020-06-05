import Dependencies._

enablePlugins(ScalaJSPlugin,ScalaJSBundlerPlugin)

// ECMAScript
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
// CommonJS
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }


ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val playJson   = "com.typesafe.play" %% "play-json" % "2.8.1"
val jena       = "org.apache.jena" % "apache-jena" % "3.14.0" pomOnly()
val pprint     = "com.lihaoyi" %% "pprint" % "0.5.6"

lazy val root = (project in file("."))
  .aggregate(lib.js, lib.jvm)
  .settings(
    publish := {},
    publishLocal := {},
  )

lazy val lib = crossProject(JSPlatform, JVMPlatform).in(file(".")).
  settings(
    name := "HelloWorld",
    version := "0.1-SNAPSHOT",
    libraryDependencies ++= Seq(playJson,pprint),
    libraryDependencies += scalaTest % Test,
    scalacOptions ++= Seq("-deprecation", "-feature")
  ).
  jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0" % "provided",
    libraryDependencies ++= Seq(jena),
  ).
  jsSettings(
    // Add JS-specific settings her
    /*npmDependencies in Compile ++= Seq(
      "@comunica/actor-init-sparql" -> "last",
    ) ,*/
    scalaJSUseMainModuleInitializer := true,
    
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
