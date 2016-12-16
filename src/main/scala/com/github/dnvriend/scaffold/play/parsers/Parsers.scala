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
import scalaz._
import Scalaz._

object Parsers {
  sealed trait Choice
  sealed trait FieldType extends Choice {
    def name: String
  }
  case object BooleanType extends FieldType { override def name: String = "Bool" }
  case object ByteType extends FieldType { override def name: String = "Byte" }
  case object CharType extends FieldType { override def name: String = "Char" }
  case object ShortType extends FieldType { override def name: String = "Short" }
  case object IntType extends FieldType { override def name: String = "Int" }
  case object LongType extends FieldType { override def name: String = "Long" }
  case object FloatType extends FieldType { override def name: String = "Float" }
  case object DoubleType extends FieldType { override def name: String = "Double" }
  case object StringType extends FieldType { override def name: String = "String" }
  case object StringOptionType extends FieldType { override def name: String = "Option[String]" }
  case object LongOptionType extends FieldType { override def name: String = "Option[Long]" }

  case object Yes extends Choice
  case object No extends Choice

  case object End extends Choice

  // scaffolds
  trait ScaffoldChoice extends Choice
  case object ControllerChoice extends ScaffoldChoice
  case object PingControllerChoice extends ScaffoldChoice
  case object WsClientChoice extends ScaffoldChoice
  case object DtoChoice extends ScaffoldChoice

  sealed trait EnablerChoice extends Choice
  case object BuildInfoEnablerChoice extends EnablerChoice
  case object ScalariformEnablerChoice extends EnablerChoice

  val enablerParser: Parser[EnablerChoice] = {
    val buildinfo: Parser[EnablerChoice] = "buildinfo" ^^^ BuildInfoEnablerChoice
    val scalariform: Parser[EnablerChoice] = "scalariform" ^^^ ScalariformEnablerChoice

    DefaultParsers.token(Space ~> (buildinfo | scalariform))
  }

  val scaffoldParser: Parser[ScaffoldChoice] = {
    val controller: Parser[ScaffoldChoice] = "controller" ^^^ ControllerChoice
    val pingcontroller: Parser[ScaffoldChoice] = "pingcontroller" ^^^ PingControllerChoice
    val wsclient: Parser[ScaffoldChoice] = "wsclient" ^^^ WsClientChoice
    val dto: Parser[ScaffoldChoice] = "dto" ^^^ DtoChoice

    DefaultParsers.token(Space ~> (controller | pingcontroller | wsclient | dto))
  }

  def packageParser(defaults: String*): Parser[String] = StringBasic.examples(defaults: _*)

  def classNameParser(defaults: String*): Parser[String] = {
    val upperCase = DefaultParsers.charClass(c => c.isLetter && c.isUpper, "upper case character")
    (upperCase.+ ~ StringBasic <~ EOF).map {
      case (xs, str) => xs.mkString + str
    }
  }.examples(defaults: _*)

  val ynParser: Parser[Choice] = {
    val yParser: Parser[Choice] = ("y" | "yes") ^^^ Yes
    val nParser: Parser[Choice] = ("n" | "no" | "exit" | "stop" | "quit" | "end" | "break") ^^^ No

    yParser | nParser
  }

  val fieldNameParser: Parser[String] = {
    val lowercaseChar = charClass(c => c.isLetter && c.isLower, "lower case character")
    (lowercaseChar.+.map(_.mkString) ~ DefaultParsers.StringBasic).map {
      case (lower, all) => lower + all
    }
  }

  val higherKindedParser: Parser[String] = {
    val future: Parser[String] = "future"
    val option: Parser[String] = "option" | "opt"
    val either: Parser[String] = "either"
    val disjunction: Parser[String] = "disjunction" | "dis" | "d"
    val disjunctionNel: Parser[String] = "disjunctionNel" | "disNel" | "dnel"
    val validation: Parser[String] = "validation" | "val"
    val validationNel: Parser[String] = "validationNel" | "valNel"
    (future | option | either | disjunction | disjunctionNel | validation | validationNel) <~ EOF
  }

  val fieldTypeParser: Parser[FieldType] = {
    val boolean: Parser[FieldType] = ("bool" | "boolean") ^^^ BooleanType
    val byte: Parser[FieldType] = "byte" ^^^ ByteType
    val char: Parser[FieldType] = "char" ^^^ CharType
    val short: Parser[FieldType] = "short" ^^^ ShortType
    val int: Parser[FieldType] = "int" ^^^ IntType
    val long: Parser[FieldType] = "long" ^^^ LongType
    val float: Parser[FieldType] = "float" ^^^ FloatType
    val double: Parser[FieldType] = "double" ^^^ DoubleType
    val str: Parser[FieldType] = ("string" | "str") ^^^ StringType
    val strOpt: Parser[FieldType] = "strOpt" ^^^ StringOptionType
    val longOpt: Parser[FieldType] = "longOpt" ^^^ LongOptionType
    (boolean | byte | char | short | int | long | float | double | str | strOpt | longOpt) <~ EOF
  }

  val endParser: Parser[Choice] = (("exit" | "stop" | "quit" | "end" | "break") <~ EOF) ^^^ End
}
