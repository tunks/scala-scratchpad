# Scala method logging with AspectJ

*June 21, 2014*

This example shows how to instrument existing Scala code with a method logging 
advice to collect basic statistics about method executions.

We start with some simple code that we want to analyze:

```scala
def add(a: Int, b: Int) = a + b

def slowAdd(a: Int, b: Int) = {
  Thread.sleep(100)
  a + b
}

add(20, 22)
add(41, 1)
slowAdd(52, -10)
```

Next we build the wrapper that will analyze it:

```scala
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
```

AspectJ needs to be configured via `META-INF/aop.xml` on the classpath:

```xml
<aspectj>

  <aspects>
    <aspect name="logger.MethodLogger"/>
  </aspects>

  <weaver options="-XnoInline">
    <include within="logger.*"/>
    <include within="example.*"/>
  </weaver>

</aspectj>
```

Finally, we tell sbt how to wire it up in *build.sbt*:

```scala
javaOptions += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.2.jar"

fork := true
```

When we run under sbt, we see some interesting output from log4j:

```
> run
[info] Running example.Main 
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.scala$App$_setter_$executionStart_$eq(1403382797609) | execution time: 0 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.scala$App$_setter_$scala$App$$initCode_$eq(ListBuffer()) | execution time: 0 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.scala$App$$initCode() | execution time: 0 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.delayedInit(<function0>) | execution time: 4 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.scala$App$$_args_$eq() | execution time: 0 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.scala$App$$initCode() | execution time: 0 ms | count: 2
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.add(20, 22) | execution time: 0 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.add(41, 1) | execution time: 1 ms | count: 2
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.slowAdd(52, -10) | execution time: 100 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - example.Main$.main() | execution time: 139 ms | count: 1
[info] 2014-06-21 13:33:17 INFO  MethodLogger:42 - main() | execution time: 494 ms | count: 1
```
