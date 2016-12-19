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
    def sql: String
  }
  case object BooleanType extends FieldType { val name: String = "Bool"; val sql = "BOOLEAN" }
  case object ByteType extends FieldType { val name: String = "Byte"; val sql = "BIGINT" }
  case object CharType extends FieldType { val name: String = "Char"; val sql: String = "VARCHAR(255)" }
  case object ShortType extends FieldType { val name: String = "Short"; val sql: String = "BIGINT" }
  case object IntType extends FieldType { val name: String = "Int"; val sql: String = "BIGINT" }
  case object LongType extends FieldType { val name: String = "Long"; val sql: String = "BIGINT" }
  case object FloatType extends FieldType { val name: String = "Float"; val sql: String = "REAL" }
  case object DoubleType extends FieldType { val name: String = "Double"; val sql: String = "REAL" }
  case object StringType extends FieldType { val name: String = "String"; val sql: String = "VARCHAR(255)" }
  case object StringOptionType extends FieldType { val name: String = "Option[String]"; val sql: String = "VARCHAR(255)" }
  case object LongOptionType extends FieldType { val name: String = "Option[Long]"; val sql: String = "BIGINT" }

  case object Yes extends Choice
  case object No extends Choice

  case object End extends Choice

  // scaffolds
  trait ScaffoldChoice extends Choice
  case object BuildInfoControllerChoice extends ScaffoldChoice
  case object ControllerChoice extends ScaffoldChoice
  case object CrudControllerChoice extends ScaffoldChoice
  case object PingControllerChoice extends ScaffoldChoice
  case object HealthControllerChoice extends ScaffoldChoice
  case object WsClientChoice extends ScaffoldChoice
  case object DtoChoice extends ScaffoldChoice

  sealed trait EnablerChoice extends Choice
  case object AkkaEnablerChoice extends EnablerChoice
  case object AnormEnablerChoice extends EnablerChoice
  case object BuildInfoEnablerChoice extends EnablerChoice
  case object ConductrEnablerChoice extends EnablerChoice
  case object DockerEnablerChoice extends EnablerChoice
  case object CircuitBreakerEnablerChoice extends EnablerChoice
  case object ScalariformEnablerChoice extends EnablerChoice
  case object KafkaEnablerChoice extends EnablerChoice
  case object LoggerEnablerChoice extends EnablerChoice
  case object JsonEnablerChoice extends EnablerChoice
  case object FpEnablerChoice extends EnablerChoice
  case object SbtHeaderEnablerChoice extends EnablerChoice
  case object SwaggerEnablerChoice extends EnablerChoice
  case object SlickEnablerChoice extends EnablerChoice
  case object SparkEnablerChoice extends EnablerChoice
  case object AllEnablerChoice extends EnablerChoice

  val enablerParser: Parser[EnablerChoice] = {
    val anorm: Parser[EnablerChoice] = "anorm" ^^^ AnormEnablerChoice
    val akka: Parser[EnablerChoice] = "akka" ^^^ AkkaEnablerChoice
    val buildinfo: Parser[EnablerChoice] = "buildinfo" ^^^ BuildInfoEnablerChoice
    val circuitBreaker: Parser[EnablerChoice] = ("circuitbreaker" | "cb") ^^^ CircuitBreakerEnablerChoice
    val conductr: Parser[EnablerChoice] = "conductr" ^^^ ConductrEnablerChoice
    val docker: Parser[EnablerChoice] = "docker" ^^^ DockerEnablerChoice
    val scalariform: Parser[EnablerChoice] = "scalariform" ^^^ ScalariformEnablerChoice
    val kafka: Parser[EnablerChoice] = "kafka" ^^^ KafkaEnablerChoice
    val logback: Parser[EnablerChoice] = ("logback" | "logger" | "log") ^^^ LoggerEnablerChoice
    val json: Parser[EnablerChoice] = ("json" | "play-json") ^^^ JsonEnablerChoice
    val fp: Parser[EnablerChoice] = ("fp" | "scalaz") ^^^ FpEnablerChoice
    val sbtHeader: Parser[EnablerChoice] = ("header" | "sbt-header" | "sbtHeader") ^^^ SbtHeaderEnablerChoice
    val slick: Parser[EnablerChoice] = "slick" ^^^ SlickEnablerChoice
    val swagger: Parser[EnablerChoice] = "swagger" ^^^ SwaggerEnablerChoice
    val spark: Parser[EnablerChoice] = "spark" ^^^ SparkEnablerChoice
    val all: Parser[EnablerChoice] = "all" ^^^ AllEnablerChoice

    DefaultParsers.token(Space ~> (all | akka | anorm | buildinfo | circuitBreaker | conductr | docker | kafka | scalariform | sbtHeader | logback | json | fp | spark | slick | swagger))
  }

  val scaffoldParser: Parser[ScaffoldChoice] = {
    val buildinfo: Parser[ScaffoldChoice] = ("buildinfo" | "buildinfo-controller") ^^^ BuildInfoControllerChoice
    val controller: Parser[ScaffoldChoice] = "controller" ^^^ ControllerChoice
    val crudController: Parser[ScaffoldChoice] = ("crud" | "crud-controller") ^^^ CrudControllerChoice
    val pingcontroller: Parser[ScaffoldChoice] = ("ping" | "ping-controller") ^^^ PingControllerChoice
    val healthcontroller: Parser[ScaffoldChoice] = ("health" | "health-controller") ^^^ HealthControllerChoice
    val wsclient: Parser[ScaffoldChoice] = "wsclient" ^^^ WsClientChoice
    val dto: Parser[ScaffoldChoice] = "dto" ^^^ DtoChoice

    DefaultParsers.token(Space ~> (buildinfo | controller | crudController | healthcontroller | pingcontroller | wsclient | dto))
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
