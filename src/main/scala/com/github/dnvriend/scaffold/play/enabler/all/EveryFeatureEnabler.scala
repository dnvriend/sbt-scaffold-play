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

package com.github.dnvriend.scaffold.play.enabler.all

import com.github.dnvriend.scaffold.play.enabler.akka.AkkaEnabler
import com.github.dnvriend.scaffold.play.enabler.anorm.AnormEnabler
import com.github.dnvriend.scaffold.play.enabler.buildinfo.BuildInfoEnabler
import com.github.dnvriend.scaffold.play.enabler.circuitbreaker.CircuitBreakerEnabler
import com.github.dnvriend.scaffold.play.enabler.fp.FpEnabler
import com.github.dnvriend.scaffold.play.enabler.json.JsonEnabler
import com.github.dnvriend.scaffold.play.enabler.logging.LoggingEnabler
import com.github.dnvriend.scaffold.play.enabler.sbtheader.SbtHeaderEnabler
import com.github.dnvriend.scaffold.play.enabler.scalariform.ScalariformEnabler
import com.github.dnvriend.scaffold.play.enabler.swagger.SwaggerEnabler
import com.github.dnvriend.scaffold.play.enabler.{ Enabler, EnablerContext, EnablerResult }

import scalaz.Disjunction

final case class EveryFeatureEnablerResult(scalariform: EnablerResult, sbtHeader: EnablerResult, buildInfo: EnablerResult, fp: EnablerResult, json: EnablerResult, logging: EnablerResult, anorm: EnablerResult, akka: EnablerResult, swagger: EnablerResult, circuitBreaker: EnablerResult) extends EnablerResult

class EveryFeatureEnabler extends Enabler {
  override def execute(ctx: EnablerContext): Disjunction[String, EnablerResult] = for {
    scalariform <- new ScalariformEnabler().execute(ctx)
    sbtHeader <- new SbtHeaderEnabler().execute(ctx)
    buildInfo <- new BuildInfoEnabler().execute(ctx)
    fp <- new FpEnabler().execute(ctx)
    json <- new JsonEnabler().execute(ctx)
    logging <- new LoggingEnabler().execute(ctx)
    anorm <- new AnormEnabler().execute(ctx)
    akka <- new AkkaEnabler().execute(ctx)
    swagger <- new SwaggerEnabler().execute(ctx)
    circuitBreaker <- new CircuitBreakerEnabler().execute(ctx)
  } yield EveryFeatureEnablerResult(scalariform, sbtHeader, buildInfo, fp, json, logging, anorm, akka, swagger, circuitBreaker)
}
