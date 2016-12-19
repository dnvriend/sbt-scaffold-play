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

package com.github.dnvriend.scaffold.play.repository

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.EnablerResult
import com.github.dnvriend.scaffold.play.enabler.all.EveryFeatureEnablerResult
import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.parsers.Parsers._
import com.github.dnvriend.scaffold.play.util.UserInput
import play.api.libs.json.Json

import scalaz._
import Scalaz._

// see: http://stackoverflow.com/questions/17021847/noise-free-json-format-for-sealed-traits-with-play-2-2-library
// see: https://github.com/julienrf/play-json-derived-codecs/releases
object ScaffoldRepository {
  final val EmptyFile = Array.empty[Byte]

  def createScaffoldStateFile(scaffoldStateFile: Path): Disjunction[String, Path] =
    Disjunction.fromTryCatchNonFatal {
      write.over(scaffoldStateFile, EmptyFile)
      scaffoldStateFile
    }.leftMap(_.toString)

  def clear(scaffoldStateFile: Path): Disjunction[String, Unit] = for {
    userInput <- UserInput.readLine(Parsers.yesNoParser, "[clear-scaffold-state]: Are you sure? > ")
    _ <- clearScaffoldState(scaffoldStateFile, userInput)
  } yield ()

  private def clearScaffoldState(scaffoldStateFile: Path, userInput: YesNoChoice): Disjunction[String, Path] = userInput match {
    case Yes => createScaffoldStateFile(scaffoldStateFile)
    case _   => "User canceled clear scaffold state".left[Path]
  }

  def checkExistsElseCreateFile(scaffoldStateFile: Path): Disjunction[String, Unit] = {
    val result: Boolean = exists ! scaffoldStateFile
    if (!result) createScaffoldStateFile(scaffoldStateFile).map(_ => ())
    else ().right[String]
  }

  def saveEnabled(scaffoldStateFile: Path, enablerResult: EnablerResult): Disjunction[String, Path] = for {
    _ <- checkExistsElseCreateFile(scaffoldStateFile)
    path <- Disjunction.fromTryCatchNonFatal {
      enablerResult match {
        case EveryFeatureEnablerResult(xs) => xs.foreach(saveEnabledRecord(scaffoldStateFile, _))
        case result                        => saveEnabledRecord(scaffoldStateFile, result)
      }
      scaffoldStateFile
    }.leftMap(_.toString)
  } yield path

  private def saveEnabledRecord(scaffoldStateFile: Path, enablerResult: EnablerResult) = {
    write.append(scaffoldStateFile, Json.toJson(enablerResult).toString + "\n")
    scaffoldStateFile
  }

  def getEnabled(scaffoldStateFile: Path): List[EnablerResult] = {
    checkExistsElseCreateFile(scaffoldStateFile)
    val xs: Vector[String] = read.lines ! scaffoldStateFile
    xs.map(Json.parse).map(_.as[EnablerResult]).toList
  }
}
