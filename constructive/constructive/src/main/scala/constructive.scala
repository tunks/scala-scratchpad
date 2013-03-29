package com.earldouglas.constructive

import language.experimental.macros
import scala.reflect.macros.Context

object `package` {

  def constructive1[A, B](f: A => B, p: (A, B)): A => B = macro constructive1_impl[A, B]

  def constructive1_impl[A, B](c: Context)(f: c.Expr[A => B], p: c.Expr[(A, B)]): c.Expr[A => B] =
    {
      val fe: c.Expr[A => B] = c.Expr[A => B](c.resetAllAttrs(f.tree))
      val pe: c.Expr[(A, B)] = c.Expr[(A, B)](c.resetAllAttrs(p.tree))

      val _f: A => B = c.eval(fe)
      val _p: (A, B)    = c.eval(pe)

      val b = _f(_p._1)
      if (b != _p._2) c.abort(c.enclosingPosition, "expected f(%s) = %s, but was %s".format(_p._1, _p._2, b))

      c.universe.reify(f.splice)
    }

}
