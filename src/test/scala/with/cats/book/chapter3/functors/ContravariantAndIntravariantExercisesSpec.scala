package `with`.cats.book.chapter3.functors

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class ContravariantAndIntravariantExercisesSpec extends AnyWordSpec with Matchers {

  "3.6.1.1 Showing off with Contramap" should {

    // contramap only makes sense for datatypes that represent transformations

    // represents transformation from A -> String
    trait Printable[A] { self =>
      def format(value: A): String
      def contramap[B](func: B => A) = new Printable[B] {
          override def format(value: B): String = self.format(func(value))
        }
    }

    // Type class instances
    object PrintableInstances {
      implicit val printableString = new Printable[String] {
        def format(value: String) = s"'$value'"
      }

      implicit val printableBoolean = new Printable[Boolean] {
        def format(value: Boolean) = if(value) "yes" else "no"
      }
    }

    object Printable {
      def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)
    }

    final case class Box[A](value: A)

    "use contramap to unbox before applying Printable" in {
      import PrintableInstances._
      import Printable._

      implicit def printableBox[A](implicit p: Printable[A]): Printable[Box[A]] = p.contramap[Box[A]](_.value)

      format(Box("Hello")) shouldBe "'Hello'"
      format(Box(true)) shouldBe "yes"

      // No implicit printableDouble in scope
      assertDoesNotCompile("Printable.format(Box(5.45))")
    }

    // TODO: Continue from "3.6.2 Invariant functors and the imap method" on page 79 / 322

  }

}
