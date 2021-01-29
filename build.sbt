import sbt.Keys.scalacOptions
import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val utestVersion = "0.7.7"
lazy val upickleVersion  = "1.2.2"
lazy val airframeLogVersion = "20.11.0"
lazy val scalaParserCombinatorVersion = "1.1.2"
lazy val RosHttpVersion = "3.0.0"
lazy val scalaJsDOMVersion = "1.1.0"
lazy val scalaStubVersion = "1.0.0"
lazy val scalatagVersion = "0.9.2"
lazy val rdf4jVersion = "3.6.0-M2"

//https://jitpack.io/

releaseIgnoreUntrackedFiles := true
val version_build = scala.util.Properties.envOrElse("DISCOVERY_VERSION", "local-SNAPSHOT" )
val SWDiscoveryVersionAtBuildTimeFile = "./shared/src/main/scala/inrae/semantic_web/SWDiscoveryVersionAtBuildTime.scala"


val buildSWDiscoveryVersionAtBuildTimeFile =
  if ( ! reflect.io.File(SWDiscoveryVersionAtBuildTimeFile).exists)
    reflect.io.File(SWDiscoveryVersionAtBuildTimeFile).writeAll(
      Predef.augmentString(
      s"""|
      |package inrae.semantic_web
      |
      |object SWDiscoveryVersionAtBuildTime {
      |   val version : String = "${version_build} - build ${java.time.LocalDate.now.toString}"
      |}""").stripMargin)


def getPackageSetting() = Seq(
  name := "discovery",
  version :=  version_build,
  scalaVersion := "2.13.4",
  organization := "com.github.p2m2",
  organizationName := "p2m2",
  organizationHomepage := Some(url("https://www6.inrae.fr/p2m2")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  homepage := Some(url("https://github.com/p2m2/Discovery")),
  description := "Ease Sparql request on the network MetaboHUB/Semantics Databases.",
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/p2m2/Discovery"),
      "scm:git@github.com:p2m2/Discovery.git"
    )
  ),
  developers := List(
    Developer("ofilangi", "Olivier Filangi", "olivier.filangi@inrae.fr",url("https://github.com/ofilangi"))
  ),
  credentials += {

    val realm = scala.util.Properties.envOrElse("REALM_CREDENTIAL", "" )
    val host = scala.util.Properties.envOrElse("HOST_CREDENTIAL", "" )
    val login = scala.util.Properties.envOrElse("LOGIN_CREDENTIAL", "" )
    val pass = scala.util.Properties.envOrElse("PASSWORD_CREDENTIAL", "" )

    val file_credential = Path.userHome / ".sbt" / ".credentials"

    if (reflect.io.File(file_credential).exists) {
      Credentials(file_credential)
    } else {
        Credentials(realm,host,login,pass)
    }
  },
  publishTo := {
    if (isSnapshot.value)
      Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")
    else
      Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  },
  publishConfiguration := publishConfiguration.value.withOverwrite(true) ,
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
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
      "fr.hmil" %%% "roshttp" % RosHttpVersion ,
      "com.softwaremill.sttp.client3" %%% "core" % "3.0.0"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.AllLibraryJars,
    coverageMinimum := 70,
    coverageFailOnMinimum := false,
    coverageHighlighting := true,
    parallelExecution in Test := false
  )
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .jsSettings(
    npmDependencies in Compile ++= Seq(
      "axios" -> "0.21.1",
      "qs" -> "6.9.6"
    ),
    scalaJSLinkerConfig in (Compile, fastOptJS ) ~= {
      _.withOptimizer(false)
        .withPrettyPrint(true)
        .withSourceMap(true)
    },
    scalaJSLinkerConfig in (Compile, fullOptJS) ~= {
      _.withSourceMap(false)
        .withModuleKind(ModuleKind.CommonJSModule)
    }
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % scalaStubVersion % "provided",
      "org.slf4j" % "slf4j-api" % "1.7.9",
      "org.slf4j" % "slf4j-simple" % "1.7.9",
      "org.eclipse.rdf4j" % "rdf4j-storage" % rdf4jVersion,
      "org.eclipse.rdf4j" % "rdf4j-tools-federation" % rdf4jVersion
    ))

Global / onChangedBuildSource := ReloadOnSourceChanges
//publishTo in ThisBuild :=
