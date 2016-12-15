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

import sbt.complete.{ DefaultParsers, Parser }

object Parsers {
  def packageParser(defaults: String*): Parser[String] =
    DefaultParsers.token(DefaultParsers.any.*.map(_.mkString), "You can use any character.")
      .examples(defaults: _*)

  def classNameParser(defaults: String*): Parser[String] =
    DefaultParsers.token(DefaultParsers.any.*.map(_.mkString), "You can use any character.")
      .examples(defaults: _*)
}
