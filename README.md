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
> enable swagger
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

## Scaffolding
...

## Releases
- v0.0.1 (2016-12-18)
  - Initial release
