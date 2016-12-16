package com.github.dnvriend.scaffold.play.util

object StringUtils {
  implicit class StringImplicits(val that: String) extends AnyVal {
    def uncapitalize: String = that.take(1).toLowerCase + that.drop(1)
  }
}
