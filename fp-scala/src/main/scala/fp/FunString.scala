// functional string manipulation
object FunString {

  // replace 'bar' with 'baz' in a source String
  def `s/bar/baz/g`(xs: List[Char]): List[Char] = xs match {
    case ys if ys.size < 3 => ys
    case 'b' :: 'a' :: 'r' :: tail => 'b' :: 'a' :: 'z' :: `s/bar/baz/g`(tail)
    case y :: ys => y :: `s/bar/baz/g`(ys)
  }
}
