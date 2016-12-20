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

package com.github.dnvriend.scaffold.play.enabler.sbtheader

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat, UserInput }
import play.api.libs.json.{ Format, Json }
import sbt.complete.DefaultParsers

import scalaz._
import Scalaz._

object SbtHeaderEnablerResult extends PathFormat {
  implicit val format: Format[SbtHeaderEnablerResult] = Json.format[SbtHeaderEnablerResult]
}

final case class SbtHeaderEnablerResult(settings: Path, plugin: Path) extends EnablerResult

object SbtHeaderEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    authorName <- UserInput.readLine(DefaultParsers.any.*.map(_.mkString).examples(ctx.organization), "[sbt-header]: Enter your name > ")
    settings <- createSettings(ctx.baseDir, Template.settings(authorName))
    plugin <- createPlugin(ctx.baseDir, Template.plugin(ctx))
  } yield SbtHeaderEnablerResult(settings, plugin)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: SbtHeaderEnablerResult => "SbtHeader plugin already enabled".left[Unit]
    case _                         => ().right[String]
  }.sequenceU

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-sbt-header.sbt", content)

  def createPlugin(baseDir: Path, content: String): Disjunction[String, Path] = {
    FileUtils.writeFile(baseDir / "project" / "plugin-sbt-header.sbt", content)
  }
}

object Template {
  def settings(authorName: String): String =
    s"""
      |enablePlugins(AutomateHeaderPlugin)
      |
      |licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))
      |
      |import de.heikoseeberger.sbtheader.license.Apache2_0
      |
      |headers := Map(
      |  "scala" -> Apache2_0("2016", "$authorName"),
      |  "conf" -> Apache2_0("2016", "$authorName", "#")
      |)
    """.stripMargin

  def plugin(ctx: EnablerContext): String =
    s"""
      |addSbtPlugin("de.heikoseeberger" % "sbt-header" % "${ctx.sbtHeaderVersion}")
    """.stripMargin
}
