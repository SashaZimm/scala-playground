package day1

import cats._
import cats.data._
import cats.implicits._

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

// http://eed3si9n.com/herding-cats/typeclasses-102.html
class I_TypeClasses102Spec extends AsyncWordSpec with Matchers {

  "Typeclasses and Datatypes" should {

    // ScalaTest has === method and we want cats version for these tests
    val convertToEqualizer = ()  // shadow ScalaTest
    import cats.syntax.eq._

    "be comparable with Eq" in {

      sealed trait TrafficLight
      object TrafficLight {
        // Need these 3 methods to overcome Cats Eq not being picked up
        def red: TrafficLight = Red
        def yellow: TrafficLight = Yellow
        def green: TrafficLight = Green
        case object Red extends TrafficLight
        case object Yellow extends TrafficLight
        case object Green extends TrafficLight
      }

      implicit val trafficLightEq: Eq[TrafficLight] = new Eq[TrafficLight] {
        def eqv(a1: TrafficLight, a2: TrafficLight): Boolean = a1 == a2
      }

      // Eq[TrafficLight] does not get picked up as Eq has non-variant subtyping
      assertDoesNotCompile("TrafficLight.Red === TrafficLight.Yellow")
      TrafficLight.red === TrafficLight.yellow shouldBe false
      TrafficLight.green === TrafficLight.green shouldBe true
    }

  }

}
