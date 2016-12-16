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

package com.github.dnvriend.scaffold.play.enabler.scalariform

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class ScalariformEnablerResult(settings: Path, plugin: Path) extends EnablerResult

class ScalariformEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
    plugin <- createPlugin(ctx.baseDir, Template.plugin())
  } yield ScalariformEnablerResult(settings, plugin)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-scalariform.sbt", content)

  def createPlugin(baseDir: Path, content: String): Disjunction[String, Path] = {
    FileUtils.writeFile(baseDir / "project" / "plugin-scalariform.sbt", content)
  }
}

object Template {
  def settings(): String =
    """
      |import scalariform.formatter.preferences._
      |import com.typesafe.sbt.SbtScalariform
      |
      |SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
      |  .setPreference(AlignSingleLineCaseStatements, true)
      |  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      |  .setPreference(DoubleIndentClassDeclaration, true)
    """.stripMargin

  def plugin(): String =
    """
      |addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
    """.stripMargin
}
