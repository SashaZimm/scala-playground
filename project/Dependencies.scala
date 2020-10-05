import sbt._

object Dependencies {

  val catsVersion = "2.1.1"
  val scalaTestVersion = "3.1.2"

  val akkaStreamsVersion = "2.6.9"

  lazy val akkaStreamsDependencies = Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaStreamsVersion
  )

  lazy val catsDependencies = Seq(
    "org.typelevel" %% "cats-core" % catsVersion
  )

  lazy val testDependencies = Seq(
    "org.scalactic" %% "scalactic" % scalaTestVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "org.typelevel" %% "simulacrum" % "1.0.0" % "test"
  )
}
