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

package com.github.dnvriend.scaffold.play.enabler.kafka

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz._
import Scalaz._

object KafkaEnablerResult extends PathFormat {
  implicit val format: Format[KafkaEnablerResult] = Json.format[KafkaEnablerResult]
}

final case class KafkaEnablerResult(settings: Path, producerConfig: Path, consumerConfig: Path) extends EnablerResult

object KafkaEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    _ <- check(ctx.enabled)
    settings <- createSettings(ctx.baseDir, Template.settings(ctx))
    producerConfig <- createProducerConfig(ctx.resourceDir)
    consumerConfig <- createConsumerConfig(ctx.resourceDir)
    _ <- addProducerConfig(ctx.resourceDir)
    _ <- addConsumerConfig(ctx.resourceDir)
  } yield KafkaEnablerResult(settings, producerConfig, consumerConfig)

  def check(enabled: List[EnablerResult]): Disjunction[String, List[Unit]] = enabled.collect {
    case x: KafkaEnablerResult => "Kafka already enabled".left[Unit]
    case _                     => ().right[String]
  }.sequenceU

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-kafka.sbt", content)

  def createProducerConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "kafka-producer.conf", Template.producerConfig)

  def createConsumerConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "kafka-consumer.conf", Template.consumerConfig)

  def addProducerConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "kafka-producer"""")

  def addConsumerConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "kafka-consumer"""")
}

object Template {
  def settings(ctx: EnablerContext): String =
    s"""
      |libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "${ctx.akkaStreamKafkaVersion}"
    """.stripMargin

  val producerConfig: String =
    """
    |# Properties for akka.kafka.ProducerSettings can be
    |# defined in this section or a configuration section with
    |# the same layout.
    |akka.kafka.producer {
    |  # Tuning parameter of how many sends that can run in parallel.
    |  parallelism = 100
    |
    |  # How long to wait for `KafkaProducer.close`
    |  close-timeout = 60s
    |
    |  # Fully qualified config path which holds the dispatcher configuration
    |  # to be used by the producer stages. Some blocking may occur.
    |  # When this value is empty, the dispatcher configured for the stream
    |  # will be used.
    |  use-dispatcher = "akka.kafka.default-dispatcher"
    |
    |  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig
    |  # can be defined in this configuration section.
    |  kafka-clients {
    |  }
    |}
  """.stripMargin

  val consumerConfig: String =
    """
    |# Properties for akka.kafka.ConsumerSettings can be
    |# defined in this section or a configuration section with
    |# the same layout.
    |akka.kafka.consumer {
    |  # Tuning property of scheduled polls.
    |  poll-interval = 50ms
    |
    |  # Tuning property of the `KafkaConsumer.poll` parameter.
    |  # Note that non-zero value means that blocking of the thread that
    |  # is executing the stage will be blocked.
    |  poll-timeout = 50ms
    |
    |  # The stage will be await outstanding offset commit requests before
    |  # shutting down, but if that takes longer than this timeout it will
    |  # stop forcefully.
    |  stop-timeout = 30s
    |
    |  # How long to wait for `KafkaConsumer.close`
    |  close-timeout = 20s
    |
    |  # If offset commit requests are not completed within this timeout
    |  # the returned Future is completed `TimeoutException`.
    |  commit-timeout = 15s
    |
    |  # If the KafkaConsumer can't connect to the broker the poll will be
    |  # aborted after this timeout. The KafkaConsumerActor will throw
    |  # org.apache.kafka.common.errors.WakeupException which will be ignored
    |  # until max-wakeups limit gets exceeded.
    |  wakeup-timeout = 3s
    |
    |  # After exceeding maxinum wakeups the consumer will stop and the stage will fail.
    |  max-wakeups = 10
    |
    |  # Fully qualified config path which holds the dispatcher configuration
    |  # to be used by the KafkaConsumerActor. Some blocking may occur.
    |  use-dispatcher = "akka.kafka.default-dispatcher"
    |
    |  # Properties defined by org.apache.kafka.clients.consumer.ConsumerConfig
    |  # can be defined in this configuration section.
    |  kafka-clients {
    |    # Disable auto-commit by default
    |    enable.auto.commit = false
    |  }
    |}
  """.stripMargin
}
