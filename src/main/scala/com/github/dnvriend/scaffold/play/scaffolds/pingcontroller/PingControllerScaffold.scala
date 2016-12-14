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

package com.github.dnvriend.scaffold.play.scaffolds.pingcontroller

import com.github.dnvriend.scaffold.play.repository.ScaffoldRepository
import com.github.dnvriend.scaffold.play.scaffolds.Scaffold
import com.google.inject.Inject
import org.slf4j.LoggerFactory
import sbt.File

class PingControllerScaffold @Inject() (repo: ScaffoldRepository) extends Scaffold {
  val log = LoggerFactory.getLogger(this.getClass)

  override def execute(baseDirectory: File): Unit = {
    log.debug("scaffolding a ping controller")
  }
}
