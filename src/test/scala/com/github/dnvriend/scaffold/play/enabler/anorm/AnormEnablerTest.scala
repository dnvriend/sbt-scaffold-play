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

package com.github.dnvriend.scaffold.play.enabler.anorm

import ammonite.ops.Path
import com.github.dnvriend.TestSpec
import com.github.dnvriend.scaffold.play.enabler.sbtheader.SbtHeaderEnablerResult
import com.github.dnvriend.scaffold.play.enabler.slick.SlickEnablerResult

class AnormEnablerTest extends TestSpec {
  it should "check rules" in {
    AnormEnabler.check(List(SbtHeaderEnablerResult(Path("/"), Path("/")))) should beRight(())
    AnormEnabler.check(List(AnormEnablerResult(Path("/"), Path("/"), Path("/")))) should beLeft("Anorm already installed")
    AnormEnabler.check(List(SlickEnablerResult(Path("/"), Path("/"), Path("/")))) should beLeft("Slick already installed")
    AnormEnabler.check(List(AnormEnablerResult(Path("/"), Path("/"), Path("/")), SlickEnablerResult(Path("/"), Path("/"), Path("/")))) should beLeft("Both Slick and Anorm already installed")
  }
}
