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
import com.github.dnvriend.scaffold.play.userinput.{ ComponentNameUserInput, EntityUserInput }
import com.github.dnvriend.scaffold.play.util.ScaffoldDisjunction.DisjunctionOfThrowableToDisjunctionOfString
import com.github.dnvriend.scaffold.play.util.FileUtils
import com.github.dnvriend.scaffold.play.util.Capitalize.ops._
import scalaz._

final case class CrudControllerScaffoldResult(componentName: String, entity: EntityUserInput) extends ScaffoldResult

class CrudControllerScaffold extends Scaffold {
  override def execute(ctx: ScaffoldContext): Disjunction[String, ScaffoldResult] = for {
    componentName <- ComponentNameUserInput.askUser("crud-controller")
    entity <- EntityUserInput.askUser("DefaultEntity")
  } yield CrudControllerScaffoldResult(componentName, entity)

  def addRoute(resourceDir: Path, packageName: String, className: String, resourceName: String): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, s"GET $resourceName $packageName.$className.action")

  //  def generateContent(packageName: String, className: String): Disjunction[String, String] =
  //    Disjunction.fromTryCatchNonFatal(Template.render(packageName, className))

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)
}

object Template {
  def entity(packageName: String, entityName: String, entityType: String): String =
    s"""
    |package $packageName
    |
    |import play.api.libs.json.{ Format, Json }
    |import anorm.{ Macro, RowParser }
    |
    |object Person {
    |  implicit val format: Format[Person] = Json.format[Person]
    |  val namedParser: RowParser[Person] = Macro.namedParser[Person]
    |}
    |
    |final case class Person(name: String, age: Int, id: Option[Long] = None)
  """.stripMargin

  def repository(packageName: String, className: String, entityName: String, entityType: String): String =
    s"""package $packageName
       |
       |import javax.inject.Inject
       |
       |import anorm._
       |import $packageName.$entityType
       |import play.api.db.Database
       |
       |class ${entityName}Repository @Inject() (db: Database) {
       |  def getAll(limit: Int, offset: Int): List[$entityType] = db.withConnection { implicit conn =>
       |    SQL"SELECT * FROM PERSON LIMIT $$limit OFFSET $$offset".as(Person.namedParser.*)
       |  }
       |
       |  def getById(id: Long): Option[$entityType] = db.withConnection { implicit conn =>
       |    SQL"SELECT * FROM PERSON WHERE id = $$id".as(Person.namedParser.singleOpt)
       |  }
       |
       |  def save(name: String, age: Int): Option[Long] = db.withConnection { implicit conn =>
       |    SQL"INSERT INTO PERSON (name, age) VALUES ($$name, $$age)".executeInsert()
       |  }
       |
       |  def updateById(id: Long, name: String, age: Int): Int = db.withConnection { implicit conn =>
       |    SQL"UPDATE PERSON SET name=$$name, age=$$age WHERE id=$$id".executeUpdate
       |  }
       |
       |  def deleteById(id: Long): Int = db.withConnection { implicit conn =>
       |    SQL"DELETE PERSON WHERE id = $$id".executeUpdate()
       |  }
       |}
  """.stripMargin

  def controller(packageName: String, className: String, entityName: String, entityType: String): String =
    s"""package $packageName
       |
       |import javax.inject.Inject
       |
       |import com.github.dnvriend.component.personregistry.Person
       |import com.github.dnvriend.component.personregistry.repository.PersonRepository
       |import com.github.dnvriend.component.personregistry.util.DisjunctionOps._
       |import com.github.dnvriend.component.personregistry.util.ValidationOps._
       |import com.github.dnvriend.component.personregistry.util.Validator
       |import play.api.mvc._
       |
       |import scalaz._
       |import Scalaz._
       |
       |class ${entityType}Controller @Inject() (${entityName}Repository: ${entityType}Repository) extends Controller {
       |  def getAll(limit: Int, offset: Int): Action[AnyContent] = Action(for {
       |    (limit, offset) <- (Validator.intValidator("limit", limit) tuple Validator.intValidator("offset", offset)).toDisjunction
       |    xs <- tryCatch(personRepository.getAll(limit, offset))
       |  } yield xs)
       |
       |  def getById(id: Long): Action[AnyContent] = Action(for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    $entityName <- tryCatch(${entityName}Repository.getById(id))
       |  } yield $entityName)
       |
       |  def save(): Action[AnyContent] = Action(request => for {
       |    $entityName <- request.toValidationNel[$entityName].toDisjunction
       |    id <- tryCatch(${entityName}Repository.save(person.name, person.age))
       |  } yield person.copy(id = id))
       |
       |  def updateById(id: Long): Action[AnyContent] = Action(request => for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    $entityName <- request.toValidationNel[Person].toDisjunction
       |    _ <- tryCatch(${entityName}Repository.updateById(id, person.name, person.age))
       |  } yield $entityName)
       |
       |  def deleteById(id: Long): Action[AnyContent] = Action(for {
       |    id <- Validator.idValidator("id", id).toDisjunction
       |    _ <- tryCatch(${entityName}Repository.deleteById(id))
       |  } yield ())
       |}
  """.stripMargin

  def disjunctionOps(packageName: String): String =
    s"""
    |package $packageName
    |
    |import play.api.libs.json.{ Format, Json }
    |import play.api.mvc.{ Result, Results }
    |
    |import scala.language.implicitConversions
    |import scalaz.Disjunction
    |
    |object DisjunctionOps extends Results {
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

  def validationOps(packageName: String): String =
    s"""
       |package $packageName
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

  def validator(packageName: String): String =
    s"""
       |package $packageName
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

  def evolution(entityName: String): String =
    """
    |# --- !Ups
    |
    |CREATE TABLE person (
    |    id SERIAL,
    |    name VARCHAR(255) NOT NULL,
    |    age INT NOT NULL
    |);
    |
    |# --- !Downs
    |
    |DROP TABLE person;
  """.stripMargin
}
