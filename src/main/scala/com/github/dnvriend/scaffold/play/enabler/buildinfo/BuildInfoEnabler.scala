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
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class BuildInfoEnablerResult(settings: Path, plugin: Path) extends EnablerResult

class BuildInfoEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createBuildInfoSettings(ctx.baseDir, Template.settings())
    plugin <- createBuildInfoPlugin(ctx.baseDir, Template.plugin())
  } yield BuildInfoEnablerResult(settings, plugin)

  def createBuildInfoSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "buildinfo-settings.sbt", content)

  def createBuildInfoPlugin(baseDir: Path, content: String): Disjunction[String, Path] = {
    FileUtils.writeFile(baseDir / "project" / "buildinfo-plugin.sbt", content)
  }
}

object Template {
  def settings(): String =
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

  def plugin(): String =
    """
    |addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
  """.stripMargin
}