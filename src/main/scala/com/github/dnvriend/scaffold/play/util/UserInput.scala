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

import com.github.dnvriend.scaffold.play.util.ParserOps._
import sbt._
import sbt.complete._
import scalaz._
import Scalaz._

object UserInput {
  def parse[A](line: String, parser: Parser[A]): ValidationNel[String, A] =
    parser.parse(line)

  def reader[A](parser: Parser[A]): FullReader =
    new sbt.FullReader(None, parser)

  def readLine[A](parser: Parser[A], prompt: String = "> "): Disjunction[String, A] = for {
    line <- reader(parser).readLine(prompt).toRightDisjunction("Could not parse user input")
    result <- parse(line, parser).disjunction.leftMap(_.toList.mkString(","))
  } yield result
}
