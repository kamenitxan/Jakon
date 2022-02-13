import sbtassembly.AssemblyPlugin.autoImport.assembly

val V = new {
	val Scala = "2.13.7"
  val jakon = "0.4-SNAPSHOT"
	val spark = "2.9.4-JAKON"
	val log4j = "2.17.0"
	val circe = "0.14.1"
	val lucene = "7.5.0"
}

scalaVersion := V.Scala
organization := "cz.kamenitxan"
name := "jakon"
version := V.jakon


ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += "Artifactory" at "https://kamenitxans-maven-repository.appspot.com/"


val Dependencies = new {

	lazy val frontend = Seq(

	)

	lazy val backend = Seq(
		libraryDependencies ++=
			Seq(
				"com.sparkjava" % "spark-core" % V.spark,
				"org.scala-lang" % "scala-reflect" % V.Scala,
				"com.sparkjava" % "spark-template-pebble" % "2.7.1-jakon.1",
				"org.slf4j" % "slf4j-api" % "1.8.0-beta4",
				"org.apache.logging.log4j" % "log4j-api" % V.log4j,
				"org.apache.logging.log4j" % "log4j-core" % V.log4j,
				"org.apache.logging.log4j" % "log4j-slf4j18-impl" % V.log4j,
				"org.xerial" % "sqlite-jdbc" % "3.36.0.2",
				"mysql" % "mysql-connector-java" % "8.0.25",
				"com.google.guava" % "guava" % "29.0-jre",
				"javax.persistence" % "javax.persistence-api" % "2.2",
				"commons-io" % "commons-io" % "2.11.0",
				"org.apache.commons" % "commons-lang3" % "3.12.0",
				"de.svenkubiak" % "jBCrypt" % "0.4.3",
				"commons-fileupload" % "commons-fileupload" % "1.4",
				"net.minidev" % "json-smart" % "2.4.7",
				"com.sun.mail" % "javax.mail" % "1.6.2",
				"com.atlassian.commonmark" % "commonmark" % "0.11.0",
				"com.google.code.gson" % "gson" % "2.9.0",
				"org.apache.lucene" % "lucene-core" % V.lucene,
				"org.apache.lucene" % "lucene-queryparser" % V.lucene,
				"io.github.classgraph" % "classgraph" % "4.8.138",
				"commons-codec" % "commons-codec" % "1.15",
				"com.zaxxer" % "HikariCP" % "5.0.0",
				"com.github.scribejava" % "scribejava-apis" % "6.5.1",
				"cz.etn" % "email-validator" % "1.3.0",
				"org.jetbrains" % "annotations" % "22.0.0",
				"com.lihaoyi" %% "sourcecode" % "0.2.8",
				"org.scalatest" %% "scalatest" % "3.2.9" % "test",
				"org.seleniumhq.selenium" % "htmlunit-driver" % "2.55.0" % "test"
			)
	)

	lazy val shared = Def.settings(
		libraryDependencies += "io.circe" %%% "circe-core" % V.circe,
		libraryDependencies += "io.circe" %%% "circe-generic" % V.circe
	)

	lazy val tests = Def.settings(
	)
}

lazy val root =
	(project in file(".")).aggregate(frontend, backend, shared.js, shared.jvm)

lazy val frontend = (project in file("modules/frontend"))
	.dependsOn(shared.js)
	.enablePlugins(ScalaJSPlugin)
	.settings(scalaJSUseMainModuleInitializer := true)
	.settings(
		Dependencies.frontend,
		Dependencies.tests//,
		//Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
	)
	.settings(
		commonBuildSettings,
		name := "jakon-fe"
	)

lazy val backend = (project in file("modules/backend"))
	.dependsOn(shared.jvm)
	.settings(Dependencies.backend)
	.settings(Dependencies.tests)
	.settings(commonBuildSettings)
	.enablePlugins(JavaAppPackaging)
	.enablePlugins(DockerPlugin)
	.settings(
		name := "jakon",
		Test / fork := true,
		Universal / mappings += {
			val appJs = (frontend / Compile / fullOptJS).value.data
			appJs -> ("lib/prod.js")
		},
		Universal / javaOptions ++= Seq(
			"--port 8080",
			"--mode prod"
		),
		Docker / packageName := "jakon-example"
	)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
	.crossType(CrossType.Pure)
	.in(file("modules/shared"))
	.jvmSettings(Dependencies.shared)
	.jsSettings(Dependencies.shared)
	.jsSettings(commonBuildSettings)
	.jvmSettings(commonBuildSettings)
	.settings(
		name := "jakon-shared"
	)

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / semanticdbEnabled := false
ThisBuild / scalacOptions += "-deprecation"
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

val jsPath = "modules/backend/src/main/resources"

fastOptCompileCopy := {
	val source = (frontend / Compile / fastOptJS).value.data
	IO.copyFile(
		source,
		baseDirectory.value / jsPath / "dev.js"
	)
}

lazy val fullOptCompileCopy = taskKey[Unit]("")

fullOptCompileCopy := {
	val source = (frontend / Compile / fullOptJS).value.data
	IO.copyFile(
		source,
		baseDirectory.value / jsPath / "prod.js"
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
