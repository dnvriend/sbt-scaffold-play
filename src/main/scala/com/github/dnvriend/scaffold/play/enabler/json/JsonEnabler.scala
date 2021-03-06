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

package com.github.dnvriend.scaffold.play.enabler.json

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz._
import Scalaz._

object JsonEnablerResult extends PathFormat {
  implicit val format: Format[JsonEnablerResult] = Json.format[JsonEnablerResult]
}

final case class JsonEnablerResult(settings: Path) extends EnablerResult

object JsonEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    settings <- createSettings(ctx.baseDir, Template.settings(ctx))
  } yield JsonEnablerResult(settings)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: JsonEnablerResult => "Json libraries already enabled".left[Unit]
    case _                    => ().right[String]
  }.sequenceU

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-json.sbt", content)
}

object Template {
  def settings(ctx: EnablerContext): String =
    s"""
       |libraryDependencies += ws
       |libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "${ctx.nscalaTimeVersion}"
       |libraryDependencies += "com.typesafe.play" %% "play-json" % "${ctx.playJsonVersion}"
    """.stripMargin
}
