# sbt-scaffold-play
A scaffolding plugin and feature enabler for the playframework

## Disclaimer
Not yet finished, do not use!

## How to use
The `sbt-scaffold-play` plugin is available when starting the following project:

- Create a new project by using `sbt new dnvriend/play-seed.g8`
- launch sbt

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