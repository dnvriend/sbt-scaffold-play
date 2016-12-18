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
- anorm
- buildinfo
- circuitbreaker
- conductr
- fp
- json
- logging
- sbtheader
- scalariform
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

### Swagger
Swagger can be enabled by typing:

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

### CircuitBreaker
CircuitBreaker can be added by typing:

```bash
[play-seed] $ enable circuitbreaker
[info] Enable complete
[success] Total time: 0 s, completed 18-dec-2016 14:27:22
```

This will create the configuration `/conf/circuit-breaker.conf`:

```bash
play.modules.enabled += "play.modules.cb.CircuitBreakerModule"
```

And it will add the `play.modules.cb.CircuitBreakerModule`:

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
- v0.0.2 (2016-12-18)
  - scaffolds: PingController, BuildInfoController, HealthController

- v0.0.1 (2016-12-18)
  - Initial release
