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

package com.github.dnvriend.scaffold.play.scaffolds.health

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.{ ComponentNameUserInput, EntityUserInput }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class HealthControllerScaffoldResult(componentName: String, componentPackage: String, createdController: Path, createdDisjunctionOps: Path, alteredRoutes: Path) extends ScaffoldResult

class HealthControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    componentName <- ComponentNameUserInput.askUser("health-controller", "health")
    componentPackage = s"${ctx.organization}.component.$componentName"
    createdController <- createController(ctx.srcDir, ctx.organization, componentPackage)
    createdDisjunctionOps <- createDisjunctionOps(ctx.srcDir, componentPackage)
    alteredRoutes <- addRoutes(ctx.resourceDir, componentPackage)
  } yield HealthControllerScaffoldResult(componentName, componentPackage, createdController, createdDisjunctionOps, alteredRoutes)

  def createController(srcDir: Path, organization: String, componentPackage: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.controller", "HealthController", Template.controller(organization, componentPackage))

  def createDisjunctionOps(srcDir: Path, componentPackage: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.util", "DisjunctionOps", Template.disjunctionOps(componentPackage))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)

  def addRoutes(resourceDir: Path, componentPackage: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, Template.routes(componentPackage))
}

object Template {
  def controller(organization: String, componentPackage: String): String =
    s"""package $componentPackage.controller
       |
       |import javax.inject.Inject
       |
       |import anorm._
       |import akka.pattern.CircuitBreaker
       |import $componentPackage.util.DisjunctionOps._
       |import io.swagger.annotations._
       |import org.slf4j.{ Logger, LoggerFactory }
       |import play.api.db.Database
       |import play.api.mvc.{ Action, Controller }
       |
       |import scalaz._
       |
       |@Api(value = "/api/health")
       |class HealthController @Inject() (db: Database, cb: CircuitBreaker) extends Controller {
       |  val log: Logger = LoggerFactory.getLogger(this.getClass)
       |
       |  @ApiOperation(value = "Endpoint for health check", response = classOf[String], httpMethod = "GET")
       |  @ApiResponses(Array(new ApiResponse(code = 200, message = "")))
       |  def check = Action { request =>
       |    log.debug(s"Received health from $${request.remoteAddress}")
       |    cb.withSyncCircuitBreaker(checkDatabase)
       |  }
       |
       |  def checkDatabase: Disjunction[String, Long] = Disjunction.fromTryCatchNonFatal {
       |    db.withConnection { implicit conn =>
       |      SQL"SELECT 1".executeQuery().as(anorm.SqlParser.scalar[Long].single)
       |    }
       |  }.leftMap(_.toString)
       |}
  """.stripMargin

  def disjunctionOps(componentPackage: String): String =
    s"""
       |package $componentPackage.util
       |
       |import play.api.libs.json.{ Format, Json }
       |import play.api.mvc.{ Result, Results }
       |
       |import scala.language.implicitConversions
       |import scalaz._
       |
       |object DisjunctionOps extends Results {
       |  def tryCatch[A](block: => A): Disjunction[String, A] =
       |    Disjunction.fromTryCatchNonFatal(block).leftMap(_.toString)
       |
       |  implicit def ToActionUnit(maybe: Disjunction[String, Unit]): Result =
       |    maybe.map(value => NoContent)
       |      .leftMap(messages => BadRequest(messages)) match {
       |        case DRight(result) => result
       |        case DLeft(result)  => result
       |      }
       |
       |  implicit def ToActionMaybe[A: Format](maybe: Disjunction[String, Option[A]]): Result =
       |    maybe
       |      .map(maybeValue => maybeValue.map(value => Ok(Json.toJson(value))).getOrElse(NotFound))
       |      .leftMap(messages => BadRequest(messages)) match {
       |        case DRight(result) => result
       |        case DLeft(result)  => result
       |      }
       |
       |  implicit def ToActionList[A: Format](maybe: Disjunction[String, List[A]]): Result =
       |    maybe.map(xs => Ok(Json.toJson(xs)))
       |      .leftMap(messages => BadRequest(messages)) match {
       |        case DRight(result) => result
       |        case DLeft(result)  => result
       |      }
       |
       |  implicit def ToActionAny[A: Format](maybe: Disjunction[String, A]): Result =
       |    maybe.map(value => Ok(Json.toJson(value)))
       |      .leftMap(messages => BadRequest(messages)) match {
       |        case DRight(result) => result
       |        case DLeft(result)  => result
       |      }
       |}
    """.stripMargin

  def routes(componentPackage: String): String = {
    val controller: String = s"$componentPackage.controller.HealthController"
    s"""
       |GET /api/health $controller.check
    """.stripMargin
  }
}