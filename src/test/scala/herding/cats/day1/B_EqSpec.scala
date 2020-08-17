package herding.cats.day1

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import cats._
import cats.data._
import cats.implicits._

// http://eed3si9n.com/herding-cats/Eq.html
class B_EqSpec extends AsyncWordSpec with Matchers {

  "Eq" should {

    // Do the Scala dance!
    // ScalaTest has === method and we want cats version for these tests
    val convertToEqualizer = ()  // shadow ScalaTest
    import cats.syntax.eq._

    "indicate 1 is equal to 1 when using Cats Eq" in {
      1 === 1 shouldBe true
    }

    "indicate 1 not equal to 1 is false when using Cats Neqv" in {
      1 =!= 1 shouldBe false
    }

    "indicate 1 is not equal to 5 when using Cats Eq" in {
      1 === 5 shouldBe false
    }

    "indicate 1 not equal to 5 is true when using Cats Neqv" in {
      1 =!= 5 shouldBe true
    }

    "not fail to compile (losing type safety) when comparing using standard scala Eq" in {
      1 == "Hello" shouldBe false
    }

    "fail to compile when comparing different types with Cats Eq" in {
      assertDoesNotCompile("1 === \"blah\"")
      assertDoesNotCompile("1 === new Object()")
    }
  }

}
