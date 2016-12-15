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

package com.github.dnvriend.scaffold.play.scaffolds.controller

import com.github.dnvriend.scaffold.play.repository.ScaffoldRepository
import com.github.dnvriend.scaffold.play.scaffolds.{ Scaffold, ScaffoldContext }
import com.google.inject.Inject
import org.slf4j.{ Logger, LoggerFactory }

object ControllerScaffold {
  final val ID: String = classOf[ControllerScaffold].getName
  final case class UserInput(packageName: String, className: String)
}

class ControllerScaffold @Inject() (repo: ScaffoldRepository) extends Scaffold {
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def execute(ctx: ScaffoldContext): Unit = {
    log.debug("Scaffolding a simple controller")
  }
}
