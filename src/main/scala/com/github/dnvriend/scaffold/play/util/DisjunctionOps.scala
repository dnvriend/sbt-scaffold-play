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

package com.github.dnvriend.scaffold.play.util

import scala.language.implicitConversions
import scalaz.Disjunction

object DisjunctionOps {
  implicit def DisjunctionOfThrowableToDisjunctionOfString[A](that: Disjunction[Throwable, A]): Disjunction[String, A] = that.leftString
  implicit class DisjunctionImplicits[A](val that: Disjunction[Throwable, A]) extends AnyVal {
    def leftString: Disjunction[String, A] = that.leftMap(_.toString)
  }
}
