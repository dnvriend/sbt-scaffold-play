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

import com.github.dnvriend.scaffold.play.enabler.buildinfo.BuildInfoEnabler
import com.github.dnvriend.scaffold.play.enabler.{ BuildInfoEnablerChoice, EnablerChoice, EnablerContext }
import com.github.dnvriend.scaffold.play.scaffolds.wsclient.WsClientScaffold
import com.github.dnvriend.scaffold.play.scaffolds.pingcontroller.PingControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds._
import com.github.dnvriend.scaffold.play.scaffolds.controller.ControllerScaffold
import com.github.dnvriend.scaffold.play.util.GuiceUtil
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Environment }
import sbt.Keys._
import sbt._

object SbtScaffoldPlay extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {
    val scaffoldBuildInfo: SettingKey[String] = settingKey[String]("The scaffold build info")
    val scaffoldDirectory: SettingKey[File] = settingKey[File]("The scaffold directory containing the play application settings and h2 database")
    val scaffoldDb: SettingKey[File] = settingKey[File]("The scaffold database location")
    val scaffoldActorSystemName: SettingKey[String] = settingKey[String]("The scaffold actor system name")
    val scaffoldConfigurationFile: SettingKey[File] = settingKey[File]("The scaffold configuration file")
    val scaffoldApplication: SettingKey[Application] = settingKey[Application]("The scaffold play application")
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

    scaffoldActorSystemName := "scaffold-system",

    scaffoldConfigurationFile := {
      val scaffoldCfg: File = scaffoldDirectory.value / "application.conf"
      val jdbcUrl = "jdbc:h2:" + scaffoldDb.value.absolutePath
      val actorSystemName = scaffoldActorSystemName.value
      if (!scaffoldCfg.exists()) {
        IO.write(
          scaffoldCfg,
          s"""
          |play.akka.actor-system = "$actorSystemName"
          |db.default.driver=org.h2.Driver
          |db.default.url="$jdbcUrl"
          |play.modules.enabled += "play.api.db.DBModule"
          |play.modules.enabled += "play.api.db.HikariCPModule"
          |play.modules.enabled += "com.github.dnvriend.scaffold.play.ScaffoldModule"
          |
        """.stripMargin
        )
      }
      scaffoldCfg
    },

    scaffoldApplication := {
      val environment = Environment.simple(scaffoldDirectory.value)
      new GuiceApplicationBuilder(
        environment = environment,
        configuration = play.api.Configuration.load(environment, Map("config.file" -> scaffoldConfigurationFile.value.absolutePath))
      ).build()
    },

    scaffoldBuildInfo := BuildInfo.toString,

    enablerContext := {
      val baseDir = baseDirectory.value
      val srcDir = (sourceDirectory in Compile).value
      val testDir = (sourceDirectory in Test).value
      EnablerContext(baseDir, srcDir, testDir, organization.value)
    },

    enable := {
      val ctx = enablerContext.value
      implicit val app = scaffoldApplication.value
      val choice = EnablerChoice.parser.parsed
      choice match {
        case BuildInfoEnablerChoice =>
          GuiceUtil.get[BuildInfoEnabler].execute(ctx)
      }
    },

    scaffoldContext := {
      val baseDir = baseDirectory.value
      val srcDir = (sourceDirectory in Compile).value
      val testDir = (sourceDirectory in Test).value
      ScaffoldContext(ammonite.ops.Path(baseDir), ammonite.ops.Path(srcDir), ammonite.ops.Path(testDir), organization.value)
    },

    scaffold := {
      val ctx = scaffoldContext.value
      implicit val app = scaffoldApplication.value
      val choice = ScaffoldChoice.parser.parsed
      choice match {
        case ControllerChoice =>
          GuiceUtil.get[ControllerScaffold].execute(ctx)
        case PingControllerChoice =>
          GuiceUtil.get[PingControllerScaffold].execute(ctx)
        case WsClientChoice =>
          GuiceUtil.get[WsClientScaffold].execute(ctx)
      }
    }
  )

  override def projectSettings: Seq[Setting[_]] =
    defaultSettings
}
