package com.github.dnvriend.scaffold.play.enabler.akka

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class AkkaEnablerResult(setting: Path, config: Path) extends EnablerResult

class AkkaEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
    config <- createConfig(ctx.resourceDir, Template.config())
    _ <- addConfig(ctx.resourceDir)
  } yield AkkaEnablerResult(settings, config)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-akka.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "akka.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "akka"""")
}

object Template {
  def settings(): String =
    """
      |
    """.stripMargin

  def config(): String =
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
