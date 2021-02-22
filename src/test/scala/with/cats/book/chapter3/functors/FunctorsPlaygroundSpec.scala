package `with`.cats.book.chapter3.functors

import cats.implicits.catsSyntaxOptionId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.instances.function._
import cats.syntax.functor._ // for map

class FunctorsPlaygroundSpec extends AsyncWordSpec with Matchers {

  "Functors" should {
    "be able to apply map in different ways" in {

      val func1: Int => Double = (i: Int) => i.toDouble
      val func2: Double => Double = (d: Double) => d * 2

      (func1 map func2)(1) shouldBe 2.0
      (func1 andThen func2)(1) shouldBe 2.0
      func2(func1(1)) shouldBe 2.0
    }

    "be be used for functional composition (sequencing)" in {

      // calling map appends another operation to the chain but doesn't call anything yet
      val functionalComposition = ((i: Int) => i.toDouble)
        .map(_ + 1)
        .map(_ * 2)
        .map(res => s"$res!")

      // now its called
      functionalComposition(123) shouldBe "248.0!"
    }

    "obey the Functor laws!" in {
      val number = 1.some

      // Law 1 Identity - calling map with the identity function is the same as doing nothing
      number.map(a => a) shouldBe number

      val f = (i: Int) => i * 2
      val g = (i: Int) => i  * 3

      // Law 2 Composition - mapping with 2 functions f and g is the same as mapping with f and then mapping with g
      number.map(n => g(f(n))) shouldBe number.map(f).map(g)
    }
  }

}
