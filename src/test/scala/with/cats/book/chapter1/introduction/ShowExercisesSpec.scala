package `with`.cats.book.chapter1.introduction

import cats.Show
import cats.instances.int._
import cats.instances.string._
import cats.syntax.show._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.util.Date

class ShowExercisesSpec extends AsyncWordSpec with Matchers {

  "1.4.6 Show" should {
    "can be defined if cats instances are in scope" in {
      val showInt = Show.apply[Int]
      val showString = Show.apply[String]

      showInt.show(123) shouldBe "123"
      showString.show("abc") shouldBe "abc"
    }

    "can be called using interface syntax if cats.syntax.show._ is imported" in {
      // (from import cats.syntax.show._)
      123.show shouldBe "123"
      "abc".show shouldBe "abc"
    }

    "can create custom instances by implementing trait" in {
      implicit val dateShow: Show[Date] = new Show[Date] {
        def show(date: Date): String = s"${date.getTime}ms since the epoch."
      }

      new Date().show should endWith("ms since the epoch.")
    }

    "can create custom instances with convenience methods" in {
      implicit val dateShow: Show[Date] =
        Show.show(date => s"${date.getTime}ms since the epoch.")

      new Date().show should endWith("ms since the epoch.")
    }

    "can re-implement printable library exercise with far less code" in {
      case class Cat(name: String, age: Int, colour: String)

      implicit val catShow: Show[Cat] = Show.show[Cat] { cat =>
        // (from import cats.instances.int|string._)
        val name = cat.name.show
        val age = cat.age.show
        val colour = cat.colour.show

        s"$name is a $age year-old $colour cat."
      }

      Cat(name = "Fluffy", age = 7, colour = "white").show shouldBe "Fluffy is a 7 year-old white cat."
    }
  }

}
