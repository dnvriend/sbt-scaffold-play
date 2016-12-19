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

package com.github.dnvriend.scaffold.play.enabler.conductr

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz.Disjunction

object ConductrEnablerResult extends PathFormat {
  implicit val format: Format[ConductrEnablerResult] = Json.format[ConductrEnablerResult]
}

final case class ConductrEnablerResult(settings: Path, plugin: Path) extends EnablerResult

object ConductrEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings(ctx.projectName))
    plugin <- createPlugin(ctx.baseDir, Template.plugin(ctx))
  } yield ConductrEnablerResult(settings, plugin)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-conductr.sbt", content)

  def createPlugin(baseDir: Path, content: String): Disjunction[String, Path] = {
    FileUtils.writeFile(baseDir / "project" / "plugin-conductr.sbt", content)
  }
}

object Template {
  def settings(projectName: String): String =
    s"""
       |enablePlugins(PlayBundlePlugin)
       |
       |BundleKeys.endpoints := Map(
       |  "play" -> Endpoint(bindProtocol = "http", bindPort = 0, services = Set(URI("http://:9000/$projectName"))),
       |  "akka-remote" -> Endpoint("tcp")
       |)
       |
       |normalizedName in Bundle := "$projectName"
       |
       |BundleKeys.system := "play"
       |
       |BundleKeys.startCommand += "-Dhttp.address=$$PLAY_BIND_IP -Dhttp.port=$$PLAY_BIND_PORT -Dplay.akka.actor-system=$$BUNDLE_SYSTEM"
    """.stripMargin

  def plugin(ctx: EnablerContext): String =
    s"""
      |addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "${ctx.conductrVersion}")
    """.stripMargin
}
