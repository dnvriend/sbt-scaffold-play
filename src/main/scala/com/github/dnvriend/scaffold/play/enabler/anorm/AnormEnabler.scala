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

package com.github.dnvriend.scaffold.play.enabler.anorm

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnablerResult
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz.Scalaz._
import scalaz._

object AnormEnablerResult extends PathFormat {
  implicit val format: Format[AnormEnablerResult] = Json.format[AnormEnablerResult]
}
final case class AnormEnablerResult(setting: Path, config: Path, createdModule: Path) extends EnablerResult

object AnormEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    settings <- createSettings(ctx.baseDir, Template.settings(ctx))
    config <- createConfig(ctx.resourceDir, Template.config)
    createdModule <- createModule(ctx.srcDir, "play.modules.anorm", "AnormModule")
    _ <- FileUtils.createDirectory(ctx.resourceDir / "evolutions" / "default")
    _ <- addConfig(ctx.resourceDir)
  } yield AnormEnablerResult(settings, config, createdModule)

  def check(enabled: List[EnablerResult]): Disjunction[String, Unit] = {
    val slick = enabled.find(_.isInstanceOf[SlickEnablerResult])
    val anorm = enabled.find(_.isInstanceOf[AnormEnablerResult])
    (slick, anorm) match {
      case (Some(_), Some(_)) => "Both Slick and Anorm already installed".left[Unit]
      case (Some(_), None)    => "Slick already installed".left[Unit]
      case (None, Some(_))    => "Anorm already installed".left[Unit]
      case _                  => ().right[String]
    }
  }

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-anorm.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "anorm.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "anorm"""")

  def createModule(srcDir: Path, packageName: String, className: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, Template.module)
}

object Template {
  def settings(ctx: EnablerContext): String =
    s"""
      |// database support
      |libraryDependencies += jdbc
      |libraryDependencies += evolutions
      |libraryDependencies += "com.zaxxer" % "HikariCP" % "${ctx.hikariCpVersion}"
      |libraryDependencies += "com.typesafe.play" %% "anorm" % "${ctx.anormVersion}"
      |// database driver
      |libraryDependencies += "com.h2database" % "h2" % "${ctx.h2Version}"
      |libraryDependencies += "org.postgresql" % "postgresql" % "${ctx.postgresVersion}"
    """.stripMargin

  val module: String =
    """
    |package play.modules.anorm
    |
    |import javax.inject.Singleton
    |
    |import akka.actor.ActorSystem
    |import com.google.inject.{AbstractModule, Provides}
    |
    |import scala.concurrent.ExecutionContext
    |
    |class AnormModule extends AbstractModule {
    |  override def configure(): Unit = {
    |    @Provides @Singleton
    |    def anormExecutionContextProvider(system: ActorSystem): AnormExecutionContext =
    |      new AnormExecutionContext(system)
    |  }
    |}
    |
    |class AnormExecutionContext (system: ActorSystem) extends ExecutionContext {
    |  val ec: ExecutionContext = system.dispatchers.lookup("anorm.context")
    |  override def execute(runnable: Runnable): Unit = ec.execute(runnable)
    |  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
    |}
  """.stripMargin

  val config: String =
    """
    |# H2 configuration
    |db.default.driver=org.h2.Driver
    |db.default.url="jdbc:h2:mem:play"
    |
    |# Postgres configuration
    |#db.default.driver=org.postgresql.Driver
    |#db.default.url="jdbc:postgresql://localhost:5432/postgres?reWriteBatchedInserts=true"
    |#db.default.username="postgres"
    |#db.default.password="postgres"
    |
    |# play evolutions
    |play.evolutions.enabled=true
    |play.evolutions.autoApply=true
    |
    |# Connection pool configuration
    |play.db.autocommit = true
    |play.db.connectionTimeout = 30 seconds
    |play.db.idleTimeout = 10 minutes
    |play.db.maxLifetime = 30 minutes
    |play.db.maximumPoolSize = 10
    |play.db.initializationFailFast = false
    |play.db.isolateInternalQueries = false
    |play.db.allowPoolSuspension = false
    |play.db.readOnly = false
    |play.db.registerMbeans = false
    |play.db.validationTimeout = 5 seconds
    |
    |anorm {
    |  context {
    |   fork-join-executor {
    |      parallelism-max=10
    |    }
    |  }
    |}
    |
    |play.modules.enabled += "play.modules.anorm.AnormModule"
  """.stripMargin
}
