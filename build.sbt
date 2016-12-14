name := "sbt-scaffold-play"

organization := "com.github.dnvriend"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.6"

sbtPlugin := true

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.7"
libraryDependencies += "com.typesafe.play" %% "twirl-api" % "1.1.1"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.0"
libraryDependencies += "com.typesafe.play" %% "play-jdbc" % "2.4.8"
libraryDependencies += "com.typesafe.play" %% "play-jdbc-api" % "2.4.8"
libraryDependencies += "com.h2database" % "h2" % "1.4.193"

// testing
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.1" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

// enable publishing to jcenter
homepage := Some(url("https://github.com/dnvriend/sbt-scaffold-play"))

publishMavenStyle := false

sbtPlugin := true

bintrayRepository := "sbt-plugins"

bintrayReleaseOnPublish := true

enablePlugins(AutomateHeaderPlugin, SbtScalariform, BintrayPlugin, BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoOptions += BuildInfoOption.ToMap

buildInfoOptions += BuildInfoOption.ToJson

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoPackage := "com.github.dnvriend.scaffold.play"