# Scoped sbt-assembly

_30 May 2012_

[sbt-assembly](https://github.com/sbt/sbt-assembly) is a handy sbt plugin which approximates the behavior of the [Maven Assembly Plugin](http://maven.apache.org/plugins/maven-assembly-plugin/). By default, sbt-assembly extracts and bundles every project dependency into the final package, however it can be useful to limit dependencies to those in a certain scope. This is easily achieved by overriding the `assemblyPath` method:

```scala
class MyProject(info: ProjectInfo) extends DefaultProject(info) with assembly.AssemblyBuilder {
  override def assemblyClasspath = fullClasspath(config("compile"))
  // etc.
}
```

This will include only dependencies in the compile scope and exclude those of other scopes (provided, test, and so on). 
