package day0

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec


class _01_AdHocPolymorphismSpec extends AsyncWordSpec with Matchers {

  trait Monoid[A] {
    def mappend(a: A, b: A): A
    def mzero: A
  }
}
