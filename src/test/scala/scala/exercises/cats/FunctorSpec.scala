package scala.exercises.cats

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats._
import cats.implicits._

class FunctorSpec extends AsyncWordSpec with Matchers {

  // Functor is a type class with 1 "hole"
  // i.e. types with shape F[*] such as Option, List, Future
  // (Int has no holes)
  // (Tuple2 has 2 holes Tuple2[*, *])

  // The Functor category has a single operator named map:
  // def map[A, B](fa: F[A])(f: A => B): F[B]

  "Functor" should {
    "be able to create for types with their own map method" in {
      implicit val optionFunctor: Functor[Option] = new Functor[Option] {
        override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa map f
      }

      implicit val listFunctor: Functor[List] = new Functor[List] {
        override def map[A, B](fa: List[A])(f: A => B): List[B] = fa map f
      }

      // (Option is a functor that only applies the function when the Option is a Some)
      Functor[Option].map(None: Option[String])(_.length) shouldBe None

      Functor[Option].map(Option("I exist!"))(_.length) shouldBe Some(8)
      Functor[List].map(List("One","Two","Three"))(_.length) shouldBe List(3,3,5)
    }

    "be able to create for a type with no map method" in {
      implicit def function1Functor[In]: Functor[Function1[In, *]] =
        new Functor[Function1[In, *]] {
          def map[A, B](fa: In => A)(f: A => B): Function1[In, B] = fa andThen f
      }

//      implicit val f1f = function1Functor

      case class Thing(name: String) { def nameSize = name.length }

      // TODO how to use the above implicit???

//      Functor[Function1[Thing, Int]].map(Thing("someName"))(size1) shouldBe 8 // Doesn't like Thing passed to .map
      true shouldBe false
    }

    "be able to lift a function from A => B to F[A] => F[B]" in {
      val lenOption: Option[String] => Option[Int] = Functor[Option].lift(_.length)

      lenOption(Some("abcd")) shouldBe Some(4)
    }

    "be able to apply fproduct to pair a value with the result of applying a function to that value" in {
      val source = List("Some", "Random", "Strings")
      val product: Map[String, Int] = Functor[List].fproduct(source)(_.length).toMap

      product should contain only ("Some" -> 4, "Random" -> 6, "Strings" -> 7)
    }

    "be able to composed" in {
      // Given any functor F[_] and any functor G[_] we can compose them to create a new Functor[F[G[_]]
      val listOpt = Functor[List] compose Functor[Option]

      listOpt.map(List(Some(1), None, Some(3)))(_ + 1) shouldBe List(Some(2), None, Some(4))
    }

  }
}
