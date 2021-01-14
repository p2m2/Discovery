import sbt.Keys.scalacOptions
import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val utestVersion = "0.7.5"
lazy val upickleVersion  = "1.2.2"
lazy val airframeLogVersion = "20.11.0"
lazy val scalaParserCombinatorVersion = "1.1.2"
lazy val scalaReflectVersion = "1.0.0"
lazy val RosHttpVersion = "3.0.0"
lazy val scalaJsDOMVersion = "1.1.0"
lazy val scalaStubVersion = "1.0.0"
lazy val scalatagVersion = "0.9.2"
lazy val jenaVersion = "3.16.0"

releaseIgnoreUntrackedFiles := true

def getPackageSetting() = Seq(
  name := "discovery",
  version := "0.0.2-SNAPSHOT",
  scalaVersion := "2.13.4",
  organization := "com.github.p2m2"
)

lazy val root = (project in file("."))
  .aggregate(discovery.js, discovery.jvm)
  .settings(
    // crossScalaVersions must be set to Nil on the aggregating project
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val discovery=crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(getPackageSetting())
  .settings(
    resolvers += Resolver.bintrayRepo("hmil", "maven"),
    libraryDependencies ++= Seq(
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
    coverageMinimum := 70,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    // release ->  https://oss.sonatype.org/service/local/staging/deploy/maven2
    publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots"),
    publishMavenStyle := true,
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDOMVersion
    ),
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .jvmSettings(
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % scalaStubVersion % "provided",
      "org.apache.jena" % "apache-jena" % jenaVersion pomOnly()
    ))
  //.enablePlugins(ScalaJSBundlerPlugin)

Global / onChangedBuildSource := ReloadOnSourceChanges
//publishTo in ThisBuild :=
