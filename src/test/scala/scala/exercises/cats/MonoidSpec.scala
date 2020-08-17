package scala.exercises.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats._
import cats.implicits._


class MonoidSpec extends AsyncWordSpec with Matchers {

  "Monoid" should {
    "extend semigroup and implement empty allowing semigroups to combine with sensible defaults" in {
      Monoid[String].empty shouldBe ""
      Monoid[Int].empty shouldBe 0
      Monoid[Option[Int]].empty shouldBe None
      Monoid[List[_]].empty shouldBe List()
    }

    "combineAll" in {
      Monoid[String].combineAll(List("a","b","c")) shouldBe "abc"

      val emptyList: List[String] = List()
      Monoid[String].combineAll(emptyList) shouldBe ""
    }

    "combineAll on more complex types" in {
      Monoid[Map[String, Int]]
        .combineAll(List(Map("a" -> 1, "b" -> 2), Map("a" -> 3))) shouldBe Map("a" -> 4, "b" -> 2)

      Monoid[Map[String, Int]]
        .combineAll(List()) shouldBe Map.empty
    }

    "foldMap maps over elements and accumulates (combines) the results, using the available monoid for the type mapped onto" in {
      val integers = List(1,2,3,4,5)
      integers.foldMap(identity) shouldBe 15
      integers.foldMap(i => i.toString) shouldBe "12345"
    }

    "find a monoid for a generic 2-tuple provided by cats" in {
      val integers = List(3,2,1)
      integers.foldMap(i => (i, i.toString)) shouldBe (6 -> "321")
    }

    "use custom monoid for a 2-tuple provided in scope for the given type fold-mapped onto" in {
      // This is what cats provides in the above test
      implicit def monoidTuple[A: Monoid, B: Monoid]: Monoid[(A, B)] =
        new Monoid[(A, B)] {
          def combine(x: (A, B), y: (A, B)): (A, B) = {
            val (xa, xb) = x
            val (ya, yb) = y
            (Monoid[A].combine(xa, ya), Monoid[B].combine(xb, yb))
          }
          def empty: (A, B) = (Monoid[A].empty, Monoid[B].empty)
        }

      val integers = List(1,2,3,4,5)
      // TODO doesn't find monoidTuple with passing explicitly
      integers.foldMap(i => (i, i.toString))(monoidTuple) shouldBe (15, "12345")
    }

  }
}
