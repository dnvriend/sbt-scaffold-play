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

package com.github.dnvriend.scaffold.play.scaffolds.wsclient

import ammonite.ops._
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext }
import com.github.dnvriend.scaffold.play.userinput.PackageClassUserInput
import com.github.dnvriend.scaffold.play.util.FileUtils
import sbt.Logger

import scalaz._

object WsClientScaffold {
  final val ID: String = classOf[WsClientScaffold].getName
  final val DefaultClassName = "DefaultWsClient"
}

class WsClientScaffold(implicit log: Logger) extends Scaffold {

  override def execute(ctx: ScaffoldContext): Unit = {
    log.debug("Scaffolding a web service client: " + ctx)

    val maybeResult: Disjunction[String, Path] = for {
      input <- PackageClassUserInput.askUser(ctx.organization, WsClientScaffold.DefaultClassName)
      content <- generateContent(input.packageName, input.className)
      createdClass <- create(ctx.srcDir, input.packageName, input.className, content)
    } yield createdClass

    maybeResult match {
      case DRight(created) =>
        log.info(s"Successfully created: $created")
      case DLeft(message) =>
        log.warn(s"Could not scaffold a web service client: $message")
    }
  }

  def generateContent(packageName: String, className: String): Disjunction[String, String] =
    Disjunction.fromTryCatchNonFatal(Template.render(packageName, className).toString)
      .leftMap(_.toString)

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)
}

object Template {
  def render(packageName: String, className: String): String =
    s"""package $packageName
    |
    |import javax.inject.Inject
    |import play.api.libs.ws.{WSClient, WSRequest}
    |import scala.concurrent.{ExecutionContext, Future}
    |import org.slf4j.{ Logger, LoggerFactory }
    |
    |class $className @Inject() (wsClient: WSClient)(implicit ec: ExecutionContext) {
    |  val log: Logger = LoggerFactory.getLogger(this.getClass)
    |}
  """.stripMargin
}