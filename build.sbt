import sbtassembly.AssemblyPlugin.autoImport.assembly

val V = new {
	val Scala = "3.2.2"
  val jakon = "0.5.2-SNAPSHOT"
	val spark = "2.9.4-JAKON.2"
	val log4j = "2.20.0"
	val circeVersion = "0.14.5"
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

	//noinspection SpellCheckingInspection
	lazy val backend = Seq(
		libraryDependencies ++=
			Seq(
				"com.sparkjava" % "spark-core" % V.spark,
				"com.sparkjava" % "spark-template-pebble" % "2.7.1-jakon.1",
				"org.slf4j" % "slf4j-api" % "2.0.7",
				"org.apache.logging.log4j" % "log4j-api" % V.log4j,
				"org.apache.logging.log4j" % "log4j-core" % V.log4j,
				"org.apache.logging.log4j" % "log4j-slf4j2-impl" % V.log4j,
				"org.xerial" % "sqlite-jdbc" % "3.40.1.0",
				"mysql" % "mysql-connector-java" % "8.0.32",
				"com.google.guava" % "guava" % "29.0-jre",
				"commons-io" % "commons-io" % "2.11.0",
				"org.apache.commons" % "commons-lang3" % "3.12.0",
				"commons-codec" % "commons-codec" % "1.15",
				"commons-fileupload" % "commons-fileupload" % "1.5",
				"de.svenkubiak" % "jBCrypt" % "0.4.3",
				"net.minidev" % "json-smart" % "2.4.10", // TODO remove
				"com.sun.mail" % "jakarta.mail" % "2.0.1",
				"com.atlassian.commonmark" % "commonmark" % "0.17.0",
				"com.google.code.gson" % "gson" % "2.10.1", // TODO remove
				"io.circe" %% "circe-core" % V.circeVersion,
				"io.circe" %% "circe-generic"% V.circeVersion,
				"io.circe" %% "circe-parser"% V.circeVersion,
				"org.typelevel" %% "shapeless3-deriving" % "3.3.0", // TODO remove
				//"org.apache.lucene" % "lucene-core" % "7.5.0",
				//"org.apache.lucene" % "lucene-queryparser" % "7.5.0",
				"io.github.classgraph" % "classgraph" % "4.8.157",
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
			"com.squareup.okhttp3" % "okhttp" % "4.10.0",
			"org.scalatest" %% "scalatest" % "3.2.15" % "test",
			"org.seleniumhq.selenium" % "htmlunit-driver" % "3.63.0" % "test"
		)
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
		publishMavenStyle :=true
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
addCommandAlias("jar", "clean; coverageOff; assembly")
addCommandAlias("githubTest", "coverageOn; coverage; test; coverageReport; coverageOff;")
