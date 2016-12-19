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

package com.github.dnvriend.scaffold.play.enabler.docker

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.{ FileUtils, PathFormat }
import play.api.libs.json.{ Format, Json }

import scalaz.Disjunction

object DockerEnablerResult extends PathFormat {
  implicit val format: Format[DockerEnablerResult] = Json.format[DockerEnablerResult]
}

final case class DockerEnablerResult(dockerCompose: Path) extends EnablerResult

object DockerEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    dockerCompose <- createDockerCompose(ctx.baseDir)
  } yield DockerEnablerResult(dockerCompose)

  def createDockerCompose(baseDir: Path): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "docker-compose.yml", Template.dockerCompose)
}

object Template {
  val dockerCompose: String =
    """
    |version: '2'
    |
    |services:
    |  zookeeper:
    |    image: wurstmeister/zookeeper
    |    restart: always
    |    ports:
    |      - "2181:2181"
    |
    |  kafka:
    |    image: wurstmeister/kafka
    |    restart: always
    |    ports:
    |      - "9092:9092"
    |    environment:
    |      - "KAFKA_ADVERTISED_HOST_NAME=localhost"
    |      - "KAFKA_CREATE_TOPICS=test:1:1"
    |      - "KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181"
    |    volumes:
    |      - "/var/run/docker.sock:/var/run/docker.sock"
    |
    |  cassandra:
    |    image: cassandra:latest
    |    restart: always
    |    ports:
    |      - "9042:9042"
    |
    |  postgres:
    |    image: postgres:latest
    |    restart: always
    |    ports:
    |      - "5432:5432"
    |    environment:
    |      - "POSTGRES_DB=postgres"
    |      - "POSTGRES_USER=postgres"
    |      - "POSTGRES_PASSWORD=postgres"
    |    volumes:
    |      - "./initdb:/docker-entrypoint-initdb.d"
  """.stripMargin
}
