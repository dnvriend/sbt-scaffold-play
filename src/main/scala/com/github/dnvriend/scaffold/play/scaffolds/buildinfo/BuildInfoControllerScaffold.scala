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

package com.github.dnvriend.scaffold.play.scaffolds.buildinfo

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.ComponentNameUserInput
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class BuildInfoControllerScaffoldResult(componentName: String, componentPackage: String, createdController: Path, alteredRoutes: Path) extends ScaffoldResult

class BuildInfoControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    componentName <- ComponentNameUserInput.askUser("buildinfo-controller", "buildinfo")
    componentPackage = s"${ctx.organization}.component.$componentName"
    createdController <- createController(ctx.srcDir, ctx.organization, componentPackage)
    alteredRoutes <- addRoutes(ctx.resourceDir, componentPackage)
  } yield BuildInfoControllerScaffoldResult(componentName, componentPackage, createdController, alteredRoutes)

  def createController(srcDir: Path, organization: String, componentPackage: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.controller", "BuildInfoController", Template.controller(organization, componentPackage))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)

  def addRoutes(resourceDir: Path, componentPackage: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, Template.routes(componentPackage))
}

object Template {
  def controller(organization: String, componentPackage: String): String =
    s"""package $componentPackage.controller
       |
       |import play.api.http.ContentTypes
       |import play.api.mvc.{ Action, Controller }
       |import org.slf4j.{ Logger, LoggerFactory }
       |import io.swagger.annotations._
       |
       |@Api(value = "/api/info")
       |class BuildInfoController extends Controller {
       |   val log: Logger = LoggerFactory.getLogger(this.getClass)
       |
       |   @ApiOperation(value = "Endpoint for BuildInfo", response = classOf[String], httpMethod = "GET")
       |   @ApiResponses(Array(new ApiResponse(code = 200, message = "BuildInfo")))
       |   def info = Action { request =>
       |     log.debug(s"Received buildinfo from $${request.remoteAddress}")
       |     Ok($organization.BuildInfo.toJson).as(ContentTypes.JSON)
       |   }
       |}
  """.stripMargin

  def routes(componentPackage: String): String = {
    val controller: String = s"$componentPackage.controller.BuildInfoController"
    s"""
       |GET /api/info $controller.info
    """.stripMargin
  }
}