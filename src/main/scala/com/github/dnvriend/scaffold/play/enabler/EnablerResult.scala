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

package com.github.dnvriend.scaffold.play.enabler

import com.github.dnvriend.scaffold.play.enabler.akka.AkkaEnablerResult
import com.github.dnvriend.scaffold.play.enabler.all.EveryFeatureEnablerResult
import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnablerResult
import com.github.dnvriend.scaffold.play.enabler.buildinfo.BuildInfoEnablerResult
import com.github.dnvriend.scaffold.play.enabler.circuitbreaker.CircuitBreakerEnablerResult
import com.github.dnvriend.scaffold.play.enabler.conductr.ConductrEnablerResult
import com.github.dnvriend.scaffold.play.enabler.docker.DockerEnablerResult
import com.github.dnvriend.scaffold.play.enabler.fp.FpEnablerResult
import com.github.dnvriend.scaffold.play.enabler.json.JsonEnablerResult
import com.github.dnvriend.scaffold.play.enabler.kafka.KafkaEnablerResult
import com.github.dnvriend.scaffold.play.enabler.logging.LoggingEnablerResult
import com.github.dnvriend.scaffold.play.enabler.sbtheader.SbtHeaderEnablerResult
import com.github.dnvriend.scaffold.play.enabler.scalariform.ScalariformEnablerResult
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnablerResult
import com.github.dnvriend.scaffold.play.enabler.spark.SparkEnablerResult
import com.github.dnvriend.scaffold.play.enabler.swagger.SwaggerEnablerResult
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object EnablerResult {
  val reads: Reads[EnablerResult] =
    ((JsPath \ "class").read[String] and (JsPath \ "object").read[JsValue])((str, js) => str match {
      case "AnormEnablerResult"          => js.as[AnormEnablerResult](AnormEnablerResult.format)
      case "SlickEnablerResult"          => js.as[SlickEnablerResult](SlickEnablerResult.format)
      case "AkkaEnablerResult"           => js.as[AkkaEnablerResult](AkkaEnablerResult.format)
      case "BuildInfoEnablerResult"      => js.as[BuildInfoEnablerResult](BuildInfoEnablerResult.format)
      case "CircuitBreakerEnablerResult" => js.as[CircuitBreakerEnablerResult](CircuitBreakerEnablerResult.format)
      case "ConductrEnablerResult"       => js.as[ConductrEnablerResult](ConductrEnablerResult.format)
      case "DockerEnablerResult"         => js.as[DockerEnablerResult](DockerEnablerResult.format)
      case "FpEnablerResult"             => js.as[FpEnablerResult](FpEnablerResult.format)
      case "JsonEnablerResult"           => js.as[JsonEnablerResult](JsonEnablerResult.format)
      case "LoggingEnablerResult"        => js.as[LoggingEnablerResult](LoggingEnablerResult.format)
      case "KafkaEnablerResult"          => js.as[KafkaEnablerResult](KafkaEnablerResult.format)
      case "ScalariformEnablerResult"    => js.as[ScalariformEnablerResult](ScalariformEnablerResult.format)
      case "SbtHeaderEnablerResult"      => js.as[SbtHeaderEnablerResult](SbtHeaderEnablerResult.format)
      case "SwaggerEnablerResult"        => js.as[SwaggerEnablerResult](SwaggerEnablerResult.format)
      case "SparkEnablerResult"          => js.as[SparkEnablerResult](SparkEnablerResult.format)
    })

  val writes: Writes[EnablerResult] = new Writes[EnablerResult] {
    override def writes(result: EnablerResult): JsValue = {
      val json: JsValue = result match {
        case x: AnormEnablerResult          => Json.toJson(x)(AnormEnablerResult.format)
        case x: SlickEnablerResult          => Json.toJson(x)(SlickEnablerResult.format)
        case x: AkkaEnablerResult           => Json.toJson(x)(AkkaEnablerResult.format)
        case x: BuildInfoEnablerResult      => Json.toJson(x)(BuildInfoEnablerResult.format)
        case x: CircuitBreakerEnablerResult => Json.toJson(x)(CircuitBreakerEnablerResult.format)
        case x: ConductrEnablerResult       => Json.toJson(x)(ConductrEnablerResult.format)
        case x: DockerEnablerResult         => Json.toJson(x)(DockerEnablerResult.format)
        case x: FpEnablerResult             => Json.toJson(x)(FpEnablerResult.format)
        case x: JsonEnablerResult           => Json.toJson(x)(JsonEnablerResult.format)
        case x: LoggingEnablerResult        => Json.toJson(x)(LoggingEnablerResult.format)
        case x: KafkaEnablerResult          => Json.toJson(x)(KafkaEnablerResult.format)
        case x: ScalariformEnablerResult    => Json.toJson(x)(ScalariformEnablerResult.format)
        case x: SbtHeaderEnablerResult      => Json.toJson(x)(SbtHeaderEnablerResult.format)
        case x: SwaggerEnablerResult        => Json.toJson(x)(SwaggerEnablerResult.format)
        case x: SparkEnablerResult          => Json.toJson(x)(SparkEnablerResult.format)
      }
      Json.obj("class" -> result.getClass.getSimpleName, "object" -> json)
    }
  }

  implicit val format: Format[EnablerResult] = Format(reads, writes)
}

trait EnablerResult
