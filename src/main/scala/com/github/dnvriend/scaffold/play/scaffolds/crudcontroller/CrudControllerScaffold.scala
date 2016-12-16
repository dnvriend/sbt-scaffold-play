package com.github.dnvriend.scaffold.play.scaffolds.crudcontroller

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{Scaffold, ScaffoldContext, ScaffoldResult}
import com.github.dnvriend.scaffold.play.userinput.{ComponentNameUserInput, EntityUserInput}
import com.github.dnvriend.scaffold.play.util.DisjunctionOps.DisjunctionOfThrowableToDisjunctionOfString
import com.github.dnvriend.scaffold.play.util.FileUtils
import com.github.dnvriend.scaffold.play.util.StringUtils.StringImplicits

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
    |object $entityType {
    | implicit val ...
    |}
    |
    |final case class $entityType()
  """.stripMargin

  def repository(packageName: String, className: String, entityName: String, entityType: String): String =
    s"""package $packageName
       |
       |class ${entityType}Repository @Inject() (db: Database) {
       |  def getAll(limit: Int, offset: Int): Seq[$entityType] = ???
       |
       |  def getById(id: Long): $entityType = ???
       |
       |  def save($entityName: $entityType): Int = ???
       |
       |  def updateById(id: Long, $entityName: $entityType): Int = ???
       |
       |  def deleteById(id: Long): Int = ???
       |}
  """.stripMargin

  def controller(packageName: String, className: String, entityName: String): String =
    s"""package $packageName
       |
       |class ${entityName}Controller @Inject() (${entityName}Repository: ${entityName}Repository) {
       |  def getAll(limit: Int = 10, offset: Int = 0) = ???
       |
       |  def getById(id: Long) = ???
       |
       |  def save = ???
       |
       |  def updateById(id: Long) = ???
       |
       |  def deleteById(id: Long) = ???
       |}
  """.stripMargin

  def evolution(entityName: String): String =
  """
    |
  """.stripMargin
}