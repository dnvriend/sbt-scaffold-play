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

package com.github.dnvriend.scaffold.play.scaffolds.dto

import ammonite.ops.Path
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext }
import com.github.dnvriend.scaffold.play.userinput.{ PackageClassUserInput, ProductUserInput }
import sbt.Logger

import scalaz._
import Scalaz._

object DtoScaffold {
  final val ID: String = classOf[DtoScaffold].getName
  final val DefaultClassName = "DefaultDto"
}

class DtoScaffold(implicit log: Logger) extends Scaffold {
  override def execute(ctx: ScaffoldContext): Unit = {
    val maybeResult: Disjunction[String, ProductUserInput] = for {
      input <- ProductUserInput.askUser(ctx.organization, DtoScaffold.DefaultClassName)
    } yield input

    maybeResult match {
      case DRight(created) =>
        log.info(s"Successfully created: $created")
      case DLeft(message) =>
        log.warn(s"Could not scaffold a web service client: $message")
    }
  }
}
