import Dependencies._

name := "scala-playground"

version := "0.1"

scalaVersion := "2.13.2"
//scalaVersion := "2.12.5"

//resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies ++=
  catsDependencies ++
  testDependencies