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

import com.github.dnvriend.scaffold.play.com.github.dnvriend.TestSpec
import OptionOps._
import scalaz._
import Scalaz._

class OptionOpsTest extends TestSpec {
  it should "xor" in {
    "str".some xor "str".some should not be defined
    "str".some xor none[String] shouldBe "str".some
    none[String] xor "str".some shouldBe "str".some
    none[String] xor none[String] should not be defined
  }
}
