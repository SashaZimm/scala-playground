import Dependencies._

name := "scala-playground"

version := "0.1"

scalaVersion := "2.13.2"

// Add the line below to ~/.sbt/<version>/global.sbt (create if not present)
resolvers in ThisBuild += "Artima Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies ++=
  akkaStreamsDependencies ++
  catsDependencies ++
  testDependencies

// allows wildcard generic syntax e.g. see FunctorSpec
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-language:postfixOps",
//  "-deprecation",
  "-Ymacro-annotations",
//  "-P:artima-supersafe:config-file:project/supersafe.cfg"
//  "-feature",
//  "-language:_"
)