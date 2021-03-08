package `with`.cats.book.chapter3.functors

import cats.implicits.catsSyntaxOptionId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.instances.function._
import cats.syntax.functor._


class FunctorsPlaygroundSpec extends AsyncWordSpec with Matchers {

  "Functors" should {
    "be able to apply map in different ways" in {

      val func1: Int => Double = (i: Int) => i.toDouble
      val func2: Double => Double = (d: Double) => d * 2

      (func1 map func2)(1) shouldBe 2.0
      (func1 andThen func2)(1) shouldBe 2.0
      func2(func1(1)) shouldBe 2.0
    }

    "be be used for functional composition (sequencing)" in {

      // calling map appends another operation to the chain but doesn't call anything yet
      val functionalComposition = ((i: Int) => i.toDouble)
        .map(_ + 1)
        .map(_ * 2)
        .map(res => s"$res!")

      // now its called
      functionalComposition(123) shouldBe "248.0!"
    }

    "obey the Functor laws!" in {
      val number = 1.some

      // Law 1 Identity - calling map with the identity function is the same as doing nothing
      number.map(a => a) shouldBe number

      val f = (i: Int) => i * 2
      val g = (i: Int) => i  * 3

      // Law 2 Composition - mapping with 2 functions f and g is the same as mapping with f and then mapping with g
      number.map(n => g(f(n))) shouldBe number.map(f).map(g)
    }
  }

  "Cats Functors" should {
    import cats.Functor
    import cats.instances.list._ // for Functor
    import cats.instances.option._ // for Functor

    "offer a Functor type class" in {
      val list = List(1,2,3)
      Functor[List].map(list)(_ * 2) shouldBe List(2,4,6)

      val option = Some(123)
      Functor[Option].map(option)(_.toString) shouldBe Some("123")
    }

    "offer a lift method to convert a function of A => B to function of Functor[A] => Functor[B]" in {
      val func = (x: Int) => x + 1

      val liftedFunc: Option[Int] => Option[Int] = Functor[Option].lift(func)

      liftedFunc(Option(5)) shouldBe Option(6)
    }

    "offer an as method to replace the value inside the Functor with the given value" in {

      val list = List(1,2,3)

      Functor[List].as(list, 9) shouldBe List(9,9,9)
      Functor[List].as(list, "Quack") shouldBe List("Quack","Quack","Quack")
    }

    "offer a map method as their main benefit" in {

      val func1 = (x: Int) => x + 1
      val func2 = (x: Int) => x * 2
      val func3 = (x: Int) => s"$x!"

      // map method from Cats Functor
      val func4 = func1.map(func2).map(func3)

      func4(123) shouldBe "248!"
    }

    "offer a map method as their main benefit (example that works for any Functor)" in {

      def doMath[F[_]](start: F[Int])
                      (implicit functor: Functor[F]): F[Int] =
        start.map(n => n + 1 * 2)

      doMath(Option(123)) shouldBe Option(125)
      doMath(List(1,2,3)) shouldBe List(3,4,5)
    }

    "allow custom Functor instances to be defined" in {

      // hypothetical - Cats already provides Functor for Option
      implicit val optionFunctor: Functor[Option] = new Functor[Option] {
        def map[A, B](value: Option[A])(func: A => B): Option[B] =
          value.map(func)
      }

      val func1 = (x: Int) => x + 1
      optionFunctor.map(Option(3))(func1) shouldBe Option(4)
    }

    "be able to inject dependencies when creating functor instances (can't add parameters directly to Functor.map)" in {

      trait Dependency{}
      implicit val dependency = new Dependency{}

      // again hypothetical - Cats already provides Functor for Option
      implicit def optionFunctor(implicit injected: Dependency): Functor[Option] = new Functor[Option] {
        override def map[A, B](fa: Option[A])(f: A => B): Option[B] = {
          // The Dependency instance is injected, and available for use by fa
          // A more realistic example would be a futureFunctor that needs an implicit ExecutionContext for it's map method
          // and this would be injected, but I had errors due to multiple implicits found in scope :/
          fa.map(f)
        }
      }


      val func1 = (name: String)  => s"Hello $name"
      optionFunctor.map(Option("Barry"))(func1) shouldBe Some("Hello Barry")
    }

    // TODO: Continue from 3.5.4 "Exercise: Branching out with Functors" on page 75 / 322
  }

}
