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

package com.github.dnvriend.scaffold.play.enabler.buildinfo

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz._
import Scalaz._

object BuildInfoEnablerResult extends PathFormat {
  implicit val format: Format[BuildInfoEnablerResult] = Json.format[BuildInfoEnablerResult]
}

final case class BuildInfoEnablerResult(settings: Path, plugin: Path) extends EnablerResult

object BuildInfoEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    settings <- createSettings(ctx.baseDir, Template.settings)
    plugin <- createPlugin(ctx.baseDir, Template.plugin(ctx))
  } yield BuildInfoEnablerResult(settings, plugin)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: BuildInfoEnablerResult => "BuildInfo already enabled".left[Unit]
    case _                         => ().right[String]
  }.sequenceU

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-buildinfo.sbt", content)

  def createPlugin(baseDir: Path, content: String): Disjunction[String, Path] = {
    FileUtils.writeFile(baseDir / "project" / "plugin-buildinfo.sbt", content)
  }
}

object Template {
  val settings: String =
    """
      |enablePlugins(BuildInfoPlugin)
      |
      |buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
      |
      |buildInfoOptions += BuildInfoOption.ToMap
      |
      |buildInfoOptions += BuildInfoOption.ToJson
      |
      |buildInfoOptions += BuildInfoOption.BuildTime
      |
      |buildInfoPackage := organization.value
    """.stripMargin

  def plugin(ctx: EnablerContext): String =
    s"""
    |addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "${ctx.buildInfoVersion}")
  """.stripMargin
}