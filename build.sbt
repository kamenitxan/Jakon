import sbtassembly.AssemblyPlugin.autoImport.assembly

val V = new {
	val Scala = "3.3.1-RC4"
  val jakon = "0.5.8-SNAPSHOT"
	val spark = "2.9.4-jakon-3"
	val log4j = "2.20.0"
	val circeVersion = "0.14.6"
}

scalaVersion := V.Scala
organization := "cz.kamenitxan"
name := "jakon"
version := V.jakon


ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += "Artifactory" at "https://nexus.kamenitxan.eu/repository/jakon/"


val Dependencies = new {

	lazy val frontend = Seq(
		libraryDependencies ++=
			Seq(
				"org.scala-js" %%% "scalajs-dom" % "2.6.0"
			)
	)

	//noinspection SpellCheckingInspection
	lazy val backend = Seq(
		libraryDependencies ++=
			Seq(
				"com.intellisrc" % "spark-core" % V.spark,
				"com.sparkjava" % "spark-template-pebble" % "2.7.1-jakon.3",
				"org.slf4j" % "slf4j-api" % "2.0.9",
				"org.apache.logging.log4j" % "log4j-api" % V.log4j,
				"org.apache.logging.log4j" % "log4j-core" % V.log4j,
				"org.apache.logging.log4j" % "log4j-slf4j2-impl" % V.log4j,
				"org.xerial" % "sqlite-jdbc" % "3.42.0.0",
				"mysql" % "mysql-connector-java" % "8.0.33",
				"com.google.guava" % "guava" % "32.1.2-jre",
				"commons-io" % "commons-io" % "2.13.0",
				"org.apache.commons" % "commons-lang3" % "3.13.0",
				"commons-codec" % "commons-codec" % "1.16.0",
				"org.apache.commons" % "commons-fileupload2-jakarta" % "2.0.0-M1",
				"de.svenkubiak" % "jBCrypt" % "0.4.3",
				"net.minidev" % "json-smart" % "2.5.0", // TODO remove
				"com.sun.mail" % "jakarta.mail" % "2.0.1",
				"com.atlassian.commonmark" % "commonmark" % "0.17.0", // TODO https://mvnrepository.com/artifact/org.commonmark/commonmark
				"com.google.code.gson" % "gson" % "2.10.1", // TODO remove
				"io.circe" %% "circe-core" % V.circeVersion,
				"io.circe" %% "circe-generic"% V.circeVersion,
				"io.circe" %% "circe-parser"% V.circeVersion,
				//"org.apache.lucene" % "lucene-core" % "7.5.0",
				//"org.apache.lucene" % "lucene-queryparser" % "7.5.0",
				"io.github.classgraph" % "classgraph" % "4.8.162",
				"com.zaxxer" % "HikariCP" % "5.0.1",
				"com.github.scribejava" % "scribejava-apis" % "8.3.3",
				"cz.etn" % "email-validator" % "1.3.0" excludeAll(
					ExclusionRule(organization = "javax.mail", name = "javax.mail-api")
				),
				"com.lihaoyi" %% "sourcecode" % "0.3.0"
			)
	)

	lazy val shared = Def.settings(

	)

	//noinspection SpellCheckingInspection
	lazy val tests = Def.settings(
		libraryDependencies ++= Seq(
			//"dev.zio" %% "zio-http" % "3.0.0-RC2",
			"org.scalatest" %% "scalatest" % "3.2.17" % "test",
			"org.seleniumhq.selenium" % "htmlunit3-driver" % "4.12.0" % "test"
		)
	)
}

lazy val root = (project in file(".")).aggregate(frontend, backend, shared.js, shared.jvm)

lazy val frontend = (project in file("modules/frontend"))
	.dependsOn(shared.js)
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

lazy val backend = (project in file("modules/backend"))
	//.dependsOn(shared.jvm)
	.settings(Dependencies.backend)
	.settings(Dependencies.tests)
	.settings(commonBuildSettings)
	.settings(
		name := "jakon",
		Test / fork := true,
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

lazy val shared = crossProject(JSPlatform, JVMPlatform)
	.crossType(CrossType.Pure)
	.in(file("modules/shared"))
	.jvmSettings(Dependencies.shared)
	.jsSettings(Dependencies.shared)
	.jsSettings(commonBuildSettings)
	.jvmSettings(commonBuildSettings)
	.settings(
		name := "jakon-shared",

		ThisBuild / versionScheme := Some("strict"),
		publishTo := Some("Nexus" at "https://nexus.kamenitxan.eu/repository/jakon/"),
		credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
		publishMavenStyle := true
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

val jsPath = "modules/backend/src/main/resources/static/jakon/js"
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

lazy val commonBuildSettings: Seq[Def.Setting[_]] = Seq(
	scalaVersion := V.Scala,
	organization := "cz.kamenitxan",
	name := "jakon",
	version := V.jakon,
	startYear := Some(2015)
)

addCommandAlias("runDev", ";fastOptCompileCopy; backend/reStart --mode dev")
addCommandAlias("runProd", ";fullOptCompileCopy; backend/reStart --mode prod")

val scalafixRules = Seq(
	"OrganizeImports",
	"DisableSyntax",
	"LeakingImplicitClassVal",
	"NoValInForComprehension"
).mkString(" ")

val CICommands = Seq(
	"clean",
	"backend/compile",
	"backend/test",
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
addCommandAlias("githubTest", "coverageOn; coverage; test; coverageReport; coverageOff;")
