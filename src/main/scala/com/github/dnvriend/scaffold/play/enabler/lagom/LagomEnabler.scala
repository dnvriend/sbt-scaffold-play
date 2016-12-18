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

import com.github.dnvriend.scaffold.play.enabler.EnablerContext

// see: https://github.com/lagom/lagom/tree/1.3.0-M1
object LagomEnabler {

}

object Template {
  def settings(ctx: EnablerContext): String =
    s"""
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-api" % "${ctx.lagomVersion}"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-logback" % "${ctx.lagomVersion}"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-persistence-cassandra" % "${ctx.lagomVersion}"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-reloadable-server" % "${ctx.lagomVersion}"
       |libraryDependencies += "com.lightbend.lagom" %% "lagom-scaladsl-testkit" % "${ctx.lagomVersion}" % Test
    """.stripMargin
}
