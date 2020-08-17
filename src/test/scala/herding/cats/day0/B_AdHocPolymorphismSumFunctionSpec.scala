package herding.cats.day0

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

// http://eed3si9n.com/herding-cats/sum-function.html
class B_AdHocPolymorphismSumFunctionSpec extends AsyncWordSpec with Matchers {

  "Sum Function" should {

    "sum hardcoded type version" in {
      def sum(xs: List[Int]): Int = xs.foldLeft(0) { _ + _ }

      sum(List(1,2,3,4)) shouldBe 10

    }

    "sum with monoid object with hardcoded type" in {
      object IntMonoid {
        def mappend(a: Int, b: Int): Int = a + b
        def mzero: Int = 0
      }

      def sum(xs: List[Int]): Int = xs.foldLeft(IntMonoid.mzero)(IntMonoid.mappend)

      sum(List(5,6,7,8)) shouldBe 26
    }

    "sum with monoid extending trait and pass monoid as parameter to sum function" in {
      object IntMonoid extends Monoid[Int] {
        def mappend(a: Int, b: Int): Int = a + b
        def mzero: Int = 0
      }

      def sum(xs: List[Int], m: Monoid[Int]): Int = xs.foldLeft(m.mzero)(m.mappend)

      sum(List(1,2,3), IntMonoid) shouldBe 6
    }

    "sum with generic monoid passed as parameter" in {
      object IntMonoid extends Monoid[Int] {
        def mappend(a: Int, b: Int): Int = a + b
        def mzero: Int = 0
      }

      def sum[A](xs: List[A], m: Monoid[A]): A = xs.foldLeft(m.mzero)(m.mappend)

      sum(List(2,4,6), IntMonoid) shouldBe 12
    }

    "sum with generic monoid implicit" in {
      object IntMonoid extends Monoid[Int] {
        def mappend(a: Int, b: Int): Int = a + b
        def mzero: Int = 0
      }
      implicit val implicitMonoid = IntMonoid

      def sum[A](xs: List[A])(implicit m: Monoid[A]): A = xs.foldLeft(m.mzero)(m.mappend)

      sum(List(4,5,6)) shouldBe 15
    }

    "sum with generic monoid implicit as a context bound" in {
      object IntMonoid extends Monoid[Int] {
        def mappend(a: Int, b: Int): Int = a + b
        def mzero: Int = 0
      }
      implicit val intMonoid = IntMonoid

      def sum[A: Monoid](xs: List[A]): A = {
        val m = implicitly[Monoid[A]]
        xs.foldLeft(m.mzero)(m.mappend)
      }

      sum(List(2,4,6,8)) shouldBe 20
    }

    "sum with generic monoid context bound implicit resolved from companion object" in {

      trait Monoid[A] {
        def mappend(a: A, b: A): A
        def mzero: A
      }

      // Scala’s implicit resolution rules include the companion object
      object Monoid {
        implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
          def mappend(a: Int, b: Int): Int = a + b
          def mzero: Int = 0
        }

        implicit val StringMonoid: Monoid[String] = new Monoid[String] {
          def mappend(a: String, b: String): String = a + b
          def mzero: String = ""
        }
      }

      def sum[A: Monoid](xs: List[A]): A = {
        val m = implicitly[Monoid[A]]
        xs.foldLeft(m.mzero)(m.mappend)
      }

      sum(List(1,2,3)) shouldBe 6
      sum(List("a","b","c")) shouldBe "abc"
    }

    "sum and pass monoid directly" in {
      val MultiplicationMonoid: Monoid[Int] = new Monoid[Int] {
        def mappend(a: Int, b: Int): Int = a * b
        def mzero: Int = 1
      }

      def sum[A: Monoid](xs: List[A]): A = {
        val m = implicitly[Monoid[A]]
        xs.foldLeft(m.mzero)(m.mappend)
      }

      sum(List(1,2,3,4))(MultiplicationMonoid) shouldBe 24
    }
  }
}
