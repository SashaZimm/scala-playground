package day0

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

// http://eed3si9n.com/herding-cats/FoldLeft.html
class C_AdHocPolymorphismFoldLeftFunctionSpec extends AsyncWordSpec with Matchers {

  object FoldLeftList {
    def foldLeft[A, B](xs: List[A], b: B, f: (B, A) => B): B = xs.foldLeft(b)(f)
  }

  "FoldLeft Function" should {
    "be defined based on an object" in {

      def sum[A: Monoid](xs: List[A]): A = {
        val m = implicitly[Monoid[A]]
        FoldLeftList.foldLeft(xs, m.mzero, m.mappend)
      }

      sum(List(1,2,3,4)) shouldBe 10
      sum(List("a","b","c")) shouldBe "abc"
    }

    "be able to extract fold left as a trait" in {
      trait FoldLeft[F[_]] {
        def foldLeft[A, B](xs: F[A], b: B, f: (B, A) => B): B
      }

      object FoldLeft {
        implicit val FoldLeftList: FoldLeft[List] = new FoldLeft[List] {
          def foldLeft[A, B](xs: List[A], b: B, f: (B, A) => B): B = xs.foldLeft(b)(f)
        }
      }

      // List is now pulled out of sum function (compare with previous test sum method)
      def sum[M[_]: FoldLeft, A: Monoid](xs: M[A]): A = {
        val m = implicitly[Monoid[A]]
        val fl = implicitly[FoldLeft[M]]
        fl.foldLeft(xs, m.mzero, m.mappend)
      }

      sum(List(1,3,5,7)) shouldBe 16
      sum(List("x","y","z")) shouldBe "xyz"
    }
  }
}
