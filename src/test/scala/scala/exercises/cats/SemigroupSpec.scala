package scala.exercises.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import cats.Semigroup
import cats.implicits._

class SemigroupSpec  extends AsyncWordSpec with Matchers {

  "Semigroup" should {
    "combine" in {
      Semigroup[Int].combine(1,2) shouldBe 3
      Semigroup[List[Int]].combine(List(1,2,3), List(4,5,6)) shouldBe List(1,2,3,4,5,6)
      Semigroup[Option[Int]].combine(Option(1), Option(2)) shouldBe Option(3)
      Semigroup[Option[Int]].combine(Option(1), None) shouldBe Option(1)
    }

    "combine functions" in {
      Semigroup[Int => Int].combine(_ + 1, _ * 10).apply(6) shouldBe 67

      // shorthand for above (No Explicit Apply call)
      Semigroup[Int => Int].combine(_ + 1, _ * 10)(6) shouldBe 67
    }

    "combine maps with combine method" in {
      val mapOne = Map("someKey" -> Map("someNestedKey" -> 5))
      val mapTwo = Map("someKey" -> Map("someNestedKey" -> 6))

      // default scala OVERWRITES values with same key
      mapOne ++ mapTwo shouldBe Map("someKey" -> Map("someNestedKey" -> 6))

      // Semigroup combines values with same key
      Semigroup[Map[String, Map[String, Int]]].combine(mapOne, mapTwo) shouldBe Map("someKey" -> Map("someNestedKey" -> 11))
    }

    "combine semigroups using inline syntax" in {
      Option(1) |+| Option(4) shouldBe Option(5)
    }
  }

}
