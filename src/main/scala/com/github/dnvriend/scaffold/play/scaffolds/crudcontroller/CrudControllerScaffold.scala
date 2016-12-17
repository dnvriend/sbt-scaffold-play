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

package com.github.dnvriend.scaffold.play.scaffolds.crudcontroller

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.{ ComponentNameUserInput, EntityUserInput, ResourceNameUserInput }
import com.github.dnvriend.scaffold.play.util.FileUtils
import com.github.dnvriend.scaffold.play.util.Capitalize.ops._

import scalaz._

final case class CrudControllerScaffoldResult(componentName: String, entity: EntityUserInput, componentPackage: String, createdEntity: Path, createdRepository: Path, createdController: Path, createdDisjunctionOps: Path, createdValidationOps: Path, createdValidator: Path, createdEvolution: Path, alteredRoutes: Path) extends ScaffoldResult

class CrudControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    componentName <- ComponentNameUserInput.askUser("crud-controller")
    resourceName <- ResourceNameUserInput.askUser("crud-controller", componentName)
    entityUserInput <- EntityUserInput.askUser("DefaultEntity")
    componentPackage = s"${ctx.organization}.component.$componentName"
    createdEntity <- createEntity(ctx.srcDir, componentPackage, entityUserInput)
    createdRepository <- createRepository(ctx.srcDir, componentPackage, entityUserInput)
    createdController <- createController(ctx.srcDir, componentPackage, entityUserInput)
    createdDisjunctionOps <- createDisjunctionOps(ctx.srcDir, componentPackage, entityUserInput)
    createdValidationOps <- createValidationOps(ctx.srcDir, componentPackage, entityUserInput)
    createdValidator <- createValidator(ctx.srcDir, componentPackage, entityUserInput)
    maxEvolution <- FileUtils.maxEvolution(ctx.resourceDir)
    createdEvolution <- createEvolution(ctx.resourceDir / "evolutions" / "default", maxEvolution, Template.evolution(entityUserInput))
    alteredRoutes <- addRoutes(ctx.resourceDir, resourceName, componentPackage, entityUserInput)
  } yield CrudControllerScaffoldResult(componentName, entityUserInput, componentPackage, createdEntity, createdRepository, createdController, createdDisjunctionOps, createdValidationOps, createdValidator, createdEvolution, alteredRoutes)

  def createEntity(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, componentPackage, entity.className, Template.entity(componentPackage, entity))

  def createRepository(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.repository", s"${entity.className}Repository", Template.repository(componentPackage, entity))

  def createController(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.controller", s"${entity.className}Controller", Template.controller(componentPackage, entity))

  def createDisjunctionOps(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.util", "DisjunctionOps", Template.disjunctionOps(componentPackage))

  def createValidationOps(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.util", "ValidationOps", Template.validationOps(componentPackage))

  def createValidator(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.util", "Validator", Template.validator(componentPackage))

  def createEvolution(defaultEvolutionDir: Path, maxEvolution: Int, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(defaultEvolutionDir / s"${maxEvolution + 1}.sql", content)

  def addRoutes(resourceDir: Path, resourceName: String, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, Template.routes(componentPackage, resourceName, entity))
}

object Template {
  def entity(componentPackage: String, entity: EntityUserInput): String =
    s"""
    |package $componentPackage
    |
    |import play.api.libs.json.{ Format, Json }
    |import anorm.{ Macro, RowParser }
    |
    |object ${entity.className} {
    |  implicit val format: Format[${entity.className}] = Json.format[${entity.className}]
    |  val namedParser: RowParser[${entity.className}] = Macro.namedParser[${entity.className}]
    |}
    |
    |final case class ${entity.className}(${entity.renderFields}, id: Option[Long] = None)
  """.stripMargin

  def repository(componentPackage: String, entity: EntityUserInput): String =
    s"""package $componentPackage.repository
       |
       |import javax.inject.Inject
       |
       |import anorm._
       |import $componentPackage.${entity.className}
       |import play.api.db.Database
       |
       |class ${entity.className}Repository @Inject() (db: Database) {
       |  def getAll(limit: Int, offset: Int): List[${entity.className}] = db.withConnection { implicit conn =>
       |    SQL"${entity.renderGetAllSql}".as(${entity.className}.namedParser.*)
       |  }
       |
       |  def getById(id: Long): Option[${entity.className}] = db.withConnection { implicit conn =>
       |    SQL"${entity.renderGetByIdSql}".as(${entity.className}.namedParser.singleOpt)
       |  }
       |
       |  def save(${entity.renderFields}): Option[Long] = db.withConnection { implicit conn =>
       |    SQL"${entity.renderSaveSql}".executeInsert()
       |  }
       |
       |  def updateById(id: Long, ${entity.renderFields}): Int = db.withConnection { implicit conn =>
       |    SQL"${entity.renderUpdateByIdSql}".executeUpdate
       |  }
       |
       |  def deleteById(id: Long): Int = db.withConnection { implicit conn =>
       |    SQL"${entity.renderDeleteByIdSql}".executeUpdate()
       |  }
       |}
  """.stripMargin

  def controller(componentPackage: String, entity: EntityUserInput): String = {
    val controllerType = s"${entity.className}Controller"
    val repo = s"${entity.className.uncapitalize}Repository"
    val repoType = s"${entity.className}Repository"
    val entityType: String = entity.className
    val entityName: String = entity.className.uncapitalize

    s"""package $componentPackage.controller
       |
       |import javax.inject.Inject
       |
       |import $componentPackage.$entityType
       |import $componentPackage.repository.$repoType
       |import $componentPackage.util.DisjunctionOps._
       |import $componentPackage.util.ValidationOps._
       |import $componentPackage.util.Validator
       |import play.api.mvc._
       |
       |import scalaz._
       |import Scalaz._
       |
       |class $controllerType @Inject() ($repo: $repoType) extends Controller {
       |  def getAll(limit: Int, offset: Int): Action[AnyContent] = Action(for {
       |    (limit, offset) <- (Validator.intValidator("limit", limit) tuple Validator.intValidator("offset", offset)).toDisjunction
       |    xs <- tryCatch($repo.getAll(limit, offset))
       |  } yield xs)
       |
       |  def getById(id: Long): Action[AnyContent] = Action(for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    $entityName <- tryCatch($repo.getById(id))
       |  } yield $entityName)
       |
       |  def save(): Action[AnyContent] = Action(request => for {
       |    $entityName <- request.toValidationNel[$entityType].toDisjunction
       |    id <- tryCatch($repo.save(${entity.renderEntityNameField}))
       |  } yield $entityName.copy(id = id))
       |
       |  def updateById(id: Long): Action[AnyContent] = Action(request => for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    $entityName <- request.toValidationNel[$entityType].toDisjunction
       |    _ <- tryCatch($repo.updateById(id, ${entity.renderEntityNameField}))
       |  } yield $entityName)
       |
       |  def deleteById(id: Long): Action[AnyContent] = Action(for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    _ <- tryCatch($repo.deleteById(id))
       |  } yield ())
       |}
  """.stripMargin
  }

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

  def validationOps(componentPackage: String): String =
    s"""
       |package $componentPackage.util
       |
       |import play.api.data.validation.ValidationError
       |import play.api.libs.json.{ Format, JsPath, JsResult }
       |import play.api.mvc.{ AnyContent, Request }
       |
       |import scalaz._
       |import Scalaz._
       |
       |object ValidationOps {
       |  implicit class ValImplicits[A](val that: ValidationNel[String, A]) extends AnyVal {
       |    def toDisjunction: Disjunction[String, A] = that.disjunction.leftMap(_.toList.mkString(","))
       |    def toDisjunctionNel: Disjunction[NonEmptyList[String], A] = that.disjunction
       |  }
       |
       |  def jsResultToValidationNel[A](jsResult: JsResult[A]): ValidationNel[String, A] = {
       |    def validationToString(path: JsPath, xs: Seq[ValidationError]): String = {
       |      val pathString = path.toString
       |      val errorsString = xs.flatMap(_.messages).mkString(",")
       |      s"'$$pathString', '$$errorsString'"
       |    }
       |    def validationErrorsToString(xs: Seq[(JsPath, Seq[ValidationError])]): String =
       |      xs.map((validationToString _).tupled).mkString(",")
       |
       |    jsResult.asEither
       |      .validation
       |      .leftMap(validationErrorsToString)
       |      .leftMap(_.wrapNel)
       |  }
       |
       |  implicit class JsResultOps[A](val that: JsResult[A]) extends AnyVal {
       |    def toValidationNel: ValidationNel[String, A] =
       |      jsResultToValidationNel(that)
       |  }
       |
       |  implicit class OptionJsResultOps[A](val that: Option[JsResult[A]]) extends AnyVal {
       |    def toValidationNel: ValidationNel[String, A] =
       |      that.map(jsResultToValidationNel).getOrElse("No JsResult to validate".failureNel[A])
       |  }
       |
       |  implicit class RequestOps(val that: Request[AnyContent]) extends AnyVal {
       |    def toValidationNel[A: Format]: ValidationNel[String, A] =
       |      that.body.asJson.map(_.validate[A]).toValidationNel
       |  }
       |}
     """.stripMargin

  def validator(componentPackage: String): String =
    s"""
       |package $componentPackage.util
       |
       |import scalaz._
       |import Scalaz._
       |
       |object Validator {
       |  def intValidator(fieldName: String, value: Int): ValidationNel[String, Int] =
       |    Option(value).filter(_ >= 0).toSuccessNel(s"Field '$$fieldName' with value '$$value' must be gte zero")
       |
       |  def idValidator(fieldName: String, value: Long): ValidationNel[String, Long] =
       |    Option(value).filter(_ >= 0).toSuccessNel(s"Field '$$fieldName' with value '$$value' must be gte zero")
       |
       |  def stringValidator(fieldName: String, value: String): ValidationNel[String, String] =
       |    Option(value).map(_.trim).filterNot(_.isEmpty).toSuccessNel(s"Field '$$fieldName' with value '$$value' must not be empty")
       |}
     """.stripMargin

  def evolution(entity: EntityUserInput): String =
    s"""
    |# --- !Ups
    |
    |${entity.renderCreateTable}
    |
    |# --- !Downs
    |
    |${entity.renderDropTable}
  """.stripMargin

  def routes(componentPackage: String, resource: String, entity: EntityUserInput): String = {
    val controller: String = s"$componentPackage.controller.${entity.className}Controller"
    s"""
      |GET           /api/$resource            $controller.getAll(limit: Int ?= 10, offset: Int ?= 0)
      |GET           /api/$resource/:id        $controller.getById(id: Long)
      |POST          /api/$resource            $controller.save()
      |PUT           /api/$resource/:id        $controller.updateById(id: Long)
      |DELETE        /api/$resource/:id        $controller.deleteById(id: Long)
    """.stripMargin
  }
}
