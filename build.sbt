ThisBuild / version := "0.1.2-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

assembly / mainClass := Some("MovieRecommendationsALS")

lazy val root = (project in file("."))
  .settings(
    name := "raccomandator",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.3.0",
  "org.apache.spark" %% "spark-sql" % "3.3.0",
  "org.apache.spark" %% "spark-mllib" % "3.3.0")