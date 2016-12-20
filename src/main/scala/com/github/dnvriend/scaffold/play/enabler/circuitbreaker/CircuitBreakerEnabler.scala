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

package com.github.dnvriend.scaffold.play.enabler.circuitbreaker

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz._
import Scalaz._

object CircuitBreakerEnablerResult extends PathFormat {
  implicit val format: Format[CircuitBreakerEnablerResult] = Json.format[CircuitBreakerEnablerResult]
}

final case class CircuitBreakerEnablerResult(config: Path) extends EnablerResult

object CircuitBreakerEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    config <- createConfig(ctx.resourceDir)
    createdModule <- createModule(ctx.srcDir, "play.modules.cb", "CircuitBreakerModule")
    _ <- addConfig(ctx.resourceDir)
  } yield CircuitBreakerEnablerResult(config)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: CircuitBreakerEnablerResult => "CircuitBreaker already enabled".left[Unit]
    case _                              => ().right[String]
  }.sequenceU

  def createConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "circuit-breaker.conf", Template.config)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "circuit-breaker"""")

  def createModule(srcDir: Path, packageName: String, className: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, Template.module)
}

object Template {
  val module: String =
    """
      |package play.modules.cb
      |
      |import akka.actor.ActorSystem
      |import akka.pattern.CircuitBreaker
      |import com.google.inject.{ AbstractModule, Provides }
      |
      |import scala.concurrent.ExecutionContext
      |import scala.concurrent.duration._
      |
      |class CircuitBreakerModule extends AbstractModule {
      |  override def configure(): Unit = {
      |    @Provides
      |    def circuitBreakerProvider(system: ActorSystem)(implicit ec: ExecutionContext): CircuitBreaker = {
      |      val maxFailures: Int = 3
      |      val callTimeout: FiniteDuration = 1.seconds
      |      val resetTimeout: FiniteDuration = 10.seconds
      |      new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
      |    }
      |  }
      |}
    """.stripMargin

  val config: String =
    """
      |play.modules.enabled += "play.modules.cb.CircuitBreakerModule"
    """.stripMargin
}
