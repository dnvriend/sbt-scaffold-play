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

package com.github.dnvriend.scaffold.play.userinput

import com.github.dnvriend.scaffold.play.util.UserInput
import sbt.complete.DefaultParsers

import scalaz._

final case class ControllerUserInput(packageName: String, className: String, resourceName: String)

object ControllerUserInput {
  def askUser(defaultPackageName: String, defaultClassName: String, defaultResourceName: Option[String] = None): Disjunction[String, ControllerUserInput] = for {
    packageClass <- PackageClassUserInput.askUser(defaultPackageName, defaultClassName)
    resourceName <- UserInput.readLine(resourceParser(packageClass.className, defaultResourceName), "Enter REST resource name > ")
  } yield ControllerUserInput(packageClass.packageName, packageClass.className, resourceName)

  def resourceParser(className: String, defaultResourceName: Option[String]) = defaultResourceName.map { resourceName =>
    DefaultParsers.StringBasic.examples(resourceName)
  }.getOrElse(DefaultParsers.StringBasic.examples(s"/api/${className.toLowerCase}"))
}
