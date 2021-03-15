package `with`.cats.book.chapter3.functors

import cats.Functor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FunctorExercisesSpec extends AnyWordSpec with Matchers {

  sealed trait Tree[+T]
  final case class Leaf[A](value: A) extends Tree[A]
  final case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

  "3.5.4 Branching Out With Functors" should {
    "implement a Functor for the binary tree data type" in {

      implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
        override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] =
          fa match {
            case Leaf(value) => Leaf(f(value))
            case Branch(left, right) => Branch(map(left)(f), map(right)(f))
          }
      }

      val doubleF = (n: Int) => n * 2

      treeFunctor.map(Leaf(5))(doubleF) shouldBe Leaf(10)
      treeFunctor.map(Branch(Leaf(5), Branch(Leaf(6), Branch(Leaf(7), Leaf(8)))))(doubleF) shouldBe Branch(Leaf(10), Branch(Leaf(12), Branch(Leaf(14), Leaf(16))))
    }

  }

}
