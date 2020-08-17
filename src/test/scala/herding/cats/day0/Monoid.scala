package herding.cats.day0

trait Monoid[A] {
  def mappend(a: A, b: A): A
  def mzero: A
}

// Scala’s implicit resolution rules include the companion object
object Monoid {
  implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
    def mappend(a: Int, b: Int): Int = a + b
    def mzero: Int = 0
  }

  implicit val StringMonoid: Monoid[String] = new Monoid[String] {
    def mappend(a: String, b: String): String = a + b
    def mzero: String = ""
  }
}
