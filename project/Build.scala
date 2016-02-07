import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

/**
 * Application settings. Configure the build for your application here.
 * You normally don't have to touch the actual build definition after this.
 */

object RWwScalaJS extends Build {

  type PE = Project => Project

  lazy val commonSettings: PE = _.settings(
    organization := "net.bblfish",
    scalaVersion := "2.11.7",
    version := "0.3.1",
    description := "Read/Write SoLiD User Interface in Scala-JS",
    licenses := Seq("Apache License, Version 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
    homepage := Some(url("https://github.com/read-write-web/rww-scala-js")),
    resolvers += "bblfish-snapshots" at "http://bblfish.net/work/repo/snapshots/",
    publishTo := {
      //eg: export SBT_PROPS=-Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/
      val nexus = "https://oss.sonatype.org/"
      val other = Option(System.getProperty("banana.publish")).map(_.split(":"))
      if (version.value.trim.endsWith("SNAPSHOT")) {
        val repo = other.map(p => Resolver.ssh("banana.publish specified server", p(0), p(1) + "snapshots"))
        repo.orElse(Some("snapshots" at nexus + "content/repositories/snapshots"))
      } else {
        val repo = other.map(p => Resolver.ssh("banana.publish specified server", p(0), p(1) + "releases"))
        repo.orElse(Some("releases" at nexus + "service/local/staging/deploy/maven2"))
      }
    },
    publishArtifact in Test := false,
    //suggested that I add relativeSourceMaps see https://github.com/japgolly/scalajs-react/issues/14
    //but is it still needed?
    relativeSourceMaps := true,
    pomIncludeRepository := { _ => false}
  )

  // only needed for speed test - should be moved to a different subproject

//  utest.jsrunner.Plugin.utestJsSettings

  lazy val root = project.in(file("."))
    .configure(commonSettings)
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "rww-scala-js",
      jsDependencies ++= Seq(
        "org.webjars.bower" % "react"     % "0.14.3" / "react-with-addons.js"
          minified "react-with-addons.min.js"
          commonJSName "React",
        "org.webjars.bower" % "react" % "0.14.3" / "react-dom.js"
          commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"
      ),
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.0-SNAPSHOT",
        //repo: https://github.com/marklister/base64
        //http://central.maven.org/maven2/com/github/marklister/base64_sjs0.6_2.11/0.1.1/
        "com.github.marklister" %%% "base64" % "0.2.0",
        "com.github.japgolly.scalajs-react" %%% "core" % "0.10.4",
        //https://github.com/viagraphs/scalajs-rx-idb
        "com.viagraphs" %%% "scalajs-rx-idb" % "0.0.8-lean-SNAPSHOT",
//      "com.github.japgolly.scalajs-react" %%% "test" % "0.9.0" % "test",
        "com.github.japgolly.scalajs-react" %%% "extra" % "0.10.4",
        "com.github.japgolly.scalajs-react" %%% "ext-scalaz72" % "0.10.4",
        "com.github.japgolly.scalacss" %%% "core" % "0.3.1",
        "com.github.japgolly.scalacss" %%% "ext-react" % "0.3.1",
        "com.lihaoyi" %%% "scalarx" % "0.2.8",
        "com.lihaoyi" %%% "utest" % "0.3.1",
        "org.webjars" % "font-awesome" % "4.3.0-1" % Provided,
        "org.webjars" % "bootstrap" % "3.3.2" % Provided,
        "org.w3" %%% "banana-plantain" % "0.8.2-SNAPSHOT",
        "org.w3" %%% "banana-io-ntriples" % "0.8.2-SNAPSHOT",
        "org.w3" %%% "banana-n3-js" % "0.8.2-SNAPSHOT",
        "org.w3" %%% "banana-jsonld-js" % "0.8.2-SNAPSHOT",
        "org.monifu" %%% "monifu" % "1.0-M2",
        "akka.js" %%% "akkaactor" % "0.2-SNAPSHOT"

        //"org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.7-SNAPSHOT",
        //"org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6",
      ),
      skip in packageJSDependencies := false
    )


}
