
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.2.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.13.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addDependencyTreePlugin

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"