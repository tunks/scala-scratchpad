object StructuralVariance extends App {
  val javaVariance = new JavaVariance()

  inv(javaVariance)
  cov[Employee](javaVariance)
  cont[Engineer](javaVariance)

  def inv[A](invar:    { def invariant():     java.util.List[A] })(implicit m: scala.reflect.Manifest[A]) = println(invar.invariant())
  def cov[A](covar:    { def covariant():     java.util.List[_ <: A] }) = println(covar.covariant())
  def cont[A](contra:  { def contravariant(): java.util.List[_ >: A] }) = println(contra.contravariant())
}

