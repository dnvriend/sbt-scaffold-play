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

package com.github.dnvriend.scaffold.play.parsers

import sbt.complete.DefaultParsers._
import sbt.complete.{ DefaultParsers, Parser }

object Parsers {
  def packageParser(defaults: String*): Parser[String] = {
    val lowercaseChar = DefaultParsers.charClass(c => c.isLetter && c.isLower, "lower case character")
    val lowercaseCharOrdot = DefaultParsers.charClass(c => (c.isLetter && c.isLower) || c == '.', "lowercase letter or dot")
    (lowercaseChar.+ ~ lowercaseCharOrdot.+.? <~ EOF).map {
      case (xs, ys) => (xs ++ ys.getOrElse(Seq.empty[Char])).mkString
    }.examples(defaults: _*)
  }

  def classNameParser(defaults: String*): Parser[String] = {
    val upperCase = DefaultParsers.charClass(c => c.isLetter && c.isUpper, "upper case character")
    (upperCase.+ ~ StringBasic <~ EOF).map {
      case (xs, str) => xs.mkString + str
    }
  }.examples(defaults: _*)
}
