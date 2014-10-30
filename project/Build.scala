import sbt._
import Keys._
import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin._

import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.testing.JSClasspathLoader

import utest.jsrunner._

object Build extends sbt.Build{
  lazy val cross = new BootstrapCrossBuild(
    Seq(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.scala-sbt" % "test-interface" % "1.0"
      ) ++ (
        if (scalaVersion.value startsWith "2.11.") Nil
        else Seq(
          compilerPlugin("org.scalamacros" % s"paradise" % "2.0.0" cross CrossVersion.full),
          "org.scalamacros" %% s"quasiquotes" % "2.0.0"
        )
      ),
      name := "utest"
    ) ++ sharedSettings
  )

  lazy val root = cross.root.aggregate(runner)
  lazy val js = cross.js.settings(
    (jsEnv in Test) := new NodeJSEnv()
  )
  lazy val jvm = cross.jvm.dependsOn(runner).settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.2" % "test"
  )

  lazy val runner = project.settings(sharedSettings:_*)
                           .settings(
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
    name := "utest-runner"
  )

  lazy val jsPlugin = project.in(file("jsPlugin"))
                             .dependsOn(runner)
                             .settings(sharedSettings:_*)
                             .settings(
    scalaVersion := "2.10.4",
    addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.6.0-SNAPSHOT"),
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
    name := "utest-js-plugin",
    sbtPlugin := true
  )

  lazy val sharedSettings = Seq(
    organization := "com.lihaoyi",
    version := Plugin.utestVersion,
    scalaVersion := "2.11.0",
    // Sonatype2
    publishArtifact in Test := false,
    publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),

    pomExtra := (
      <url>https://github.com/lihaoyi/utest</url>
        <licenses>
          <license>
            <name>MIT license</name>
            <url>http://www.opensource.org/licenses/mit-license.php</url>
          </license>
        </licenses>
        <scm>
          <url>git://github.com/lihaoyi/utest.git</url>
          <connection>scm:git://github.com/lihaoyi/utest.git</connection>
        </scm>
        <developers>
          <developer>
            <id>lihaoyi</id>
            <name>Li Haoyi</name>
            <url>https://github.com/lihaoyi</url>
          </developer>
        </developers>
      )
  )
}
