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
import com.github.dnvriend.scaffold.play.parsers.Parsers.{ Choice, FieldType, No, Yes }
import com.github.dnvriend.scaffold.play.util.UserInput

import scalaz._
import Scalaz._

final case class ProductUserInput(packageName: String, className: String, fields: List[(String, FieldType)]) extends UserInput {
  def render: String = ProductUserInput.renderProductType(className, fields)
}

object ProductUserInput {
  def askUser(defaultPackageName: String, defaultClassName: String): Disjunction[String, ProductUserInput] = for {
    packageClass <- PackageClassUserInput.askUser(defaultPackageName, defaultClassName)
    fields <- askFields(packageClass.className)
  } yield ProductUserInput(packageClass.packageName, packageClass.className, fields)

  def askField(className: String, fields: List[(String, FieldType)]): Disjunction[String, (String, FieldType, Choice)] = {
    val state = renderProductType(className, fields)
    for {
      fieldName <- UserInput.readLine(Parsers.fieldNameParser, s"$state: Enter field Name > ")
      fieldType <- UserInput.readLine(Parsers.fieldTypeParser, s"$state: Enter field type > ")
      continue <- UserInput.readLine(Parsers.ynParser, s"$state: Another field ? > ")
    } yield (fieldName, fieldType, continue)
  }

  def askFields(className: String): Disjunction[String, List[(String, FieldType)]] = {
    def loop(acc: List[(String, FieldType)]): Disjunction[String, List[(String, FieldType)]] =
      askField(className, acc) match {
        case DRight((fieldName, fieldType, Yes)) =>
          loop(acc :+ (fieldName, fieldType))
        case DRight((fieldName, fieldType, No)) =>
          (acc :+ (fieldName, fieldType)).right[String]
        case err @ DLeft(_) => err
      }
    loop(Nil)
  }

  def renderProductType(className: String, fields: List[(String, FieldType)]): String = {
    def renderField(p: (String, FieldType)): String = s"${p._1}: ${p._2.name}"
    val renderedFields = fields.map(renderField).mkString(", ")
    s"$className($renderedFields)"
  }
}
