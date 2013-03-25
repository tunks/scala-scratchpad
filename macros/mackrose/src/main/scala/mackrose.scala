package com.earldouglas.mackrose

import language.experimental.macros
import scala.reflect.macros.Context

object `package` {
  def mackrose2[A](f: Function0[Unit], a: A): A = macro mackrose2_impl[A]
  def mackrose2_impl[A](c: Context)(f: c.Expr[Function0[Unit]], a: c.Expr[A]): c.Expr[A] =
    {
      val fun = c.eval(c.Expr[Function0[Unit]](c.resetAllAttrs(f.tree)))
      fun()
    c.universe.reify(a.splice)
  }
}
