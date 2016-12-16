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

package com.github.dnvriend.scaffold.play.scaffolds.controller

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.ControllerUserInput
import com.github.dnvriend.scaffold.play.util.DisjunctionOps.DisjunctionOfThrowableToDisjunctionOfString
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz._

final case class ControllerScaffoldResult(input: ControllerUserInput, content: String, createdClass: Path) extends ScaffoldResult

class ControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    input <- ControllerUserInput.askUser(ctx.organization + ".controller", "DefaultController")
    content <- generateContent(input.packageName, input.className)
    _ <- addRoute(ctx.resourceDir, input.packageName, input.className, input.resourceName)
    createdClass <- create(ctx.srcDir, input.packageName, input.className, content)

  } yield ControllerScaffoldResult(input, content, createdClass)

  def addRoute(resourceDir: Path, packageName: String, className: String, resourceName: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, s"GET $resourceName $packageName.$className.action")

  def generateContent(packageName: String, className: String): Disjunction[String, String] =
    Disjunction.fromTryCatchNonFatal(Template.render(packageName, className))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)
}

object Template {
  def render(packageName: String, className: String): String =
    s"""package $packageName
    |
    |import javax.inject.Inject
    |import play.api.mvc.{ Action, Controller }
    |import org.slf4j.{ Logger, LoggerFactory }
    |
    |class $className @Inject() () extends Controller {
    |   val log: Logger = LoggerFactory.getLogger(this.getClass)
    |   def action = Action(Ok)
    |}
  """.stripMargin
}