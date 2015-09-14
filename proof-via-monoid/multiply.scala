import scala.language.implicitConversions // eyeroll
import scala.language.higherKinds

import org.scalacheck.Prop.forAll
import org.scalacheck.Arbitrary

object Main0 extends App {

  def multiply(x: Int, y: Int): Int = 42

  assert(multiply(6, 7) == 42) // OK

  assert(multiply(2, 2) == 4)

/*
java.lang.AssertionError: assertion failed
*/

}

object Main1 extends App {

  def multiply(x: Int, y: Int): Int = 42

  val propMultiplyInt2 =
    forAll { (x: Int, y: Int) =>
      multiply(x, y) == x * y
    }
  propMultiplyInt2.check

/*
! Falsified after 0 passed tests.
> ARG_0: -2147483648
> ARG_1: 0
> ARG_1_ORIGINAL: -337495977
*/

}

object Main2 extends App {

  def multiply(x: Int, y: Int): Int = 42

  val propAssociative =
    forAll { (x: Int, y: Int, z: Int) =>
      multiply(x, multiply(y, z)) == multiply(multiply(x, y), z)
    }
  propAssociative.check

/*
[info] Running Main2 
+ OK, passed 100 tests.
*/

}

object Main3 extends App {

  def multiply(x: Int, y: Int): Int = 42

  val propLeftIdentity =
    forAll { (x: Int) =>
      multiply(1, x) == x
    }
  propLeftIdentity.check

/*
! Falsified after 0 passed tests.
> ARG_0: 0
> ARG_0_ORIGINAL: 1359761612
*/

  println()

  val propRightIdentity =
    forAll { (x: Int) =>
      multiply(x, 1) == x
    }
  propRightIdentity.check

/*
! Falsified after 0 passed tests.
> ARG_0: 0
> ARG_0_ORIGINAL: 871290852
*/

}

// https://en.wikibooks.org/wiki/Haskell/Monoids#Monoid_laws
object Main4 extends App {

  trait Monoid[A] {
    def zero: A
    def append(x: A, y: => A): A
  }

  def propMonoid[A:Arbitrary:Monoid] = {
    val m = implicitly[Monoid[A]]
    forAll { (x: A, y: A, z: A) =>
      m.append(x, m.append(y, z)) == m.append(m.append(x, y), z)
    } ++ forAll { (x: A) =>
      m.append(m.zero, x) == x
    } ++ forAll { (x: A) =>
      m.append(x, m.zero) == x
    }
  }

{
  implicit val multiplyInt2: Monoid[Int] =
    new Monoid[Int] {
      def zero: Int = 1
      def append(x: Int, y: => Int): Int = 42
    }

  propMonoid[Int].check

/*
! Falsified after 0 passed tests.
> ARG_0: -2147483648
*/

}

{

  implicit val multiplyInt2: Monoid[Int] =
    new Monoid[Int] {
      def zero: Int = 1
      def append(x: Int, y: => Int): Int = x * y
    }

  propMonoid[Int].check

/*
+ OK, passed 100 tests.
*/

}

}
