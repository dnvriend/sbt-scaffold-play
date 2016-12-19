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

package com.github.dnvriend.scaffold.play.util

import org.scalatest.{ FlatSpec, Matchers }
import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.EnablerResult
import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnablerResult
import play.api.libs.json.{ JsValue, Json }

class PathFormatTest extends FlatSpec with Matchers with PathFormat {
  it should "serialize" in {
    Json.toJson(Path("/")).toString shouldBe """{"file":"/"}"""
  }

  it should "deserialize" in {
    val json: JsValue = Json.parse("""{"file":"/"}""")
    json.as[Path]
  }

  def serialize(enablerResult: EnablerResult): String =
    Json.toJson(enablerResult).toString

  it should "parse AnormEnablerResult" in {
    serialize(AnormEnablerResult(Path("/"), Path("/"), Path("/"))) shouldBe """{"class":"AnormEnablerResult","object":{"setting":{"file":"/"},"config":{"file":"/"},"createdModule":{"file":"/"}}}"""
    val json = """{"class":"AnormEnablerResult","object":{"setting":{"file":"/"},"config":{"file":"/"},"createdModule":{"file":"/"}}}"""
    Json.parse(json).as[EnablerResult] shouldBe AnormEnablerResult(Path("/"), Path("/"), Path("/"))
  }
}
