import Dependencies._

name := "scala-playground"

version := "0.1"

scalaVersion := "2.13.2"

resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies ++=
  catsDependencies ++
  testDependencies

scalacOptions ++= Seq(
//  "-deprecation",
  "-Ymacro-annotations",
//  "-feature",
//  "-language:_"
)