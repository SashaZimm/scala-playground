package `with`.cats.book

import java.util.Date
import java.util.concurrent.TimeUnit

import cats.Eq
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import cats.instances.int._

class EqExercisesSpec extends AsyncWordSpec with Matchers {

  "Eq" should {

    val convertToEqualizer = ()
    import cats.syntax.eq._ // for === and =!=
    import cats.instances.int._ // Eq[Int]
    import cats.instances.long._ // for Eq[Long]
    import cats.instances.string._ // Eq[String]
    import cats.instances.option._ // Eq[Option]

    "do better than the standard equals" in {
      // IntelliJ warns about different types but compiles!!
      1 == None shouldBe false
    }

    "compare with compile time type checks" in {
      val eqInt = Eq[Int]

      eqInt.eqv(123, 123) shouldBe true
      eqInt.eqv(123, 321) shouldBe false

      assertDoesNotCompile("eqInt.eqv(123, None)")
    }

    "use interface syntax to compare" in {

      // import cats.syntax.eq._
      123 === 123 shouldBe true

      123 === 321 shouldBe false
      123 =!= 321 shouldBe true // (not equal syntax)
    }

    "compare options crudely by re-typing" in {

      // we have Eq instances for Int & Option, NOT Some[Int]
      assertDoesNotCompile("Some(1) === None")

      (Some(1) : Option[Int]) === (None : Option[Int]) shouldBe false
    }

    "compare options in a nicer fashion with the Option apply and empty methods from standard library" in {
      Option(1) === Option.empty[Int] shouldBe false
    }

    "compare options in a nicer fashion with cats interface syntax" in {
      import cats.syntax.option._ // for .some and none

      1.some === none[Int] shouldBe false
    }

    "compare custom types by defining custom Eq instances" in {
      implicit val dateEq: Eq[Date] = Eq.instance[Date] { (date1, date2) =>
        date1.getTime === date2.getTime
      }

      val d1 = new Date()
      TimeUnit.MILLISECONDS.sleep(5)
      val d2 = new Date()

      d1 === d1 shouldBe true
      d1 === d2 shouldBe false
      d1 =!= d2 shouldBe true // (not equal syntax)
    }

    // Exercise from book (above are examples & playground)
    "compare custom Cat case class with custom Eq instance" in {

      case class Cat(name: String, age: Int, colour: String)

      implicit val catEq: Eq[Cat] = Eq.instance { (c1, c2) =>
        c1.name === c2.name && c1.age === c2.age && c1.colour === c2.colour
      }

      // raw Cat assertions
      val patches = Cat("Patches", 5, "Shades of Brown")
      val whiskers = Cat("Whiskers", 10, "White")

      patches === patches shouldBe true
      patches === whiskers shouldBe false
      patches =!= whiskers shouldBe true

      // Option[Cat] assertions
      val patchesOption = Option(patches)
      val emptyCatOption = Option.empty[Cat]

      patchesOption === patchesOption shouldBe true
      patchesOption === emptyCatOption shouldBe false
      patchesOption =!= emptyCatOption shouldBe true
    }


  }

}
