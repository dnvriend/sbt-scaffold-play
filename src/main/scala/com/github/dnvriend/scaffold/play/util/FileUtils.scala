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

import ammonite.ops._

import scalaz.Disjunction
import com.github.dnvriend.scaffold.play.util.DisjunctionOps.DisjunctionOfThrowableToDisjunctionOfString

object FileUtils {
  def dotToSlash(srcDir: Path, packageName: String, className: String): Disjunction[String, Path] =
    Disjunction.fromTryCatchNonFatal {
      val packageNameAsPath = RelPath(packageName.replace(".", "/"))
      srcDir / packageNameAsPath / s"${className}.scala"
    }

  def createClass(srcDir: Path, packageName: String, className: String, content: String): Disjunction[String, Path] = for {
    path <- dotToSlash(srcDir, packageName, className)
    writtenFile <- writeFile(path, content)
  } yield writtenFile

  def writeFile(path: Path, content: String): Disjunction[String, Path] =
    Disjunction.fromTryCatchNonFatal(writeToFile(path, content))

  private def writeToFile(path: Path, content: String): Path = {
    write.over(path, content)
    path
  }

  private def appendToFile(path: Path, content: String): Path = {
    write.append(path, content + "\n")
    path
  }

  def append(path: Path, content: String): Disjunction[String, Path] =
    Disjunction.fromTryCatchNonFatal(appendToFile(path, content))

  def appendToRoutes(resourceDir: Path, content: String): Disjunction[String, Path] =
    append(resourceDir / "routes", content)

  def appendToApplication(resourceDir: Path, content: String): Disjunction[String, Path] =
    append(resourceDir / "application.conf", content)

  private def makeDir(path: Path): Path = {
    mkdir(path)
    path
  }

  def createDirectory(path: Path): Disjunction[String, Path] =
    Disjunction.fromTryCatchNonFatal(makeDir(path))
}
