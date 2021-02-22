package `with`.cats.book.chapter1.introduction

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class PrintableLibrarySpec extends AsyncWordSpec with Matchers {

  "1.3 The Printable Library" should {

    // type class
    trait Printable[A] {
      def format(value: A): String
    }

    // type class instances
    object PrintableInstances {
      implicit val printableString = new Printable[String] {
        def format(value: String): String = value
      }

      implicit val printableInt = new Printable[Int] {
        def format(value: Int): String = value.toString
      }
    }

    // Interface object
    object Printable {
      def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)
      def print[A](value: A)(implicit p: Printable[A]): Unit = println(format(value))
    }

    "find implicit instances for string and int when imported" in {
      import PrintableInstances._
      Printable.format("Hello") shouldBe "Hello"
      Printable.format(5) shouldBe "5"
    }

    "be usable for case classes when an implicit instance defined" in {
      case class Cat(name: String, age: Int, colour: String)

      import PrintableInstances._
      implicit val catPrintable = new Printable[Cat] {
        override def format(value: Cat): String = {
          val name = Printable.format(value.name)
          val age = Printable.format(value.age)
          val colour = Printable.format(value.colour)

          s"$name is a $age year-old $colour cat."
        }
      }

      val cat = Cat(name = "Whiskers", age = 4, colour = "brown")

      Printable.format(cat) shouldBe "Whiskers is a 4 year-old brown cat."
    }

    "be usable via a PrintableSyntax for cleaner code" in {
      case class Cat(name: String, age: Int, colour: String)

      import PrintableInstances._
      implicit val catPrintable = new Printable[Cat] {
        override def format(value: Cat): String = {
          val name = Printable.format(value.name)
          val age = Printable.format(value.age)
          val colour = Printable.format(value.colour)

          s"$name is a $age year-old $colour cat."
        }
      }

      object PrintableSyntax {
        implicit class PrintableOps[A](value: A) {
          def format(implicit p: Printable[A]): String = p.format(value)
          def print(implicit p: Printable[A]): Unit = println(format(p))
        }
      }

      // allows cat.print syntax
      import PrintableSyntax._
      Cat("Whiskers", 4, "brown").format shouldBe "Whiskers is a 4 year-old brown cat."
    }

  }
}
