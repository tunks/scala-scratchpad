object Main extends App {

  {
    trait Semigroup[A] {
      def append(a1: A, a2: A): A
    }
    val listSemigroup =
      new Semigroup[List[Int]] {
        def append(a1: List[Int], a2: List[Int]): List[Int] = a1 ++ a2
      }
    import listSemigroup._

    val list = append(List(1,2,3), List(4,5,6))
    println("append(List(1,2,3), List(4,5,6)) = " + list.toString)
  }

  {
    trait Semigroup[A] {
      def append(a1: A, a2: A): A
      class InfixSemigroup(a1: A) {
        def ⋅(a2: A): A = Semigroup.this.append(a1, a2)
      }
      implicit def infix(a1: A) = new InfixSemigroup(a1)
    }
    val listSemigroup =
      new Semigroup[List[Int]] {
        def append(a1: List[Int], a2: List[Int]): List[Int] = a1 ++ a2
      }
    import listSemigroup._

    val list = List(1,2,3) ⋅ List(4,5,6)
    println("List(1,2,3) ⋅ List(4,5,6) = " + list.toString)
  }

}
