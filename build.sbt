

val V = new {
	val Scala = "2.13.6"

	val spark = "2.9.4-JAKON"
	val log4j = "2.14.1"
	val laminar = "0.13.1"
	val http4s = "0.23.4"
	val sttp = "3.3.13"
	val circe = "0.14.1"
	val decline = "2.1.0"
	val weaver = "0.7.6"
	val doobieVersion = "1.0.0-RC1"
	val lucene = "7.5.0"
}

scalaVersion := V.Scala
name := "jakon"
version := "0.4-SNAPSHOT"


ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += "Artifactory" at "https://kamenitxans-maven-repository.appspot.com/"


val Dependencies = new {


	lazy val frontend = Seq(

	)

	lazy val backend = Seq(
		libraryDependencies ++=
			Seq(
				"com.sparkjava" % "spark-core" % V.spark,
				"com.sparkjava" % "spark-template-pebble" % "2.7.1-jakon.1",
				"org.slf4j" % "slf4j-api" % "1.7.32",
				"org.apache.logging.log4j" % "log4j-api" % V.log4j,
				"org.apache.logging.log4j" % "log4j-core" % V.log4j,
				"org.apache.logging.log4j" % "log4j-slf4j-impl" % V.log4j,
				"org.xerial" % "sqlite-jdbc" % "3.36.0.1",
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
				"com.google.code.gson" % "gson" % "2.8.7",
				"org.apache.lucene" % "lucene-core" % V.lucene,
				"org.apache.lucene" % "lucene-queryparser" % V.lucene,
				"io.github.classgraph" % "classgraph" % "4.8.114",
				"commons-codec" % "commons-codec" % "1.11",
				"com.zaxxer" % "HikariCP" % "3.1.0",
				"com.github.scribejava" % "scribejava-apis" % "6.5.1",
				"cz.etn" % "email-validator" % "1.1.2",
				"org.jetbrains" % "annotations" % "22.0.0",
				"com.lihaoyi" %% "sourcecode" % "0.2.7",
				"org.scalatest" %% "scalatest" % "3.1.1" % "test",
				"org.seleniumhq.selenium" % "htmlunit-driver" % "2.52.0" % "test"
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
		Dependencies.tests,
		Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
	)
	.settings(
		commonBuildSettings,
		name := "fluvii-fe"
	)

lazy val backend = (project in file("modules/backend"))
	.dependsOn(shared.jvm)
	.settings(Dependencies.backend)
	.settings(Dependencies.tests)
	.settings(commonBuildSettings)
	.enablePlugins(JavaAppPackaging)
	.enablePlugins(DockerPlugin)
	.settings(
		name := "fluvii",
		Test / fork := true,
		Universal / mappings += {
			val appJs = (frontend / Compile / fullOptJS).value.data
			appJs -> ("lib/prod.js")
		},
		Universal / javaOptions ++= Seq(
			"--port 8080",
			"--mode prod"
		),
		Docker / packageName := "laminar-http4s-example"
	)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
	.crossType(CrossType.Pure)
	.in(file("modules/shared"))
	.jvmSettings(Dependencies.shared)
	.jsSettings(Dependencies.shared)
	.jsSettings(commonBuildSettings)
	.jvmSettings(commonBuildSettings)
	.settings(
		name := "fluvii-shared"
	)

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / semanticdbEnabled := false
ThisBuild / scalacOptions += "-deprecation"

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
