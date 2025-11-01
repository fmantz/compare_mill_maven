import Dependencies._

ThisBuild / scalaVersion     := "2.13.16"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

javacOptions +="-g"

lazy val root = (project in file("."))
  .settings(
    name := "bcel-test",
    libraryDependencies += bcel,
    libraryDependencies += commonsLang,
    libraryDependencies += junit % Test
  )
