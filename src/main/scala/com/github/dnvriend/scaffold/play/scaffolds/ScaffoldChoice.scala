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

package com.github.dnvriend.scaffold.play.scaffolds

import sbt._
import sbt.complete.DefaultParsers._
import sbt.complete._

sealed trait ScaffoldChoice
case object ControllerChoice extends ScaffoldChoice
case object PingControllerChoice extends ScaffoldChoice
case object WsClientChoice extends ScaffoldChoice

object ScaffoldChoice {
  val controller = "controller" ^^^ ControllerChoice
  val pingcontroller = "pingcontroller" ^^^ PingControllerChoice
  val wsclient = "wsclient" ^^^ WsClientChoice

  val parser: Parser[ScaffoldChoice] =
    DefaultParsers.token(Space ~> (controller | pingcontroller | wsclient))
}