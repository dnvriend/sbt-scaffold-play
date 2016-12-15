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
import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.repository.ScaffoldRepository
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, UserInput }
import com.google.inject.Inject
import org.slf4j.{ Logger, LoggerFactory }

import scalaz._

object WsClientScaffold {
  final val ID: String = classOf[WsClientScaffold].getName
  final val DefaultClassName = "DefaultWsClient"
}

class WsClientScaffold @Inject() (repo: ScaffoldRepository) extends Scaffold {
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def execute(ctx: ScaffoldContext): Unit = {
    log.debug("Scaffolding a web service client: " + ctx)

    val maybeResult: Disjunction[String, Path] = for {
      packageName <- UserInput.readLine[String](Parsers.packageParser(ctx.organization), "Enter package name > ")
      className <- UserInput.readLine[String](Parsers.classNameParser(WsClientScaffold.DefaultClassName), "Enter className > ")
      content <- generateContent(packageName, className)
      createdClass <- create(ctx.srcDir, packageName, className, content)
    } yield createdClass

    maybeResult match {
      case DRight(created) =>
        log.debug(s"Successfully created: $created")
      case DLeft(message) =>
        log.warn(s"Could not scaffold a web service client: $message")
    }
  }

  def generateContent(packageName: String, className: String): Disjunction[String, String] =
    Disjunction.fromTryCatchNonFatal(scaffold.wsclient.txt.wsclient.render(packageName, className).toString)
      .leftMap(_.toString)

  def create(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, content)
}