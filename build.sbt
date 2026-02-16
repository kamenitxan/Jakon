import sbt.Tests.{Group, SubProcess}
import sbtassembly.AssemblyPlugin.autoImport.assembly

val V = new {
	val Scala = "3.3.7"
  val jakon = "0.7.2-SNAPSHOT"
	val log4j = "2.25.3"
	val circeVersion = "0.14.15"
	val javalin = "7.0.0-beta.2"
}

scalaVersion := V.Scala
organization := "cz.kamenitxan"
name := "jakon"
version := V.jakon


ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += "Artifactory" at "https://nexus.kamenitxan.eu/repository/maven-central/"
ThisBuild / resolvers += "Artifactory" at "https://nexus.kamenitxan.eu/repository/jakon/"


val Dependencies = new {

	lazy val frontend = Seq(
		libraryDependencies ++=
			Seq(
				"org.scala-js" %%% "scalajs-dom" % "2.8.0"
			)
	)

	//noinspection SpellCheckingInspection
	lazy val core = Seq(
		libraryDependencies ++=
			Seq(
				"io.javalin" % "javalin" % V.javalin,
				"io.javalin" % "javalin-rendering-pebble" % V.javalin,
				"org.slf4j" % "slf4j-api" % "2.0.17",
				"org.apache.logging.log4j" % "log4j-api" % V.log4j,
				"org.apache.logging.log4j" % "log4j-core" % V.log4j,
				"org.apache.logging.log4j" % "log4j-slf4j2-impl" % V.log4j,
				"org.xerial" % "sqlite-jdbc" % "3.51.2.0",
				"com.mysql" % "mysql-connector-j" % "9.6.0" % Optional,
				"com.google.guava" % "guava" % "33.5.0-jre",
				"commons-io" % "commons-io" % "2.21.0",
				"org.apache.commons" % "commons-lang3" % "3.20.0",
				"commons-codec" % "commons-codec" % "1.21.0",
				"de.svenkubiak" % "jBCrypt" % "0.4.3",
				"com.sun.mail" % "jakarta.mail" % "2.0.2",
				"org.commonmark" % "commonmark" % "0.27.1",
				"org.commonmark" % "commonmark-ext-gfm-tables" % "0.27.1",
				"org.commonmark" % "commonmark-ext-gfm-strikethrough" % "0.27.1",
				"com.google.code.gson" % "gson" % "2.13.2",
				"io.circe" %% "circe-core" % V.circeVersion,
				"io.circe" %% "circe-generic"% V.circeVersion,
				"io.circe" %% "circe-parser"% V.circeVersion,
				//"org.apache.lucene" % "lucene-core" % "7.5.0",
				//"org.apache.lucene" % "lucene-queryparser" % "7.5.0",
				"io.github.classgraph" % "classgraph" % "4.8.184",
				"com.zaxxer" % "HikariCP" % "7.0.2",
				"com.github.scribejava" % "scribejava-apis" % "8.3.3",
				"cz.etn" % "email-validator" % "1.4.0",
				"com.lihaoyi" %% "sourcecode" % "0.4.4"
			)
	)

	//noinspection SpellCheckingInspection
	lazy val tests = Def.settings(
		libraryDependencies ++= Seq(
			"org.scalatest" %% "scalatest" % "3.2.19" % "test",
			"org.scalamock" %% "scalamock" % "7.5.5" % Test,
			"org.seleniumhq.selenium" % "htmlunit3-driver" % "4.40.0" % "test"
		)
	)
}

lazy val root = (project in file(".")).aggregate(frontend, core)

lazy val frontend = (project in file("modules/frontend"))
	.enablePlugins(ScalaJSPlugin)
	.settings(scalaJSUseMainModuleInitializer := false)
	.settings(
		Dependencies.frontend,
		Dependencies.tests//,
		//Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
	)
	.settings(
		commonBuildSettings,
		name := "jakon-fe"
		//scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
	)

lazy val core = (project in file("modules/core"))
	.settings(Dependencies.core)
	.settings(Dependencies.tests)
	.settings(commonBuildSettings)
	.settings(
		name := "jakon",
		Test / fork := true,
		Test / testGrouping := (Test / definedTests).value.map { suite =>
			Group(suite.name, Seq(suite), SubProcess(ForkOptions().withWorkingDirectory(new File("modules/core"))))
		},
		ThisBuild / versionScheme := Some ("strict"),
		publishTo := Some ("Nexus" at "https://nexus.kamenitxan.eu/repository/jakon/"),
		credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
		publishMavenStyle :=true,
		scalacOptions ++= Seq(
			"-deprecation", // emit warning and location for usages of deprecated APIs
			//"-explain", // explain errors in more detail
			"-explain-types", // explain type errors in more detail
			"-feature", // emit warning and location for usages of features that should be imported explicitly
			"-no-indent", // do not allow significant indentation.
			"-print-lines", // show source code line numbers.
			"-unchecked", // enable additional warnings where generated code depends on assumptions
			//"-Xfatal-warnings", // fail the compilation if there are any warnings
			//"-Yexplicit-nulls"
		)
	)


ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / semanticdbEnabled := false
ThisBuild / assembly / assemblyMergeStrategy := {
	case PathList("module-info.class") => MergeStrategy.discard
	case x if x.endsWith("module-info.class") => MergeStrategy.discard
	case x =>
		val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
		oldStrategy(x)
}
Test / fork := true
Test / testForkedParallel := false
Test / logBuffered := false

lazy val fastOptCompileCopy = taskKey[Unit]("")

val jsPath = "modules/core/src/main/resources/static/jakon/js"
fastOptCompileCopy := {
	val source = (frontend / Compile / fastOptJS).value.data
	IO.copyFile(
		source,
		baseDirectory.value / jsPath / "scalajs.js"
	)
}

lazy val fullOptCompileCopy = taskKey[Unit]("")

fullOptCompileCopy := {
	val source = (frontend / Compile / fullOptJS).value.data
	IO.copyFile(
		source,
		baseDirectory.value / jsPath / "scalajs.js"
	)
	IO.copyFile(
		new File(source.getAbsolutePath + ".map"),
		baseDirectory.value / jsPath / "jakon-fe-opt.js.map"
	)
}

lazy val commonBuildSettings: Seq[Def.Setting[?]] = Seq(
	scalaVersion := V.Scala,
	organization := "cz.kamenitxan",
	name := "jakon",
	version := V.jakon,
	startYear := Some(2015)
)

addCommandAlias("runDev", ";fastOptCompileCopy; core/reStart --mode dev")
addCommandAlias("runProd", ";fullOptCompileCopy; core/reStart --mode prod")

val scalafixRules = Seq(
	"OrganizeImports",
	"DisableSyntax",
	"LeakingImplicitClassVal",
	"NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
	"clean",
	"core/compile",
	"core/test",
	"frontend/compile",
	"frontend/fastOptJS",
	"frontend/test",
	s"scalafix --check $scalafixRules"
).mkString(";")

val PrepareCICommands = Seq(
	s"scalafix $scalafixRules"
).mkString(";")

addCommandAlias("ci", CICommands)
addCommandAlias("preCI", PrepareCICommands)
addCommandAlias("jar", "clean; fullOptCompileCopy; coverageOff; assembly")
addCommandAlias("testJar", "clean; coverageOff; fullOptCompileCopy; set assembly / mainClass := Some(\"cz.kamenitxan.jakon.Main\"); assembly")
addCommandAlias("githubTest", "coverageOn; coverage; core/test; coverageReport; coverageOff;")
addCommandAlias("outdated", "dependencyUpdates")
