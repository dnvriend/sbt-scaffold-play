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

package com.github.dnvriend.scaffold.play.scaffolds.crudcontroller

import com.github.dnvriend.scaffold.play.parsers.Parsers.{ IntType, StringType }
import com.github.dnvriend.scaffold.play.userinput.EntityUserInput
import org.scalatest.{ FlatSpec, Matchers }

class CrudControllerTemplateTest extends FlatSpec with Matchers {
  it should "render evolution" in {
    val entity = EntityUserInput("Person", List(("name", StringType), ("age", IntType)))
    println(Template.evolution(entity))
  }

  it should "render " in {
    val entity = EntityUserInput("Person", List(("name", StringType), ("age", IntType)))
    println(Template.repository("com.github.dnvriend", entity))
  }
}
