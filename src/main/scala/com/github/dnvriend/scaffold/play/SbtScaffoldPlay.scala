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
import com.github.dnvriend.scaffold.play.enabler.circuitbreaker.CircuitBreakerEnabler
import com.github.dnvriend.scaffold.play.enabler.conductr.ConductrEnabler
import com.github.dnvriend.scaffold.play.enabler.docker.DockerEnabler
import com.github.dnvriend.scaffold.play.enabler.fp.FpEnabler
import com.github.dnvriend.scaffold.play.enabler.json.JsonEnabler
import com.github.dnvriend.scaffold.play.enabler.kafka.KafkaEnabler
import com.github.dnvriend.scaffold.play.enabler.logging.LoggingEnabler
import com.github.dnvriend.scaffold.play.enabler.sbtheader.SbtHeaderEnabler
import com.github.dnvriend.scaffold.play.enabler.scalariform.ScalariformEnabler
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnabler
import com.github.dnvriend.scaffold.play.enabler.spark.SparkEnabler
import com.github.dnvriend.scaffold.play.enabler.swagger.SwaggerEnabler
import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.parsers.Parsers._
import com.github.dnvriend.scaffold.play.scaffolds._
import com.github.dnvriend.scaffold.play.scaffolds.buildinfo.BuildInfoControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds.controller.ControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds.crudcontroller.CrudControllerScaffold
import com.github.dnvriend.scaffold.play.scaffolds.dto.DtoScaffold
import com.github.dnvriend.scaffold.play.scaffolds.health.HealthControllerScaffold
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

    val scaffoldVersionAkka: SettingKey[String] = settingKey[String]("Enabler Akka version")
    val scaffoldVersionHikariCp: SettingKey[String] = settingKey[String]("Enabler hikariCP version")
    val scaffoldVersionH2: SettingKey[String] = settingKey[String]("Enabler H2 version")
    val scaffoldVersionPostgres: SettingKey[String] = settingKey[String]("Enabler Postgres version")
    val scaffoldVersionAnorm: SettingKey[String] = settingKey[String]("Enabler Anorm version")
    val scaffoldVersionSwagger: SettingKey[String] = settingKey[String]("Enabler Swagger version")
    val scaffoldVersionAkkaStreamKafka: SettingKey[String] = settingKey[String]("Enabler AkkaStreamKafka version")
    val scaffoldVersionBuildInfo: SettingKey[String] = settingKey[String]("Enabler BuildInfo version")
    val scaffoldVersionConductr: SettingKey[String] = settingKey[String]("Enabler Conductr version")
    val scaffoldVersionSbtHeader: SettingKey[String] = settingKey[String]("Enabler SbtHeader version")
    val scaffoldVersionScalariform: SettingKey[String] = settingKey[String]("Enabler Scalariform version")
    val scaffoldVersionSpark: SettingKey[String] = settingKey[String]("Enabler Spark version")
    val scaffoldVersionScalaz: SettingKey[String] = settingKey[String]("Enabler Scalaz version")
    val scaffoldVersionScalazScalaTest: SettingKey[String] = settingKey[String]("Enabler ScalazScalaTest version")
    val scaffoldVersionShapeless: SettingKey[String] = settingKey[String]("Enabler Shapeless version")
    val scaffoldVersionNscalaTime: SettingKey[String] = settingKey[String]("Enabler nscalatime version")
    val scaffoldVersionPlayJson: SettingKey[String] = settingKey[String]("Enabler play-json version")
    val scaffoldVersionLagom: SettingKey[String] = settingKey[String]("Enabler Lagom version")
    val scaffoldVersionSlick: SettingKey[String] = settingKey[String]("Enabler Slick version")
    val scaffoldVersionPlaySlick: SettingKey[String] = settingKey[String]("Enabler PlaySlick version")

  }

  import autoImport._

  lazy val defaultSettings: Seq[Setting[_]] = Seq(
    scaffoldVersionAkka := "2.4.12",
    scaffoldVersionHikariCp := "2.5.1",
    scaffoldVersionH2 := "1.4.193",
    scaffoldVersionPostgres := "9.4.1212",
    scaffoldVersionAnorm := "2.5.2",
    scaffoldVersionSwagger := "1.5.3",
    scaffoldVersionAkkaStreamKafka := "0.13",
    scaffoldVersionBuildInfo := "0.6.1",
    scaffoldVersionConductr := "2.1.20",
    scaffoldVersionSbtHeader := "1.5.1",
    scaffoldVersionScalariform := "1.6.0",
    scaffoldVersionSpark := "2.0.2",
    scaffoldVersionScalaz := "7.2.8",
    scaffoldVersionScalazScalaTest := "1.1.0",
    scaffoldVersionShapeless := "2.3.2",
    scaffoldVersionNscalaTime := "2.14.0",
    scaffoldVersionPlayJson := "2.5.10",
    scaffoldVersionLagom := "1.3.0-M1",
    scaffoldVersionSlick := "3.1.1",
    scaffoldVersionPlaySlick := "2.0.2",

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
      new EnablerContext(
        ammonite.ops.Path(baseDir),
        ammonite.ops.Path(srcDir),
        ammonite.ops.Path(resourceDir),
        ammonite.ops.Path(testDir),
        organizationName,
        projectName,
        scaffoldVersionAkka.value,
        scaffoldVersionHikariCp.value,
        scaffoldVersionH2.value,
        scaffoldVersionPostgres.value,
        scaffoldVersionAnorm.value,
        scaffoldVersionSwagger.value,
        scaffoldVersionAkkaStreamKafka.value,
        scaffoldVersionBuildInfo.value,
        scaffoldVersionConductr.value,
        scaffoldVersionSbtHeader.value,
        scaffoldVersionScalariform.value,
        scaffoldVersionSpark.value,
        scaffoldVersionScalaz.value,
        scaffoldVersionScalazScalaTest.value,
        scaffoldVersionShapeless.value,
        scaffoldVersionNscalaTime.value,
        scaffoldVersionPlayJson.value,
        scaffoldVersionLagom.value,
        scaffoldVersionSlick.value,
        scaffoldVersionPlaySlick.value
      )
    },

    enable := {
      val ctx = enablerContext.value
      implicit val log: Logger = streams.value.log
      val choice = Parsers.enablerParser.parsed
      val enablerResult = choice match {
        case AllEnablerChoice =>
          EveryFeatureEnabler.execute(ctx)
        case AnormEnablerChoice =>
          AnormEnabler.execute(ctx)
        case AkkaEnablerChoice =>
          AkkaEnabler.execute(ctx)
        case BuildInfoEnablerChoice =>
          BuildInfoEnabler.execute(ctx)
        case CircuitBreakerEnablerChoice =>
          CircuitBreakerEnabler.execute(ctx)
        case ConductrEnablerChoice =>
          ConductrEnabler.execute(ctx)
        case DockerEnablerChoice =>
          DockerEnabler.execute(ctx)
        case FpEnablerChoice =>
          FpEnabler.execute(ctx)
        case JsonEnablerChoice =>
          JsonEnabler.execute(ctx)
        case LoggerEnablerChoice =>
          LoggingEnabler.execute(ctx)
        case KafkaEnablerChoice =>
          KafkaEnabler.execute(ctx)
        case ScalariformEnablerChoice =>
          ScalariformEnabler.execute(ctx)
        case SbtHeaderEnablerChoice =>
          SbtHeaderEnabler.execute(ctx)
        case SlickEnablerChoice =>
          SlickEnabler.execute(ctx)
        case SwaggerEnablerChoice =>
          SwaggerEnabler.execute(ctx)
        case SparkEnablerChoice =>
          SparkEnabler.execute(ctx)
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
        case BuildInfoControllerChoice =>
          new BuildInfoControllerScaffold().execute(ctx)
        case ControllerChoice =>
          new ControllerScaffold().execute(ctx)
        case CrudControllerChoice =>
          new CrudControllerScaffold().execute(ctx)
        case PingControllerChoice =>
          new PingControllerScaffold().execute(ctx)
        case HealthControllerChoice =>
          new HealthControllerScaffold().execute(ctx)
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
