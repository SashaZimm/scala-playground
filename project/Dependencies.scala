import sbt._

object Dependencies {

  val catsVersion = "2.1.1"
  val scalaTestVersion = "3.1.2"

  lazy val catsDependencies = Seq(
    "org.typelevel" %% "cats-core" % catsVersion
  )

  lazy val testDependencies = Seq(
    "org.scalactic" %% "scalactic" % scalaTestVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
}
