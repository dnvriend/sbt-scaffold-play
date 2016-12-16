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

package com.github.dnvriend.scaffold.play

import com.github.dnvriend.scaffold.play.enabler.EnablerContext
import com.github.dnvriend.scaffold.play.enabler.akka.AkkaEnabler
import com.github.dnvriend.scaffold.play.enabler.all.EveryFeatureEnabler
import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnabler
import com.github.dnvriend.scaffold.play.enabler.buildinfo.BuildInfoEnabler
import com.github.dnvriend.scaffold.play.enabler.conductr.ConductrEnabler
import com.github.dnvriend.scaffold.play.enabler.fp.FpEnabler
import com.github.dnvriend.scaffold.play.enabler.json.JsonEnabler
import com.github.dnvriend.scaffold.play.enabler.logging.LoggingEnabler
import com.github.dnvriend.scaffold.play.enabler.sbtheader.SbtHeaderEnabler
import com.github.dnvriend.scaffold.play.enabler.scalariform.ScalariformEnabler
import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.parsers.Parsers._
import com.github.dnvriend.scaffold.play.scaffolds._
import com.github.dnvriend.scaffold.play.scaffolds.controller.ControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds.dto.DtoScaffold
import com.github.dnvriend.scaffold.play.scaffolds.pingcontroller.PingControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds.wsclient.WsClientScaffold
import sbt.Keys._
import sbt._

import scalaz._

object SbtScaffoldPlay extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val scaffoldBuildInfo: SettingKey[String] = settingKey[String]("The scaffold build info")
    val scaffoldDirectory: SettingKey[File] = settingKey[File]("The scaffold directory containing the play application settings and h2 database")
    val scaffoldSourceDirectory: SettingKey[File] = settingKey[File]("The compile source directory that scaffold will use to generate files")
    val scaffoldTestDirectory: SettingKey[File] = settingKey[File]("The test source directory that scaffold will use to generate files")
    val scaffoldResourceDirectory: SettingKey[File] = settingKey[File]("The compile resource directory that scaffold will use to generate/append configuration")
    val scaffoldDb: SettingKey[File] = settingKey[File]("The scaffold database location")
    val enable: InputKey[Unit] = inputKey[Unit]("enables features in play")
    val scaffold: InputKey[Unit] = inputKey[Unit]("scaffold features in play")
    val scaffoldContext: TaskKey[ScaffoldContext] = taskKey[ScaffoldContext]("Creates the scaffold context")
    val enablerContext: TaskKey[EnablerContext] = taskKey[EnablerContext]("Creates the enabler context")
  }

  import autoImport._

  lazy val defaultSettings: Seq[Setting[_]] = Seq(
    scaffoldDirectory := {
      val dir = baseDirectory.value / ".scaffold"
      IO.createDirectory(dir)
      dir
    },

    scaffoldDb := scaffoldDirectory.value / "scaffold.h2",

    scaffoldSourceDirectory := (sourceDirectories in Compile).value.find(file => file.absolutePath.endsWith("scala") || file.absolutePath.endsWith("app")).getOrElse((sourceDirectory in Compile).value),

    scaffoldTestDirectory := (sourceDirectories in Test).value.find(file => file.absolutePath.endsWith("scala") || file.absolutePath.endsWith("app")).getOrElse((sourceDirectory in Test).value),

    scaffoldResourceDirectory := (resourceDirectory in Compile).value,

    scaffoldBuildInfo := BuildInfo.toString,

    enablerContext := {
      val baseDir: File = baseDirectory.value
      val srcDir: File = scaffoldSourceDirectory.value
      val resourceDir: File = scaffoldResourceDirectory.value
      val testDir: File = scaffoldTestDirectory.value
      val organizationName: String = organization.value
      val projectName: String = name.value
      EnablerContext(ammonite.ops.Path(baseDir), ammonite.ops.Path(srcDir), ammonite.ops.Path(resourceDir), ammonite.ops.Path(testDir), organizationName, projectName)
    },

    enable := {
      val ctx = enablerContext.value
      implicit val log: Logger = streams.value.log
      val choice = Parsers.enablerParser.parsed
      val enablerResult = choice match {
        case AllEnablerChoice =>
          new EveryFeatureEnabler().execute(ctx)
        case AnormEnablerChoice =>
          new AnormEnabler().execute(ctx)
        case AkkaEnablerChoice =>
          new AkkaEnabler().execute(ctx)
        case BuildInfoEnablerChoice =>
          new BuildInfoEnabler().execute(ctx)
        case ConductrEnablerChoice =>
          new ConductrEnabler().execute(ctx)
        case FpEnablerChoice =>
          new FpEnabler().execute(ctx)
        case JsonEnablerChoice =>
          new JsonEnabler().execute(ctx)
        case LoggerEnablerChoice =>
          new LoggingEnabler().execute(ctx)
        case ScalariformEnablerChoice =>
          new ScalariformEnabler().execute(ctx)
        case SbtHeaderEnablerChoice =>
          new SbtHeaderEnabler().execute(ctx)
      }

      enablerResult match {
        case DRight(_) =>
          log.info("Enable complete")
        case DLeft(message) =>
          log.warn(s"Oops, could not enable due to: $message")
      }
    },

    scaffoldContext := {
      val baseDir = baseDirectory.value
      val srcDir = scaffoldSourceDirectory.value
      val resourceDir = scaffoldResourceDirectory.value
      val testDir = scaffoldTestDirectory.value
      val organizationName: String = organization.value
      val projectName: String = name.value
      ScaffoldContext(ammonite.ops.Path(baseDir), ammonite.ops.Path(srcDir), ammonite.ops.Path(resourceDir), ammonite.ops.Path(testDir), organizationName, projectName)
    },

    scaffold := {
      val ctx = scaffoldContext.value
      val log: Logger = streams.value.log
      val choice: ScaffoldChoice = Parsers.scaffoldParser.parsed
      val scaffoldResult = choice match {
        case ControllerChoice =>
          new ControllerScaffold().execute(ctx)
        case PingControllerChoice =>
          new PingControllerScaffold().execute(ctx)
        case WsClientChoice =>
          new WsClientScaffold().execute(ctx)
        case DtoChoice =>
          new DtoScaffold().execute(ctx)
      }

      scaffoldResult match {
        case DRight(_) =>
          log.info("Scaffold complete")
        case DLeft(message) =>
          log.warn(s"Oops, could not scaffold due to: $message")
      }
    }
  )

  override def projectSettings: Seq[Setting[_]] =
    defaultSettings
}
