package day1

import cats.PartialOrder
import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

// http://eed3si9n.com/herding-cats/PartialOrder.html
class D_PartialOrderSpec extends AsyncWordSpec with Matchers {

  "Partial Order" should {
    "enable tryCompare syntax and return Some when comparable" in {
      1 tryCompare 2 shouldBe Some(-1)
    }

    "not compile when types are not comparable" in {
      assertDoesNotCompile("1 tryCompare 2.0")
    }

    "compare using cats operators and fail to compile when not comparable" in {
      def lt[A: PartialOrder](a1: A, a2: A): Boolean = a1 <= a2

      lt[Int](1,2) shouldBe true
      assertDoesNotCompile("lt[Int](1,2.0)")
    }

  }

}
