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

package com.github.dnvriend.scaffold.play.enabler.akka

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz.Scalaz._
import scalaz._

object AkkaEnablerResult extends PathFormat {
  implicit val format: Format[AkkaEnablerResult] = Json.format[AkkaEnablerResult]
}

final case class AkkaEnablerResult(setting: Path, config: Path) extends EnablerResult

object AkkaEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    settings <- createSettings(ctx.baseDir, Template.settings(ctx))
    config <- createConfig(ctx.resourceDir)
    _ <- addConfig(ctx.resourceDir)
  } yield AkkaEnablerResult(settings, config)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: AkkaEnablerResult => "Akka already enabled".left[Unit]
    case _                    => ().right[String]
  }.sequenceU

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-akka.sbt", content)

  def createConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "akka.conf", Template.config)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "akka"""")
}

object Template {
  def settings(ctx: EnablerContext): String =
    s"""
      |libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "${ctx.akkaVersion}"
      |libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "${ctx.akkaVersion}"
      |libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "${ctx.akkaVersion}"
      |libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "${ctx.akkaVersion}"
      |libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query-experimental" % "${ctx.akkaVersion}"
    """.stripMargin

  val config: String =
    """
      |akka {
      | loggers = ["akka.event.slf4j.Slf4jLogger"]
      |  loglevel = debug
      |  stdout-loglevel = info
      |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      |
      |actor {
      |    serialize-messages = off // when on, the akka framework will determine whether or not messages can be serialized, else the plugin
      |
      |    debug {
      |      receive = on // log all messages sent to an actor if that actors receive method is a LoggingReceive
      |      autoreceive = off // log all special messages like Kill, PoisoffPill etc sent to all actors
      |      lifecycle = off // log all actor lifecycle events of all actors
      |      fsm = off // enable logging of all events, transitioffs and timers of FSM Actors that extend LoggingFSM
      |      event-stream = off // enable logging of subscriptions (subscribe/unsubscribe) on the ActorSystem.eventStream
      |    }
      |  }
      |}
    """.stripMargin
}
