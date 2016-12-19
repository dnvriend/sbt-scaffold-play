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

package com.github.dnvriend.scaffold.play.enabler.slick

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class SlickEnablerResult(setting: Path, config: Path, createdModule: Path) extends EnablerResult

object SlickEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings(ctx))
    config <- createConfig(ctx.resourceDir, Template.config)
    createdModule <- createModule(ctx.srcDir, "play.modules.slick", "SlickModule")
    _ <- FileUtils.createDirectory(ctx.resourceDir / "evolutions" / "default")
    _ <- addConfig(ctx.resourceDir)
  } yield SlickEnablerResult(settings, config, createdModule)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-slick.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "slick.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "slick"""")

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
      |libraryDependencies += "com.typesafe.play" %% "play-slick" % "${ctx.playSlickVersion}"
      |libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "${ctx.playSlickVersion}"
      |libraryDependencies += "com.typesafe.slick" %% "slick" % "${ctx.slickVersion}"
      |libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "${ctx.slickVersion}"
      |// database driver
      |libraryDependencies += "com.h2database" % "h2" % "${ctx.h2Version}"
      |libraryDependencies += "org.postgresql" % "postgresql" % "${ctx.postgresVersion}"
    """.stripMargin

  val module: String =
    """
    |package play.modules.slick
    |
    |import javax.inject.Singleton
    |
    |import akka.actor.ActorSystem
    |import com.google.inject.{AbstractModule, Provides}
    |
    |import scala.concurrent.ExecutionContext
    |
    |class SlickModule extends AbstractModule {
    |  override def configure(): Unit = {
    |    @Provides @Singleton
    |    def slickExecutionContextProvider(system: ActorSystem): SlickExecutionContext =
    |      new SlickExecutionContext(system)
    |  }
    |}
    |
    |class SlickExecutionContext (system: ActorSystem) extends ExecutionContext {
    |  val ec: ExecutionContext = system.dispatchers.lookup("slick.context")
    |  override def execute(runnable: Runnable): Unit = ec.execute(runnable)
    |  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
    |}
  """.stripMargin

  val config: String =
    """
    |# H2 Configuration
    |slick.dbs.default.driver="slick.driver.H2Driver$"
    |slick.dbs.default.db.driver="org.h2.Driver"
    |slick.dbs.default.db.url="jdbc:h2:mem:play"
    |
    |# Postgres configuration
    |#slick.dbs.default.driver="slick.driver.PostgresDriver$"
    |#slick.dbs.default.db.driver="org.postgresql.Driver"
    |#slick.dbs.default.db.url="jdbc:postgresql://localhost:5432/postgres?reWriteBatchedInserts=true"
    |#slick.dbs.default.db.user=postgres
    |#slick.dbs.default.db.password=postgres
    |
    |slick.dbs.default.db.maximumPoolSize=10
    |
    |slick {
    |  context {
    |   fork-join-executor {
    |      parallelism-max=10
    |    }
    |  }
    |}
    |
    |play.modules.enabled += "play.modules.slick.SlickModule"
  """.stripMargin
}
