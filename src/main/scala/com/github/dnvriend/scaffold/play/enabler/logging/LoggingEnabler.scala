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

package com.github.dnvriend.scaffold.play.enabler.logging

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class LoggingEnablerResult(logback: Path) extends EnablerResult

class LoggingEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    logback <- createLogback(ctx.resourceDir, Template.logback(ctx.organization))
  } yield LoggingEnablerResult(logback)

  def createLogback(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "logback.xml", content)
}

object Template {
  def logback(organization: String): String = {
    <configuration debug="false">
      <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
          <level>debug</level>
        </filter>
        <encoder>
          <pattern>%date {{ISO8601}} - %logger -> %-5level[%thread] %logger {{0}} - %msg%n</pattern>
        </encoder>
      </appender>
      <logger name="akka" level="info"/>
      <logger name="slick.backend.DatabaseComponent.action" level="debug"/>
      <logger name="com.zaxxer.hikari.pool.HikariPool" level="debug"/>
      <logger name="org.jdbcdslog.ConnectionLogger" level="debug"/>
      <logger name="org.jdbcdslog.StatementLogger" level="debug"/>
      <logger name="org.jdbcdslog.ResultSetLogger" level="debug"/>
      <logger name={ organization } level="debug"/>
      <root level="error">
        <appender-ref ref="console"/>
      </root>
    </configuration>
  }.toString
}
