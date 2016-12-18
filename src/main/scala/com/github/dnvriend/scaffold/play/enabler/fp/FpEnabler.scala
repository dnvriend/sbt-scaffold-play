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

package com.github.dnvriend.scaffold.play.enabler.fp

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, UserInput }
import sbt.complete.DefaultParsers

import scalaz.Disjunction

final case class FpEnablerResult(settings: Path) extends EnablerResult

object FpEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
  } yield FpEnablerResult(settings)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-fp.sbt", content)
}

object Template {
  def settings(): String =
    s"""
       |libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.7"
       |libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"
       |libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.0" % Test
    """.stripMargin
}
