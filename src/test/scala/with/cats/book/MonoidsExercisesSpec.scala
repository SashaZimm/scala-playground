package `with`.cats.book

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class MonoidsExercisesSpec extends AsyncWordSpec with Matchers {

  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }

  object Monoid {
    def apply[A](implicit monoid: Monoid[A]) = monoid

    implicit val booleanAndMonoid: Monoid[Boolean] = new Monoid[Boolean] {
      override def combine(x: Boolean, y: Boolean):Boolean = x && y
      override def empty = true
    }

    implicit val booleanOrMonoid: Monoid[Boolean] = new Monoid[Boolean] {
      override def combine(x: Boolean, y: Boolean):Boolean = x || y
      override def empty = false
    }

    implicit val booleanXorMonoid: Monoid[Boolean] = new Monoid[Boolean] {
      override def combine(x: Boolean, y: Boolean):Boolean = (x && !y) || (!x && y)
      override def empty = false
    }

    implicit val booleanXNorMonoid: Monoid[Boolean] = new Monoid[Boolean] {
      override def combine(x: Boolean, y: Boolean):Boolean = (!x || y) && (x || !y)
      override def empty = true
    }

    // Need def here (for all Set Monoids) to allow type parameter
    implicit def setUnionMonoid[A]: Monoid[Set[A]] =  new Monoid[Set[A]] {
      override def combine(x: Set[A], y: Set[A]): Set[A] = x union y
      override def empty: Set[A] = Set.empty[A]
    }

    implicit def setIntersectionMonoid[A]: Semigroup[Set[A]] =  new Semigroup[Set[A]] {
      override def combine(x: Set[A], y: Set[A]): Set[A] = x intersect y
      // No identity element for intersection (empty set and nothing is common to both!)
    }

    implicit def setSymmetricDifferenceMonoid[A]: Monoid[Set[A]] =  new Monoid[Set[A]] {
      override def combine(x: Set[A], y: Set[A]): Set[A] = (x diff y) union (y diff x)
      override def empty: Set[A] = Set.empty[A]
    }
  }

  "2.3 The Truth About Monoids" should {
    "use AND monoid with identity true" in {
      Monoid.booleanAndMonoid.combine(true, false) shouldBe false
      Monoid.booleanAndMonoid.combine(false, true) shouldBe false
      Monoid.booleanAndMonoid.combine(true, true) shouldBe true
      Monoid.booleanAndMonoid.combine(false, false) shouldBe false
    }

    "use OR monoid with identity false" in {
      Monoid.booleanOrMonoid.combine(true, false) shouldBe true
      Monoid.booleanOrMonoid.combine(false, true) shouldBe true
      Monoid.booleanOrMonoid.combine(true, true) shouldBe true
      Monoid.booleanOrMonoid.combine(false, false) shouldBe false
    }

    "use XOR monoid with identity false" in {
      Monoid.booleanXorMonoid.combine(true, false) shouldBe true
      Monoid.booleanXorMonoid.combine(false, true) shouldBe true
      Monoid.booleanXorMonoid.combine(true, true) shouldBe false
      Monoid.booleanXorMonoid.combine(false, false) shouldBe false
    }

    "use XNOR monoid with identity true" in {
      Monoid.booleanXNorMonoid.combine(true, false) shouldBe false
      Monoid.booleanXNorMonoid.combine(false, true) shouldBe false
      Monoid.booleanXNorMonoid.combine(true, true) shouldBe true
      Monoid.booleanXNorMonoid.combine(false, false) shouldBe true
    }
  }

  "2.4 All Set For Monoids" should {
    "use union monoid for sets" in {
      Monoid.setUnionMonoid.combine(Set(1,2,3), Set(3,4,5)) shouldBe Set(1,2,3,4,5)
      Monoid.setUnionMonoid.combine(Set("A","B"), Set("C", "D")) shouldBe Set("A","B","C","D")
    }

    "use intersection semigroup for sets" in {
      Monoid.setIntersectionMonoid.combine(Set(1,2,3), Set(3,4,5)) shouldBe Set(3)
      Monoid.setIntersectionMonoid.combine(Set("A","B"), Set("C", "D")) shouldBe Set.empty
      Monoid.setIntersectionMonoid.combine(Set("A","B","C"), Set("C", "D")) shouldBe Set("C")
    }

    "use symmetric difference monoid (union less the intersection) for sets" in {
      Monoid.setSymmetricDifferenceMonoid.combine(Set(1,2,3), Set(3,4,5)) shouldBe Set(1,2,4,5)
      Monoid.setSymmetricDifferenceMonoid.combine(Set("A","B"), Set("C", "D")) shouldBe Set("A","B","C","D")
      Monoid.setSymmetricDifferenceMonoid.combine(Set("A","B","C"), Set("C", "D")) shouldBe Set("A","B","D")
    }
  }

  "2.5.4 Adding All The Things" should {
    "add Integers using cats " in {
      import cats.instances.int._ // for Monoid[Int]
      import cats.syntax.semigroup._ // for |+|

      def add(items: List[Int]): Int =
        items.foldLeft(cats.Monoid[Int].empty)(_ |+| _)

      add(List(1,2,3)) shouldBe 6
    }

    "add Options of Integers using cats" in {
      import cats.instances.option._ // for Monoid[Option]
      import cats.instances.int._ // for Monoid[Int]
      import cats.syntax.semigroup._ // for |+|

      def add[T : cats.Monoid](items: List[T]): T =
        items.foldLeft(cats.Monoid[T].empty)(_ |+| _)

      add(List(1,2,3)) shouldBe 6
      add(List(Some(1), None, Some(4), None, Some(9))) shouldBe Some(14)
    }

    "add Orders using cats" in {
      import cats.syntax.semigroup._ // for |+|

      case class Order(totalCost: Double, quantity: Double)

      def add[T : cats.Monoid](items: List[T]): T =
        items.foldLeft(cats.Monoid[T].empty)(_ |+| _)

      implicit val orderM: cats.Monoid[Order] = new cats.Monoid[Order]{
        def empty = Order(totalCost = 0, quantity = 0)
        def combine(a: Order, b: Order) = Order(
          a.totalCost + b.totalCost,
          a.quantity + b.quantity
        )
      }

      val orders = List(Order(totalCost = 1.50, quantity = 4), Order(totalCost = 4.55, quantity = 2.5))
      add(orders) shouldBe Order(totalCost = 6.05, quantity = 6.5)
    }
  }

}
