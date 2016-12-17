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

package com.github.dnvriend.scaffold.play.scaffolds.dto

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.ProductUserInput
import com.github.dnvriend.scaffold.play.util.ScaffoldDisjunction._
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz._

final case class DtoScaffoldResult(input: ProductUserInput, content: String, createdClass: Path) extends ScaffoldResult

class DtoScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    input <- ProductUserInput.askUser(ctx.organization + ".dto", "DefaultDto")
    content <- generateContent(input.packageName, input.className, input.render)
    createdClass <- create(ctx.srcDir, input.packageName, input.className, content)
  } yield DtoScaffoldResult(input, content, createdClass)

  def generateContent(packageName: String, className: String, renderedClass: String): Disjunction[String, String] =
    Disjunction.fromTryCatchNonFatal(Template.render(packageName, className, renderedClass))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)
}

object Template {
  def render(packageName: String, className: String, renderedClass: String): String =
    s"""package $packageName
       |
       |import play.api.libs.json._
       |
       |object $className {
       | implicit val format: Format[$className] = Json.format[$className]
       |}
       |
       |case class $renderedClass
  """.stripMargin
}