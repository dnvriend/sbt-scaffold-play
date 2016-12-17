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

package com.github.dnvriend.scaffold.play.parsers

import com.github.dnvriend.scaffold.play.util.ScaffoldParser._
import org.scalatest.{ FlatSpec, Matchers }
import org.typelevel.scalatest.ValidationMatchers

class ParsersTest extends FlatSpec with Matchers with ValidationMatchers {
  it should "classNameParser" in {
    Parsers.classNameParser("").parse("") shouldBe failure
    Parsers.classNameParser("").parse(" ") shouldBe failure
    Parsers.classNameParser("").parse("MyClass ") shouldBe failure
    Parsers.classNameParser("").parse("1yClass") shouldBe failure
    Parsers.classNameParser("").parse("myClass") shouldBe failure
    Parsers.classNameParser("").parse("MyClass") should beSuccess("MyClass")
    Parsers.classNameParser("").parse("MYClass") should beSuccess("MYClass")
    Parsers.classNameParser("").parse("M1Class") should beSuccess("M1Class")
  }

  it should "packageParser" in {
    //    Parsers.packageParser("").parse("") should haveFailure("Expected lower case character")
    //    Parsers.packageParser("").parse(" ") should haveFailure("Expected lower case character")
    //    Parsers.packageParser("").parse(".") should haveFailure("Expected lower case character ....")
    //    Parsers.packageParser("").parse("..") should haveFailure("Expected lower case character .....")
    //    Parsers.packageParser("").parse(".a.") should haveFailure("Expected lower case character ....a.")
    //    Parsers.packageParser("").parse("a..") should haveFailure("Expected lower case character ...a..")
    //    Parsers.packageParser("").parse("a..b") should haveFailure("Expected lower case character ...a..b")
    //    Parsers.packageParser("").parse("a..b..c") should haveFailure("Expected lower case character ...a..b..c")
    //    Parsers.packageParser("").parse("aa.bb.cc ") should haveFailure("Expected lower case character ...Expected dot ...Excluded. ...aa.bb.cc")
    //    Parsers.packageParser("").parse("AA.BB.CC") should haveFailure("Expected lower case character ...AA.BB.CC")
    //    Parsers.packageParser("").parse("aa.BB.cc") should haveFailure("Expected lower case character ...aa.BB.cc")
    //    Parsers.packageParser("").parse("aa.bb.CC") should haveFailure("Expected lower case character ...aa.bb.CC")
    Parsers.packageParser("").parse("a") should beSuccess("a")
    Parsers.packageParser("").parse("a.b") should beSuccess("a.b")
    Parsers.packageParser("").parse("aa.bb") should beSuccess("aa.bb")
    Parsers.packageParser("").parse("aa.bb.cc") should beSuccess("aa.bb.cc")
    Parsers.packageParser("").parse("com.github") should beSuccess("com.github")
    Parsers.packageParser("").parse("com.github.dnvriend") should beSuccess("com.github.dnvriend")
    Parsers.packageParser("").parse("com.github.dnvriend.controller") should beSuccess("com.github.dnvriend.controller")
  }
}