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

import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnablerResult
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnablerResult
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object EnablerResult {
  val reads: Reads[EnablerResult] =
    ((JsPath \ "class").read[String] and (JsPath \ "object").read[JsValue])((str, js) => str match {
      case "AnormEnablerResult" =>
        js.as[AnormEnablerResult](AnormEnablerResult.format)
      case "SlickEnablerResult" =>
        js.as[SlickEnablerResult](SlickEnablerResult.format)
    })

  val writes: Writes[EnablerResult] = new Writes[EnablerResult] {
    override def writes(result: EnablerResult): JsValue = {
      val json: JsValue = result match {
        case x: AnormEnablerResult =>
          Json.toJson[AnormEnablerResult](x)(AnormEnablerResult.format)
        case x: SlickEnablerResult =>
          Json.toJson[SlickEnablerResult](x)(SlickEnablerResult.format)
      }
      Json.obj("class" -> result.getClass.getSimpleName, "object" -> json)
    }
  }

  implicit val format: Format[EnablerResult] = Format(reads, writes)
}

trait EnablerResult
