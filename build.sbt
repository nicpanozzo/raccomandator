ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

assembly / mainClass := Some("popMov")

lazy val root = (project in file("."))
  .settings(
    name := "raccomandator",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0")