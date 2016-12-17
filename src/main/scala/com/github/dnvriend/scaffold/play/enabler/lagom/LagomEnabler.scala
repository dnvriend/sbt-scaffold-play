/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
