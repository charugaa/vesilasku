import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "fi.kapsi.kosmik",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "vesilasku",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.7.0",
      scalaTest % Test
    )
  )
