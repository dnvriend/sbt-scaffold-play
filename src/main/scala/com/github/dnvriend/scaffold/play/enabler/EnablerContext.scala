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

package com.github.dnvriend.scaffold.play.enabler

import ammonite.ops._
import sbt.Logger

class EnablerContext(
  val log: Logger,
  val baseDir: Path,
  val srcDir: Path,
  val resourceDir: Path,
  val testDir: Path,
  val organization: String,
  val projectName: String,
  val enabled: List[EnablerResult],
  val akkaVersion: String = "2.4.12",
  val hikariCpVersion: String = "2.5.1",
  val h2Version: String = "1.4.193",
  val postgresVersion: String = "9.4.1212",
  val anormVersion: String = "2.5.2",
  val swaggerVersion: String = "1.5.3",
  val akkaStreamKafkaVersion: String = "0.13",
  val buildInfoVersion: String = "0.6.1",
  val conductrVersion: String = "2.1.20",
  val sbtHeaderVersion: String = "1.5.1",
  val scalariformVersion: String = "1.6.0",
  val sparkVersion: String = "2.0.2",
  val scalazVersion: String = "7.2.7",
  val scalazScalaTestversion: String = "1.1.0",
  val shapelessVersion: String = "2.3.2",
  val nscalaTimeVersion: String = "2.14.0",
  val playJsonVersion: String = "2.5.10",
  val lagomVersion: String = "1.3.0-M1",
  val slickVersion: String = "3.1.1",
  val playSlickVersion: String = "2.0.2"
)
