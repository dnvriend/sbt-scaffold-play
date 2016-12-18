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

package com.github.dnvriend.scaffold.play.enabler.spark

import ammonite.ops._
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }
import com.github.dnvriend.scaffold.play.util.FileUtils

import scalaz.Disjunction

final case class SparkEnablerResult(settings: Path, config: Path, createdModule: Path) extends EnablerResult

object SparkEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    settings <- createSettings(ctx.baseDir, Template.settings())
    config <- createConfig(ctx.resourceDir, Template.config())
    createdModule <- createModule(ctx.srcDir, "play.modules.spark", "SparkModule")
    _ <- addConfig(ctx.resourceDir)
  } yield SparkEnablerResult(settings, config, createdModule)

  def createSettings(baseDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(baseDir / "build-spark.sbt", content)

  def createConfig(resourceDir: Path, content: String): Disjunction[String, Path] =
    FileUtils.writeFile(resourceDir / "spark.conf", content)

  def addConfig(resourceDir: Path): Disjunction[String, Path] =
    FileUtils.appendToApplication(resourceDir, """include "spark"""")

  def createModule(srcDir: Path, packageName: String, className: String): Disjunction[String, Path] =
    FileUtils.createClass(srcDir, packageName, className, Template.module())
}

object Template {
  def settings(): String =
    s"""
       |val SparkVersion = "2.0.2"
       |libraryDependencies += "org.apache.spark" %% "spark-core" % SparkVersion
       |libraryDependencies += "org.apache.spark" %% "spark-sql" % SparkVersion
    """.stripMargin

  def module(): String =
    """
      |package play.modules.spark
      |
      |import com.google.inject.{AbstractModule, Provides}
      |import org.apache.spark.SparkContext
      |import org.apache.spark.sql.SparkSession
      |
      |class SparkModule extends AbstractModule {
      |  override def configure(): Unit = {
      |    @Provides
      |    def sparkContextProvider(sparkSession: SparkSession): SparkContext =
      |      sparkSession.sparkContext
      |
      |    @Provides
      |    def sparkSessionProvider: SparkSession =
      |      SparkSession.builder()
      |      .config("spark.sql.warehouse.dir", "file:/tmp/spark-warehouse")
      |      .config("spark.scheduler.mode", "FAIR")
      |      .config("spark.sql.crossJoin.enabled", "true")
      |      .config("spark.ui.enabled", "true") // better to enable this to see what is going on
      |      .config("spark.sql.autoBroadcastJoinThreshold", 1)
      |      .config("spark.default.parallelism", 4) // number of cores
      |      .config("spark.sql.shuffle.partitions", 1) // default 200
      |      .config("spark.memory.offHeap.enabled", "true") // If true, Spark will attempt to use off-heap memory for certain operations.
      |      .config("spark.memory.offHeap.size", "536870912") // The absolute amount of memory in bytes which can be used for off-heap allocation.
      |      .config("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")
      |      .config("spark.streaming.stopSparkContextByDefault", "false")
      |      .config("spark.debug.maxToStringFields", 50) // default 25 see org.apache.spark.util.Utils
      |      // see: https://spark.apache.org/docs/latest/sql-programming-guide.html#caching-data-in-memory
      |      //    .config("spark.sql.inMemoryColumnarStorage.compressed", "true")
      |      //    .config("spark.sql.inMemoryColumnarStorage.batchSize", "10000")
      |      .master("local[2]") // better not to set this to 2 for spark-streaming
      |      .appName("play-spark").getOrCreate()
      |  }
      |}
    """.stripMargin

  def config(): String =
    """
      |play.modules.enabled += "play.modules.spark.SparkModule"
    """.stripMargin
}
