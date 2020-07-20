package day0

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import simulacrum._

// http://eed3si9n.com/herding-cats/method-injection.html
class D_AdHocPolymorphismMethodInjectionSpec extends AsyncWordSpec with Matchers {

  "Method Injection" should {
    "sum two types using the monoid without operator" in {
      def plus[A: Monoid](a: A, b: A): A = implicitly[Monoid[A]].mappend(a, b)

      plus(3,4) shouldBe 7
    }

    "sum with an operator enriched for all types that have an instance of monoid using cats" in {

      @typeclass trait Monoid[A] {
        @op("|+|") def mappend(a: A, b: A): A
        def mzero: A
      }

      object Monoid {
        // ops gets generated
        val syntax = ops
        implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
          def mappend(a: Int, b: Int): Int = a + b
          def mzero: Int = 0
        }
        implicit val StringMonoid: Monoid[String] = new Monoid[String] {
          def mappend(a: String, b: String): String = a + b
          def mzero: String = ""
        }
      }

      import Monoid.syntax._
      3 |+| 4 shouldBe 7
      "a" |+| "b" shouldBe "ab"
    }
  }

}
