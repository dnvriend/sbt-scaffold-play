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

import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.util.UserInput

final case class PackageClassUserInput(packageName: String, className: String)

object PackageClassUserInput {
  def askUser(defaultPackageName: String, defaultClassName: String) = for {
    packageName <- UserInput.readLine[String](Parsers.packageParser(defaultPackageName), "Enter package name > ")
    className <- UserInput.readLine[String](Parsers.classNameParser(defaultClassName), "Enter className > ")
  } yield PackageClassUserInput(packageName, className)
}
