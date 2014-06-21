package logger

import org.apache.log4j.Logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Aspect

@Aspect
class MethodLogger {

  private var counts: Map[String,Int] = Map.empty
  private def count(key: String): Int =
    synchronized {
      val count = counts.getOrElse(key, 0) + 1
      counts = counts + (key -> count)
      count
    }

  @Around("execution(* example..*.*(..))")
  def timeMethod(joinPoint: ProceedingJoinPoint): Any = {
    val start = System.currentTimeMillis
    val retVal = joinPoint.proceed
    val time = System.currentTimeMillis - start

    val targetS = target(joinPoint)
    val signatureS = signature(joinPoint)
    val countI = count(targetS + signatureS)

    val buffer = new StringBuffer
    buffer.append(target(joinPoint))
    buffer.append(signature(joinPoint))
    buffer.append("(")
    buffer.append(args(joinPoint))
    buffer.append(")")
    buffer.append(" | execution time: ")
    buffer.append(time)
    buffer.append(" ms")
    buffer.append(" | count: ")
    buffer.append(countI)

    Logger.getLogger(getClass).info(buffer.toString)

    retVal
  }

  private def target(joinPoint: ProceedingJoinPoint): String =
    Option(joinPoint.getTarget) match {
      case Some(x) => x.getClass.getName + "."
      case None => ""
    }

  private def signature(joinPoint: ProceedingJoinPoint): String =
    Option(joinPoint.getSignature) match {
      case Some(x) => x.getName
      case None => ""
    }

  private def args(joinPoint: ProceedingJoinPoint): String =
    Option(joinPoint.getArgs) match {
      case Some(x) => show(x)
      case None => ""
    }

  private def show(x: Any): String =
    if (x.isInstanceOf[Array[_]])
      x.asInstanceOf[Array[_]].map(show).mkString(", ")
    else x.toString

}
