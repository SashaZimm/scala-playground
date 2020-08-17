package herding.cats.day1

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.Order
import cats.data._
import cats.implicits._

// http://eed3si9n.com/herding-cats/Order.html
class C_OrderSpec extends AsyncWordSpec with Matchers {

  "Order" should {
    "compare different types when using standard Scala" in {
      1 > 2.0 shouldBe false
    }

    "not compile when comparing different types using Cats" in {
      assertDoesNotCompile("1 compare 2.0")
    }

    "compare same types when using Cats compare" in {
      // Normally written as 1.0 compare 2.0 but overshadowed by scala runtime version
      Order.compare(1.0, 2.0) shouldBe -1

      // Normally written as 1.0 max 2.0 but overshadowed by scala runtime version
      Order.max(1.0, 2.0) shouldBe 2
    }
  }

}
