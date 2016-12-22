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
import com.github.dnvriend.scaffold.play.enabler.EnablerResult
import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnablerResult
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnablerResult
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext, ScaffoldResult }
import com.github.dnvriend.scaffold.play.userinput.{ ComponentNameUserInput, EntityUserInput, ResourceNameUserInput }
import com.github.dnvriend.scaffold.play.util.FileUtils
import com.github.dnvriend.scaffold.play.util.Capitalize.ops._

import scalaz._
import Scalaz._

final case class CrudControllerScaffoldResult(componentName: String, entity: EntityUserInput, componentPackage: String, createdEntity: Path, createdRepository: Path, createdController: Path, packageObject: Path, createdEvolution: Path, alteredRoutes: Path) extends ScaffoldResult

class CrudControllerScaffold extends Scaffold {

  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    slickOrAnorm <- check(ctx.enabled)
    componentName <- ComponentNameUserInput.askUser("crud-controller")
    resourceName <- ResourceNameUserInput.askUser("crud-controller", componentName)
    entityUserInput <- EntityUserInput.askUser("DefaultEntity")
    componentPackage = s"${ctx.organization}.component.$componentName"
    createdEntity <- createEntity(ctx.srcDir, componentPackage, entityUserInput)
    createdRepository <- createRepository(ctx.srcDir, componentPackage, slickOrAnorm, entityUserInput)
    createdController <- createController(ctx.srcDir, componentPackage, slickOrAnorm, entityUserInput, resourceName)
    packageObject <- createPackageObject(ctx.srcDir, componentPackage, entityUserInput)
    maxEvolution <- FileUtils.maxEvolution(ctx.resourceDir)
    createdEvolution <- createEvolution(ctx.resourceDir / "evolutions" / "default", maxEvolution, Template.evolution(entityUserInput))
    alteredRoutes <- addRoutes(ctx.resourceDir, resourceName, componentPackage, entityUserInput)
  } yield CrudControllerScaffoldResult(componentName, entityUserInput, componentPackage, createdEntity, createdRepository, createdController, packageObject, createdEvolution, alteredRoutes)

  def check(enabled: List[EnablerResult]): Disjunction[String, EnablerResult] = {
    val slick = enabled.find(_.isInstanceOf[SlickEnablerResult])
    val anorm = enabled.find(_.isInstanceOf[AnormEnablerResult])
    (slick, anorm) match {
      case (Some(_), Some(_))                 => "Both Slick and Anorm enabled, please remove one or the other".left[EnablerResult]
      case (Some(slick: EnablerResult), None) => slick.right[String]
      case (None, Some(anorm: EnablerResult)) => anorm.right[String]
      case _                                  => "No Slick or Anorm enabled".left[EnablerResult]
    }
  }

  def createEntity(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, componentPackage, entity.className, Template.entity(componentPackage, entity))

  def createRepository(srcDir: Path, componentPackage: String, slickOrAnorm: EnablerResult, entity: EntityUserInput): Disjunction[String, Path] = {
    val content: String = slickOrAnorm match {
      case _: SlickEnablerResult => Template.repository(componentPackage, entity)
      case _: AnormEnablerResult => Template.repository(componentPackage, entity)
    }
    println("" + slickOrAnorm)
    FileUtils.createClass(srcDir, s"$componentPackage.repository", s"${entity.className}Repository", content)
  }

  def createController(srcDir: Path, componentPackage: String, slickOrAnorm: EnablerResult, entity: EntityUserInput, resourceName: String): Disjunction[String, Path] = {
    val content: String = slickOrAnorm match {
      case _: SlickEnablerResult => Template.controller(componentPackage, entity, resourceName)
      case _: AnormEnablerResult => Template.controller(componentPackage, entity, resourceName)
    }
    println("" + slickOrAnorm)
    FileUtils.createClass(srcDir, s"$componentPackage.controller", s"${entity.className}Controller", content)
  }

  def createPackageObject(srcDir: Path, componentPackage: String, entity: EntityUserInput): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, s"$componentPackage.util", "DisjunctionOps", Template.packageObject(componentPackage))

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
       |    SQL\"\"\"${entity.renderGetAllSql}\"\"\".as(${entity.className}.namedParser.*)
       |  }
       |
       |  def getById(id: Long): Option[${entity.className}] = db.withConnection { implicit conn =>
       |    SQL\"\"\"${entity.renderGetByIdSql}\"\"\".as(${entity.className}.namedParser.singleOpt)
       |  }
       |
       |  def save(${entity.renderFields}): Option[Long] = db.withConnection { implicit conn =>
       |    SQL\"\"\"${entity.renderSaveSql}\"\"\".executeInsert()
       |  }
       |
       |  def updateById(id: Long, ${entity.renderFields}): Int = db.withConnection { implicit conn =>
       |    SQL\"\"\"${entity.renderUpdateByIdSql}\"\"\".executeUpdate
       |  }
       |
       |  def deleteById(id: Long): Int = db.withConnection { implicit conn =>
       |    SQL\"\"\"${entity.renderDeleteByIdSql}\"\"\".executeUpdate()
       |  }
       |}
  """.stripMargin

  def controller(componentPackage: String, entity: EntityUserInput, resourceName: String): String = {
    val controllerType = s"${entity.className}Controller"
    val repo = s"${entity.className.uncapitalize}Repository"
    val repoType = s"${entity.className}Repository"
    val entityType: String = entity.className
    val entityName: String = entity.className.uncapitalize
    val executionContextName: String = ""

    s"""package $componentPackage.controller
       |
       |import javax.inject.Inject
       |
       |import $componentPackage.$entityType
       |import $componentPackage.repository.$repoType
       |import io.swagger.annotations._
       |import play.api.mvc._
       |
       |import scalaz._
       |import Scalaz._
       |
       |import javax.inject.Inject
       |
       |import com.github.dnvriend.component.address.Address
       |import com.github.dnvriend.component.address.repository.AddressRepository
       |import io.swagger.annotations._
       |import play.api.mvc._
       |import play.modules.slick.SlickExecutionContext
       |
       |import scalaz.Scalaz._
       |
       |@Api(value = "/api/address")
       |class AddressController @Inject() ($repo: $repoType)(implicit ec: $executionContextName) extends Controller {
       |
       |  @ApiOperation(value = "Get all address", response = classOf[Address], httpMethod = "GET")
       |  @ApiResponses(Array(new ApiResponse(code = 200, message = "Return a list of address")))
       |  def getAll(@ApiParam(value = "Fetch number of items") limit: Int, @ApiParam(value = "Fetch from offset") offset: Int): Action[AnyContent] =
       |    Action.async((for {
       |      (limit, offset) <- (intValidator("limit", limit) tuple intValidator("offset", offset)).liftEither
       |      xs <- addressRepository.getAll(limit, offset).liftEither
       |    } yield xs).run)
       |
       |  @ApiOperation(value = "Get address by id", response = classOf[Address], httpMethod = "GET")
       |  @ApiResponses(Array(
       |    new ApiResponse(code = 200, message = "Returns address when found"),
       |    new ApiResponse(code = 404, message = "Returns 404 when not found")
       |  ))
       |  def getById(@ApiParam(value = "id of address") id: Long): Action[AnyContent] = Action.async((for {
       |    id <- idValidator("id", id).liftEither
       |    address <- addressRepository.getById(id).liftEither
       |  } yield address).run)
       |
       |  @ApiOperation(value = "Save address", response = classOf[Address], httpMethod = "POST")
       |  @ApiResponses(Array(new ApiResponse(code = 200, message = "Returns the stored address with id")))
       |  def save(): Action[AnyContent] = Action.async(request => (for {
       |    address <- request.toValidationNel[Address].liftEither
       |    id <- addressRepository.save(address.street, address.zipcode, address.housenr).liftEither
       |  } yield address.copy(id = Option(id))).run)
       |
       |  @ApiOperation(value = "Update address by id", response = classOf[Address], httpMethod = "PUT")
       |  @ApiResponses(Array(new ApiResponse(code = 200, message = "Returns the updated address")))
       |  def updateById(@ApiParam(value = "id of address") id: Long): Action[AnyContent] = Action.async(request => (for {
       |    id <- idValidator("id", id).liftEither
       |    address <- request.toValidationNel[Address].liftEither
       |    _ <- addressRepository.updateById(id, address.street, address.zipcode, address.housenr).liftEither
       |  } yield address).run)
       |
       |  @ApiOperation(value = "Delete address by id", httpMethod = "DELETE")
       |  @ApiResponses(Array(new ApiResponse(code = 204, message = "Returns no content")))
       |  def deleteById(@ApiParam(value = "id of address") id: Long): Action[AnyContent] = Action.async((for {
       |    id <- idValidator("id", id).liftEither
       |    _ <- addressRepository.deleteById(id).liftEither
       |  } yield ()).run)
       |}
  """.stripMargin
  }

  def packageObject(componentPackage: String): String =
    s"""
       |package $componentPackage
       |
    |import play.api.data.validation.ValidationError
       |import play.api.libs.json.{Format, JsPath, JsResult, Json}
       |import play.api.mvc.{AnyContent, Request, Result, Results}
       |
    |import scala.concurrent.{ExecutionContext, Future}
       |import scala.language.implicitConversions
       |import scalaz.Scalaz._
       |import scalaz._
       |
    |package object controller extends Results {
       |  implicit class FutureImplicits[A](val that: Future[A]) extends AnyVal {
       |    def liftEither(implicit ec: ExecutionContext): DisjunctionT[Future, String, A] = EitherT(that.map(_.right[String]))
       |  }
       |  def tryCatch[A](block: => A): Disjunction[String, A] =
       |    Disjunction.fromTryCatchNonFatal(block).leftMap(_.toString)
       |
    |  implicit class FutureDisjunctionOps[A](val that: Future[Disjunction[String, A]]) extends AnyVal {
       |    def liftEither: DisjunctionT[Future, String, A] = EitherT(that)
       |  }
       |
    |  implicit def FutureUnitAction(that: Future[Disjunction[String, Unit]])(implicit ec: ExecutionContext): Future[Result] =
       |    that.map(ToActionUnit)
       |
    |  implicit def FutureAnyAction[A: Format](that: Future[Disjunction[String, A]])(implicit ec: ExecutionContext): Future[Result] =
       |    that.map(a => ToActionA(a))
       |
    |  implicit def FutureMaybeAction[A: Format](that: Future[Disjunction[String, Option[A]]])(implicit ec: ExecutionContext): Future[Result] =
       |    that.map(maybe => ToActionMaybe(maybe))
       |
    |  implicit def FutureListAction[A: Format](that: Future[Disjunction[String, List[A]]])(implicit ec: ExecutionContext): Future[Result] =
       |    that.map(xs => ToActionList(xs))
       |
    |  implicit def FutureVectorAction[A: Format](that: Future[Disjunction[String, Vector[A]]])(implicit ec: ExecutionContext): Future[Result] =
       |    that.map(xs => ToActionVector(xs))
       |
    |  implicit def ToActionUnit(unit: Disjunction[String, Unit]): Result =
       |    unit.map(value => NoContent)
       |      .leftMap(messages => BadRequest(messages)) match {
       |      case DRight(result) => result
       |      case DLeft(result)  => result
       |    }
       |
    |  implicit def ToActionMaybe[A: Format](maybe: Disjunction[String, Option[A]]): Result =
       |    maybe
       |      .map(maybeValue => maybeValue.map(value => Ok(Json.toJson(value))).getOrElse(NotFound))
       |      .leftMap(messages => BadRequest(messages)) match {
       |      case DRight(result) => result
       |      case DLeft(result)  => result
       |    }
       |
    |  implicit def ToActionVector[A: Format](xs: Disjunction[String, Vector[A]]): Result =
       |    ToActionList(xs.map(_.toList))
       |
    |  implicit def ToActionList[A: Format](xs: Disjunction[String, List[A]]): Result =
       |    xs.map(xs => Ok(Json.toJson(xs)))
       |      .leftMap(messages => BadRequest(messages)) match {
       |      case DRight(result) => result
       |      case DLeft(result)  => result
       |    }
       |
    |  implicit def ToActionA[A: Format](a: Disjunction[String, A]): Result =
       |    a.map(value => Ok(Json.toJson(value)))
       |      .leftMap(messages => BadRequest(messages)) match {
       |      case DRight(result) => result
       |      case DLeft(result)  => result
       |    }
       |
    |  implicit class ValImplicits[A](val that: ValidationNel[String, A]) extends AnyVal {
       |    def toDisjunction: Disjunction[String, A] = that.disjunction.leftMap(_.toList.mkString(","))
       |    def toDisjunctionF: Future[Disjunction[String, A]] = Future.successful(that.toDisjunction)
       |    def liftEither: DisjunctionT[Future, String, A] = EitherT(toDisjunctionF)
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
       |
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
