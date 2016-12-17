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

package com.github.dnvriend.scaffold.play.enabler.swagger

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class SwaggerEnablerResult(settings: Path, plugin: Path) extends EnablerResult

// see: https://github.com/swagger-api/swagger-play/tree/master/play-2.5/swagger-play2
class SwaggerEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
    config <- createConfig(ctx.resourceDir, Template.config())
    _ <- addConfig(ctx.resourceDir)
    _ <- addRoute(ctx.resourceDir)
  } yield SwaggerEnablerResult(settings, config)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-swagger.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "swagger.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "swagger"""")

  def addRoute(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToRoutes(resourceDir, Template.route())
}

object Template {
  def settings(): String =
    s"""
       |libraryDependencies += "io.swagger" %% "swagger-play2" % "1.5.3"
    """.stripMargin

  def config(): String =
    """
    |play.modules.enabled += "play.modules.swagger.SwaggerModule"
    |
    |api.version = "beta"                         // (String) - version of API | default: "beta"
    |swagger.api.basepath="http://localhost:9000" // (String) - base url | default: "http://localhost:9000"
    |swagger.filter=""                            // (String) - classname of swagger filter | default: empty
    |swagger.api.info = {
    |  contact=""                                 // (String) - Contact Information | default : empty,
    |  description=""                             // (String) - Description | default : empty,
    |  title=""                                   // (String) - Title | default : empty,
    |  termsOfService=""                          // (String) - Terms Of Service | default : empty,
    |  license=""                                 // (String) - Terms Of Service | default : empty,
    |  licenseUrl=""                              // (String) - Terms Of Service | default : empty
    |}
  """.stripMargin

  def route(): String =
    """
    |GET     /swagger.json           controllers.ApiHelpController.getResources
  """.stripMargin
}
