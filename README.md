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

You can also type `all` to get scalariform, sbtheader, buildinfo, fp, json, loggng, anorm, akka and swagger.

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
so you can directly call the endpoint after scaffolding and when Play recompiles the scaffold should work.

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

### PingController (not yet released)
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

## Releases
- v0.0.1 (2016-12-18)
  - Initial release
