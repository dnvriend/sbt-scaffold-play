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

package com.github.dnvriend.scaffold.play.scaffolds.pingcontroller

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.ComponentNameUserInput
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class PingControllerScaffoldResult(componentName: String, componentPackage: String, createdController: Path, alteredRoutes: Path) extends ScaffoldResult

class PingControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    componentName <- ComponentNameUserInput.askUser("ping-controller", "ping")
    componentPackage = s"${ctx.organization}.component.$componentName"
    createdController <- createController(ctx.srcDir, componentPackage)
    alteredRoutes <- addRoutes(ctx.resourceDir, componentPackage)
  } yield PingControllerScaffoldResult(componentName, componentPackage, createdController, alteredRoutes)

  def addRoute(resourceDir: Path, packageName: String, className: String, resourceName: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, s"GET $resourceName $packageName.$className.ping")

  def createController(srcDir: Path, componentPackage: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.controller", "PingController", Template.controller(componentPackage))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)

  def addRoutes(resourceDir: Path, componentPackage: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, Template.routes(componentPackage))
}

object Template {
  def controller(componentPackage: String): String =
    s"""package $componentPackage.controller
       |
       |import play.api.mvc.{ Action, Controller }
       |import org.slf4j.{ Logger, LoggerFactory }
       |import io.swagger.annotations._
       |
       |@Api(value = "/api/ping")
       |class PingController extends Controller {
       |   val log: Logger = LoggerFactory.getLogger(this.getClass)
       |
       |   @ApiOperation(value = "Endpoint for ping", response = classOf[String], httpMethod = "GET")
       |   @ApiResponses(Array(new ApiResponse(code = 200, message = "pong")))
       |   def ping = Action { request =>
       |     log.debug(s"Received ping from $${request.remoteAddress}")
       |     Ok("pong")
       |   }
       |}
  """.stripMargin

  def routes(componentPackage: String): String = {
    val controller: String = s"$componentPackage.controller.PingController"
    s"""
       |GET /api/ping $controller.ping
    """.stripMargin
  }
}