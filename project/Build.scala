import sbt.Keys._
import sbt._

import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object ScalajsReact extends Build {

  type PE = Project => Project

  lazy val commonSettings: PE = _.settings(
    organization := "net.bblfish",
    scalaVersion := "2.11.2",
    version := "0.1",
    description := "read write web User Interface in Scala-JS",
    licenses := Seq("Apache License, Version 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
    homepage := Some(url("https://github.com/read-write-web/rww-scala-js")),
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
    pomIncludeRepository := { _ => false}
  )

  //suggested that I add relativeSourceMaps see https://github.com/japgolly/scalajs-react/issues/14
  lazy val scalaJSSettingsPlus = scalaJSSettings ++ Seq(
    relativeSourceMaps := true
  )
  // only needed for speed test - should be moved to a different subproject

  utest.jsrunner.Plugin.utestJsSettings

  lazy val root = project.in(file("."))
    .configure(commonSettings)
    .settings(scalaJSSettingsPlus:_*)
    .settings(
      name := "rww-scala-js",
      jsDependencies += "org.webjars" % "react" % "0.12.1" / "react-with-addons.js" commonJSName "React",
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core" % "0.6.0",
        "com.github.japgolly.scalajs-react" %%% "test" % "0.6.0" % "test",
        "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % "0.6.0",
        "org.w3" %%% "plantain_js" % "0.7.2-SNAPSHOT"
//        "org.w3" %%% "ntriples_js" % "0.7.2-SNAPSHOT"

        //"org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.7-SNAPSHOT",
        //"org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6",
      ),
      skip in packageJSDependencies := false
    )

  def useReact(scope: String = "compile"): PE =
    _.settings(
      jsDependencies += "org.webjars" % "react" % "0.11.1" % scope / "react-with-addons.js" commonJSName "React",
      skip in packageJSDependencies := false)


}
