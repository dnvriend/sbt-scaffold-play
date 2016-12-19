# sbt-scaffold-play
A scaffolding plugin and feature enabler for the playframework

[![Download](https://api.bintray.com/packages/dnvriend/sbt-plugins/sbt-scaffold-play/images/download.svg) ](https://bintray.com/dnvriend/sbt-plugins/sbt-scaffold-play/_latestVersion)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Disclaimer
Not yet finished, do not use!

## How to use
The `sbt-scaffold-play` plugin is available when starting the following project:

- Create a new project by using `sbt new dnvriend/play-seed.g8`
- launch sbt

## Install
You should use sbt v0.13.13 or higher and put the following in your `plugins.sbt` file:

```scala
resolvers += Resolver.url(
  "bintray-dnvriend-ivy-sbt-plugins",
  url("http://dl.bintray.com/dnvriend/sbt-plugins"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.github.dnvriend" % "sbt-scaffold-play" % "0.0.1")
```

## Workflow
I have two terminals running, both running the same sbt project but the first terminal runs the play application
and the second terminal scaffolds features in your play application

- Launch play application with `sbt run`
- Launch the sbt terminal with `sbt`

## Enabler
You can enable play features with the __enable__ command for example:

```bash
[play-seed] $ enable swagger
[info] Enable complete
```

This will install swagger in your project.

The following features can be enabled:

- akka
- all
- anorm
- buildinfo
- circuitbreaker
- conductr
- docker
- fp
- json
- kafka
- logback
- sbtheader
- scalariform
- slick
- spark
- swagger

You can also type `all` to get scalariform, sbtheader, buildinfo, fp, json, loggng, anorm, akka, circuitbreaker and swagger.

Enabling all features will add the following structure to `play-seed`:

```bash
.
├── LICENSE
├── README.md
├── build-akka.sbt
├── build-anorm.sbt
├── build-buildinfo.sbt
├── build-fp.sbt
├── build-json.sbt
├── build-sbt-header.sbt
├── build-scalariform.sbt
├── build-swagger.sbt
├── build.sbt
├── conf
│   ├── akka.conf
│   ├── anorm.conf
│   ├── application.conf
│   ├── logback.xml
│   ├── routes
│   └── swagger.conf
├── project
│   ├── build.properties
│   ├── plugin-buildinfo.sbt
│   ├── plugin-sbt-header.sbt
│   ├── plugin-scalariform.sbt
│   ├── plugins.sbt
```

### Akka
[Akka](http://akka.io/) can be enabled by typing:

```
[play-seed] $ enable akka
[info] Enable complete
```

sbt settings `build-akka.sbt`:

```
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.12"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.12"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.12"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4.12"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.12"
```

configuration `conf/akka.conf`:

```
akka {
 loggers = ["akka.event.slf4j.Slf4jLogger"]
  loglevel = debug
  stdout-loglevel = info
  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"

actor {
    serialize-messages = off // when on, the akka framework will determine whether or not messages can be serialized, else the plugin

    debug {
      receive = on // log all messages sent to an actor if that actors receive method is a LoggingReceive
      autoreceive = off // log all special messages like Kill, PoisoffPill etc sent to all actors
      lifecycle = off // log all actor lifecycle events of all actors
      fsm = off // enable logging of all events, transitioffs and timers of FSM Actors that extend LoggingFSM
      event-stream = off // enable logging of subscriptions (subscribe/unsubscribe) on the ActorSystem.eventStream
    }
  }
}
```

### Anorm
[Anorm](https://github.com/playframework/anorm) can be enabled by typing:

```
[play-seed] $ enable anorm
[info] Enable complete
```

sbt settings `build-anorm.sbt`:

```
// database support
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "com.zaxxer" % "HikariCP" % "2.5.1"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.2"
// database driver
libraryDependencies += "com.h2database" % "h2" % "1.4.193"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"
```

configuration `conf/anorm.conf`:

```
# H2 configuration
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"

# Postgres configuration
#db.default.driver=org.postgresql.Driver
#db.default.url="jdbc:postgresql://localhost:5432/postgres?reWriteBatchedInserts=true"
#db.default.username="postgres"
#db.default.password="postgres"

# play evolutions
play.evolutions.enabled=true
play.evolutions.autoApply=true

# Connection pool configuration
play.db.autocommit = true
play.db.connectionTimeout = 30 seconds
play.db.idleTimeout = 10 minutes
play.db.maxLifetime = 30 minutes
play.db.maximumPoolSize = 10
play.db.initializationFailFast = false
play.db.isolateInternalQueries = false
play.db.allowPoolSuspension = false
play.db.readOnly = false
play.db.registerMbeans = false
play.db.validationTimeout = 5 seconds

anorm {
  context {
   fork-join-executor {
      parallelism-max=10
    }
  }
}

play.modules.enabled += "play.modules.anorm.AnormModule"
```

Play module: `play.modules.anorm.AnormModule`:

```scala
package play.modules.anorm

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.google.inject.{ AbstractModule, Provides }

import scala.concurrent.ExecutionContext

class AnormModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides @Singleton
    def anormExecutionContextProvider(system: ActorSystem): AnormExecutionContext =
      new AnormExecutionContext(system)
  }
}

class AnormExecutionContext(system: ActorSystem) extends ExecutionContext {
  val ec: ExecutionContext = system.dispatchers.lookup("anorm.context")
  override def execute(runnable: Runnable): Unit = ec.execute(runnable)
  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
}
```

### Buildinfo
[Buildinfo](https://github.com/sbt/sbt-buildinfo) can be enabled by typing:

```
[play-seed] $ enable buildinfo
[info] Enable complete
```

plugin config `project/plugin-buildinfo.sbt`:

```
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
```

sbt settings: `build-buildinfo.sbt`:

```
enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoOptions += BuildInfoOption.ToMap

buildInfoOptions += BuildInfoOption.ToJson

buildInfoOptions += BuildInfoOption.BuildTime

buildInfoPackage := organization.value
```

### CircuitBreaker
[CircuitBreaker](http://doc.akka.io/docs/akka/current/common/circuitbreaker.html) can be added by typing:

```bash
[play-seed] $ enable circuitbreaker
[info] Enable complete
[success] Total time: 0 s, completed 18-dec-2016 14:27:22
```

configuration `conf/circuit-breaker.conf`:

```
play.modules.enabled += "play.modules.cb.CircuitBreakerModule"
```

Play module `play.modules.cb.CircuitBreakerModule`:

```scala
package play.modules.cb

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.google.inject.{ AbstractModule, Provides }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CircuitBreakerModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def circuitBreakerProvider(system: ActorSystem)(implicit ec: ExecutionContext): CircuitBreaker = {
      val maxFailures: Int = 3
      val callTimeout: FiniteDuration = 1.seconds
      val resetTimeout: FiniteDuration = 10.seconds
      new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout)
    }
  }
}
```

After enabling CircuitBreaker, you can inject a circuitBreaker in any resource like Controller, Repository etc.

### Conductr
[Conductr](https://github.com/typesafehub/sbt-conductr) can be added by typing:

```
[play-seed] $ enable conductr
[info] Enable complete
```

plugin config `project/plugin-conductr.sbt`:

```
addSbtPlugin("com.lightbend.conductr" % "sbt-conductr" % "2.1.20")
```

configuration `build-conductr.sbt`:

```
enablePlugins(PlayBundlePlugin)

BundleKeys.endpoints := Map(
  "play" -> Endpoint(bindProtocol = "http", bindPort = 0, services = Set(URI("http://:9000/play-seed"))),
  "akka-remote" -> Endpoint("tcp")
)

normalizedName in Bundle := "play-seed"

BundleKeys.system := "play"

BundleKeys.startCommand += "-Dhttp.address=$PLAY_BIND_IP -Dhttp.port=$PLAY_BIND_PORT -Dplay.akka.actor-system=$BUNDLE_SYSTEM"
```

### Docker
A docker-compose file can be added with the latest kafka, zookeeper, cassandra and postgres by typing:

```
[play-seed] $ enable docker
[info] Enable complete
```

This will add `docker-compose.yml`:

```
version: '2'

services:
  zookeeper:
    image: wurstmeister/zookeeper
    restart: always
    ports:
      - "2181:2181"

  kafka:
    image: wurstmeister/kafka
    restart: always
    ports:
      - "9092:9092"
    environment:
      - "KAFKA_ADVERTISED_HOST_NAME=localhost"
      - "KAFKA_CREATE_TOPICS=test:1:1"
      - "KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181"
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"

  cassandra:
    image: cassandra:latest
    restart: always
    ports:
      - "9042:9042"

  postgres:
    image: postgres:latest
    restart: always
    ports:
      - "5432:5432"
    environment:
      - "POSTGRES_DB=postgres"
      - "POSTGRES_USER=postgres"
      - "POSTGRES_PASSWORD=postgres"
    volumes:
      - "./initdb:/docker-entrypoint-initdb.d"
```

### FP
Your favorite functional programming libraries can be added by typing:

```
[play-seed] $ enable fp
[info] Enable complete
```

sbt settings `build-fp.sbt`:

```
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.0" % Test
```

### Json
Play-json support can be added by typing:

```
[play-seed] $ enable json
[info] Enable complete
```

sbt settings: `build-json.sbt`:

```
libraryDependencies += ws
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.14.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "1.3.0-M1"
```

### Kafka
Kafka can be added by typing:

```
[play-seed] $ enable kafka
[info] Enable complete
```

sbt settings: `build-kafka.sbt`:

```
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
```

producer config: `conf/kafka-producer.conf`:

```
# Properties for akka.kafka.ProducerSettings can be
# defined in this section or a configuration section with
# the same layout.
akka.kafka.producer {
  # Tuning parameter of how many sends that can run in parallel.
  parallelism = 100

  # How long to wait for `KafkaProducer.close`
  close-timeout = 60s

  # Fully qualified config path which holds the dispatcher configuration
  # to be used by the producer stages. Some blocking may occur.
  # When this value is empty, the dispatcher configured for the stream
  # will be used.
  use-dispatcher = "akka.kafka.default-dispatcher"

  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig
  # can be defined in this configuration section.
  kafka-clients {
  }
}
```

consumer config: `conf/kafka-consumer.conf`:

```
# Properties for akka.kafka.ConsumerSettings can be
# defined in this section or a configuration section with
# the same layout.
akka.kafka.consumer {
  # Tuning property of scheduled polls.
  poll-interval = 50ms

  # Tuning property of the `KafkaConsumer.poll` parameter.
  # Note that non-zero value means that blocking of the thread that
  # is executing the stage will be blocked.
  poll-timeout = 50ms

  # The stage will be await outstanding offset commit requests before
  # shutting down, but if that takes longer than this timeout it will
  # stop forcefully.
  stop-timeout = 30s

  # How long to wait for `KafkaConsumer.close`
  close-timeout = 20s

  # If offset commit requests are not completed within this timeout
  # the returned Future is completed `TimeoutException`.
  commit-timeout = 15s

  # If the KafkaConsumer can't connect to the broker the poll will be
  # aborted after this timeout. The KafkaConsumerActor will throw
  # org.apache.kafka.common.errors.WakeupException which will be ignored
  # until max-wakeups limit gets exceeded.
  wakeup-timeout = 3s

  # After exceeding maxinum wakeups the consumer will stop and the stage will fail.
  max-wakeups = 10

  # Fully qualified config path which holds the dispatcher configuration
  # to be used by the KafkaConsumerActor. Some blocking may occur.
  use-dispatcher = "akka.kafka.default-dispatcher"

  # Properties defined by org.apache.kafka.clients.consumer.ConsumerConfig
  # can be defined in this configuration section.
  kafka-clients {
    # Disable auto-commit by default
    enable.auto.commit = false
  }
}
```

### Logback
A `logback.xml` can be added by typing:

```
[play-seed] $ enable logback
[info] Enable complete
```

logback configuration: `conf/logback.xml`:

```
<configuration debug="false">
  <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
      <level>debug</level>
    </filter>
    <encoder>
      <pattern>%date {ISO8601} - %logger -&gt; %-5level[%thread] %logger {0} - %msg%n</pattern>
    </encoder>
  </appender>
  <logger name="akka" level="info"/>
  <logger name="slick.backend.DatabaseComponent.action" level="debug"/>
  <logger name="com.zaxxer.hikari.pool.HikariPool" level="debug"/>
  <logger name="org.jdbcdslog.ConnectionLogger" level="debug"/>
  <logger name="org.jdbcdslog.StatementLogger" level="debug"/>
  <logger name="org.jdbcdslog.ResultSetLogger" level="debug"/>
  <logger name="com.github.dnvriend" level="debug"/>
  <root level="error">
    <appender-ref ref="console"/>
  </root>
</configuration>
```

### SbtHeader
[sbt-header](https://github.com/sbt/sbt-header) can be added by typing:

```
[play-seed] $ enable header
[sbt-header]: Enter your name > Your Name Here
[info] Enable complete
```

plugin config `project/plugin-sbt-header.sbt`:

```
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.5.1")
```

sbt settings `build-sbt-header.sbt`:

```
enablePlugins(AutomateHeaderPlugin)

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Your Name Here"),
  "conf" -> Apache2_0("2016", "Your Name Here", "#")
)
```

### Slick
[Slick](http://slick.lightbend.com/) and [play-slick](https://www.playframework.com/documentation/2.5.x/PlaySlick) can be enabled by typing:

```
[play-seed] $ enable slick
[info] Enable complete
```

sbt settings `build-slick.sbt`:

```
// database support
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "com.zaxxer" % "HikariCP" % "2.5.1"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "2.0.2"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.1.1"
libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1"
// database driver
libraryDependencies += "com.h2database" % "h2" % "1.4.193"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4.1212"
```

configuration `conf/slick.conf`:

```
# H2 Configuration
slick.dbs.default.driver="slick.driver.H2Driver$"
slick.dbs.default.db.driver="org.h2.Driver"
slick.dbs.default.db.url="jdbc:h2:mem:play"

# Postgres configuration
#slick.dbs.default.driver="slick.driver.PostgresDriver$"
#slick.dbs.default.db.driver="org.postgresql.Driver"
#slick.dbs.default.db.url="jdbc:postgresql://localhost:5432/postgres?reWriteBatchedInserts=true"
#slick.dbs.default.db.user=postgres
#slick.dbs.default.db.password=postgres

slick.dbs.default.db.maximumPoolSize=10

slick {
  context {
   fork-join-executor {
      parallelism-max=10
    }
  }
}

play.modules.enabled += "play.modules.slick.SlickModule"
```

Play module: `play.modules.slick.SlickModule`:

```scala
package play.modules.slick

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}

import scala.concurrent.ExecutionContext

class SlickModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides @Singleton
    def slickExecutionContextProvider(system: ActorSystem): SlickExecutionContext =
      new SlickExecutionContext(system)
  }
}

class SlickExecutionContext (system: ActorSystem) extends ExecutionContext {
  val ec: ExecutionContext = system.dispatchers.lookup("slick.context")
  override def execute(runnable: Runnable): Unit = ec.execute(runnable)
  override def reportFailure(cause: Throwable): Unit = ec.reportFailure(cause)
}
```

### Scalariform
[sbt-scalariform](https://github.com/sbt/sbt-scalariform) can be added by typing:

```
[play-seed] $ enable scalariform
[info] Enable complete
```

plugin config `project/plugin-scalariform.sbt`:

```
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
```

sbt settings `build-scalariform.sbt`:

```
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
```

### Spark
[Apache Spark](https://github.com/apache/spark) can be added by typing:

```
[play-seed] $ enable spark
[info] Enable complete
```

sbt settings `build-spark.sbt`:

```
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.0.2"
```

configuration `conf/spark.conf`:

```bash
play.modules.enabled += "play.modules.spark.SparkModule"
```

And it will add the `play.modules.spark.SparkModule`:

```scala
package play.modules.spark

import com.google.inject.{AbstractModule, Provides}
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

class SparkModule extends AbstractModule {
  override def configure(): Unit = {
    @Provides
    def sparkContextProvider(sparkSession: SparkSession): SparkContext =
      sparkSession.sparkContext

    @Provides
    def sparkSessionProvider: SparkSession =
      SparkSession.builder()
      .config("spark.sql.warehouse.dir", "file:/tmp/spark-warehouse")
      .config("spark.scheduler.mode", "FAIR")
      .config("spark.sql.crossJoin.enabled", "true")
      .config("spark.ui.enabled", "true") // better to enable this to see what is going on
      .config("spark.sql.autoBroadcastJoinThreshold", 1)
      .config("spark.default.parallelism", 4) // number of cores
      .config("spark.sql.shuffle.partitions", 1) // default 200
      .config("spark.memory.offHeap.enabled", "true") // If true, Spark will attempt to use off-heap memory for certain operations.
      .config("spark.memory.offHeap.size", "536870912") // The absolute amount of memory in bytes which can be used for off-heap allocation.
      .config("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")
      .config("spark.streaming.stopSparkContextByDefault", "false")
      .config("spark.debug.maxToStringFields", 50) // default 25 see org.apache.spark.util.Utils
      // see: https://spark.apache.org/docs/latest/sql-programming-guide.html#caching-data-in-memory
      //    .config("spark.sql.inMemoryColumnarStorage.compressed", "true")
      //    .config("spark.sql.inMemoryColumnarStorage.batchSize", "10000")
      .master("local[2]") // better not to set this to 2 for spark-streaming
      .appName("play-spark").getOrCreate()
  }
}
```

After enabling Spark, you can inject a `org.apache.spark.sql.SparkSession` in any resource like Controller, Repository etc.

### Swagger
[Swagger](https://github.com/swagger-api/swagger-play/tree/master/play-2.5/swagger-play2) can be enabled by typing:

```bash
[play-seed] $ enable swagger
[info] Enable complete
```

The following routes will be added:

```bash
GET           /api-docs              controllers.ApiHelpController.getResources
GET           /api-docs/*path        controllers.ApiHelpController.getResource(path: String)
```

By default, the swagger api is available at http://localhost:9000/api-docs or `http :9000/api-docs` if you are using [httpie](https://httpie.org/).


## Scaffolding
You can scaffold (quickly create basic working functionality which you then alter to fit your needs) using the __scaffold__
command for example:

```bash
[play-seed] $ scaffold crud
[crud-controller] Enter component name > people
[crud-controller] Enter REST resource name > people
Enter entityName > Person
Person(): Enter field Name > name
Person(): Enter field type > str
Person(): Another field ? > y
Person(name: String): Enter field Name > age
Person(name: String): Enter field type > int
Person(name: String): Another field ? > n
[info] Scaffold complete
```

The scaffold is available for use.

## Available Scaffolds

### crud
Creates a very simple CRUD REST endpoint with `swagger` annotations, an `evolution` script, a REST endpoint added to `routes`
so you can directly call the endpoint after scaffolding.

```bash
[play-seed] $ scaffold crud
[crud-controller] Enter component name > people
[crud-controller] Enter REST resource name > people
Enter entityName > Person
Person(): Enter field Name > name
Person(): Enter field type > str
Person(): Another field ? > y
Person(name: String): Enter field Name > age
Person(name: String): Enter field type > int
Person(name: String): Another field ? > n
[info] Scaffold complete
```

The scaffold creates the following structure (package name will be different on your project):

```bash
.
├── LICENSE
├── README.md
├── app
│   └── com
│       └── github
│           └── dnvriend
│               ├── Module.scala
│               └── component
│                   └── people
│                       ├── Person.scala
│                       ├── controller
│                       │   └── PersonController.scala
│                       ├── repository
│                       │   └── PersonRepository.scala
│                       └── util
│                           ├── DisjunctionOps.scala
│                           ├── ValidationOps.scala
│                           └── Validator.scala
├── build-akka.sbt
├── build-anorm.sbt
├── build-buildinfo.sbt
├── build-fp.sbt
├── build-json.sbt
├── build-sbt-header.sbt
├── build-scalariform.sbt
├── build-swagger.sbt
├── build.sbt
├── conf
│   ├── akka.conf
│   ├── anorm.conf
│   ├── application.conf
│   ├── evolutions
│   │   └── default
│   │       └── 1.sql
│   ├── logback.xml
│   ├── routes
│   └── swagger.conf
├── project
│   ├── build.properties
│   ├── plugin-buildinfo.sbt
│   ├── plugin-sbt-header.sbt
│   ├── plugin-scalariform.sbt
│   ├── plugins.sbt
└── test
    ├── com
    │   └── github
    │       └── dnvriend
    │           └── TestSpec.scala
    └── resources
        └── application.conf
```

### PingController
Creates a ping REST endpoint with resource `/api/ping` with `swagger` annotations, with endpoint added to `routes` file,
so you can directly call the endpoint after scaffolding.

```bash
[play-seed] $ scaffold pingcontroller
[ping-controller] Enter component name > ping
[info] Scaffold complete
```

The scaffold creates the following structure (package name will be different on your project):

```bash
.
├── app
│   └── com
│       └── github
│           └── dnvriend
│               └── component
│                   └── ping
│                       └── controller
│                           └── PingController.scala
```

### BuildInfoController
Creates a ping REST endpoint with resource `/api/info` with `swagger` annotations, with endpoint added to `routes` file,
so you can directly call the endpoint after scaffolding.

```bash
[play-seed] $ scaffold buildinfo
[buildinfo-controller] Enter component name > buildinfo
[info] Scaffold complete
```

The scaffold creates the following structure (package name will be different on your project):

```bash
├── app
│   └── com
│       └── github
│           └── dnvriend
│               └── component
│                   ├── buildinfo
│                   │   └── controller
│                   │       └── BuildInfoController.scala
```

### HealthController
Creates a health REST endpoint with resource `/api/health` with `swagger` annotations, with endpoint added to `routes` file,
so you can directly call the endpoint after scaffolding.

```bash
[play-seed] $ scaffold health
[health-controller] Enter component name > health
[info] Scaffold complete
```

The scaffold creates the following structure (package name will be different on your project):


```bash
├── app
│   ├── com
│   │   └── github
│   │       └── dnvriend
│   │           └── component
│   │               ├── health
│   │               │   ├── controller
│   │               │   │   └── HealthController.scala
│   │               │   └── util
│   │               │       └── DisjunctionOps.scala
```

## Releases
- v0.0.3 (2016-12-18)
  - Configurable library versions

- v0.0.2 (2016-12-18)
  - scaffolds: PingController, BuildInfoController, HealthController

- v0.0.1 (2016-12-18)
  - Initial release
