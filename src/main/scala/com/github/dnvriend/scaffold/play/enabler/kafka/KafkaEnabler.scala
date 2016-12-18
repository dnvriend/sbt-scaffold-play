package com.github.dnvriend.scaffold.play.enabler.kafka

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class KafkaEnablerResult(settings: Path, config: Path) extends EnablerResult

class KafkaEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = ???
}

object Template {
  def settings(authorName: String): String =
    """
      |libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
    """.stripMargin
}