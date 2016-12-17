package com.github.dnvriend.scaffold.play.enabler.lagom

class LagomEnabler {

}

object Template {
  def settings(): String =
    s"""
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-api" % "1.3.0-M1"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-logback" % "1.3.0-M1"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-persistence-cassandra" % "1.3.0-M1"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-reloadable-server" % "1.3.0-M1"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-testkit" % "1.3.0-M1" % Test
    """.stripMargin
}