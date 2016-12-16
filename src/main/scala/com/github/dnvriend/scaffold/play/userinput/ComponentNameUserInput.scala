package com.github.dnvriend.scaffold.play.userinput

import com.github.dnvriend.scaffold.play.util.UserInput
import sbt.complete.DefaultParsers

import scalaz._

object ComponentNameUserInput {
  def askUser(context: String): Disjunction[String, String] =
    UserInput.readLine(DefaultParsers.StringBasic, s"[$context] Enter component name > ")
}
