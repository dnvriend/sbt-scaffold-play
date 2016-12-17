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
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class AnormEnablerResult(setting: Path, config: Path) extends EnablerResult

class AnormEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, AnormEnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
    config <- createConfig(ctx.resourceDir, Template.config())
    _ <- FileUtils.createDirectory(ctx.resourceDir / "evolutions" / "default")
    _ <- addConfig(ctx.resourceDir)
  } yield AnormEnablerResult(settings, config)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-anorm.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "anorm.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "anorm"""")
}

object Template {
  def settings(): String =
    """
      |// database support
      |libraryDependencies += jdbc
      |libraryDependencies += evolutions
      |libraryDependencies += "com.zaxxer" % "HikariCP" % "2.5.1"
      |libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.2"
      |// database driver
      |libraryDependencies += "com.h2database" % "h2" % "1.4.193"
      |libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"
    """.stripMargin

  def config(): String =
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
    |play.db.pool=hikaricp
    |play.db.prototype.hikaricp.dataSourceClassName = null
    |play.db.prototype.hikaricp.autocommit = true
    |play.db.prototype.hikaricp.connectionTimeout = 30 seconds
    |play.db.prototype.hikaricp.idleTimeout = 10 minutes
    |play.db.prototype.hikaricp.maxLifetime = 30 minutes
    |play.db.prototype.hikaricp.connectionTestQuery = null
    |play.db.prototype.hikaricp.minimumIdle = null
    |play.db.prototype.hikaricp.maximumPoolSize = 10
    |play.db.prototype.hikaricp.poolName = null
    |play.db.prototype.hikaricp.initializationFailFast = false
    |play.db.prototype.hikaricp.isolateInternalQueries = false
    |play.db.prototype.hikaricp.allowPoolSuspension = false
    |play.db.prototype.hikaricp.readOnly = false
    |play.db.prototype.hikaricp.registerMbeans = false
    |play.db.prototype.hikaricp.catalog = null
    |play.db.prototype.hikaricp.connectionInitSql = null
    |play.db.prototype.hikaricp.transactionIsolation = null
    |play.db.prototype.hikaricp.validationTimeout = 5 seconds
    |play.db.prototype.hikaricp.leakDetectionThreshold = null
  """.stripMargin
}
