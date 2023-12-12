ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

assembly / mainClass := Some("Main")

lazy val root = (project in file("."))
  .settings(
    name := "raccomandator",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.0")