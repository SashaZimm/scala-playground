package day1

import cats._
import cats.data._
import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

// http://eed3si9n.com/herding-cats/Show.html
class E_ShowSpec extends AsyncWordSpec with Matchers {

  "Show" should {
    "reveal as a String" in {
      1.show shouldBe "1"
      "Five".show shouldBe "Five"
    }

    "can create and use Show with toString or function" in {
      object Show {
        def show[A](f: A => String): Show[A] = new Show[A] {
          def show(a: A): String = f(a)
        }

        def fromToString[A]: Show[A] = new Show[A] {
          def show(a: A): String = a.toString
        }

        implicit val catsContravariantForShow: Contravariant[Show] = new Contravariant[Show] {
          override def contramap[A, B](fa: Show[A])(f: B => A): Show[B] =
            show[B](fa.show _ compose f)
        }
      }

      case class Person(name: String)
      case class Car(model: String)

      implicit val personShow = Show.show[Person](_.name)
      implicit val carShow = Show.fromToString[Car]

      Person("Alice").show shouldBe "Alice"
      Car("CR-V").show shouldBe "Car(CR-V)"
    }

  }

}
