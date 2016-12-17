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

package com.github.dnvriend.scaffold.play.userinput

import com.github.dnvriend.scaffold.play.parsers.Parsers
import com.github.dnvriend.scaffold.play.parsers.Parsers.{ Choice, FieldType, No, Yes }
import com.github.dnvriend.scaffold.play.util.UserInput

import scalaz._
import Scalaz._

final case class EntityUserInput(className: String, fields: List[(String, FieldType)]) extends UserInput {
  def renderClass: String = EntityUserInput.renderProductType(className, fields)
  def renderFields: String = EntityUserInput.renderFields(fields)
  def renderEntityNameField: String = EntityUserInput.renderEntityNameField(className, fields)
  def renderFieldNames: String = EntityUserInput.renderFieldNames(fields)
  def renderGetAllSql: String = EntityUserInput.renderGetAllSql(className)
  def renderGetByIdSql: String = EntityUserInput.renderGetById(className)
  def renderSaveSql: String = EntityUserInput.renderSaveSql(className, fields)
  def renderUpdateByIdSql: String = EntityUserInput.renderUpdateByIdSql(className, fields)
  def renderDeleteByIdSql: String = EntityUserInput.renderDeleteByIdSql(className)
  def renderCreateTable: String = EntityUserInput.renderCreateTable(className, fields)
  def renderDropTable: String = EntityUserInput.renderDropTable(className)
}

object EntityUserInput {
  type Field = (String, FieldType)
  type Fields = List[Field]

  def askUser(defaultClassName: String): Disjunction[String, EntityUserInput] = for {
    className <- UserInput.readLine[String](Parsers.classNameParser(defaultClassName), "Enter entityName > ")
    fields <- askFields(className)
  } yield EntityUserInput(className, fields)

  def askField(className: String, fields: Fields): Disjunction[String, (String, FieldType, Choice)] = {
    val state = renderProductType(className, fields)
    for {
      fieldName <- UserInput.readLine(Parsers.fieldNameParser, s"$state: Enter field Name > ")
      fieldType <- UserInput.readLine(Parsers.fieldTypeParser, s"$state: Enter field type > ")
      continue <- UserInput.readLine(Parsers.ynParser, s"$state: Another field ? > ")
    } yield (fieldName, fieldType, continue)
  }

  def askFields(className: String): Disjunction[String, Fields] = {
    def loop(acc: Fields): Disjunction[String, Fields] =
      askField(className, acc) match {
        case DRight((fieldName, fieldType, Yes)) =>
          loop(acc :+ (fieldName, fieldType))
        case DRight((fieldName, fieldType, No)) =>
          (acc :+ (fieldName, fieldType)).right[String]
        case err @ DLeft(_) => err
      }

    loop(List.empty[Field])
  }

  def renderGetAllSql(className: String): String =
    s"SELECT * FROM ${className.toUpperCase} LIMIT $$limit OFFSET $$offset"

  def renderGetById(className: String): String =
    s"SELECT * FROM ${className.toUpperCase} WHERE id = $$id"

  def renderSaveSql(className: String, fields: Fields): String =
    s"INSERT INTO ${className.toUpperCase} (${renderFieldNames(fields)}) VALUES (${renderFieldNamesInterpolated(fields)})"

  def renderUpdateByIdSql(className: String, fields: Fields): String =
    s"UPDATE ${className.toUpperCase} SET ${renderFieldsIsInterpolated(fields)} WHERE id=$$id"

  def renderDeleteByIdSql(className: String): String =
    s"DELETE ${className.toUpperCase} WHERE id = $$id"

  def renderCreateTable(className: String, fields: Fields): String =
    s"CREATE TABLE ${className.toUpperCase} (id SERIAL, ${renderSqlFields(fields)});"

  def renderDropTable(className: String): String =
    s"DROP TABLE ${className.toUpperCase};"

  def renderFieldsIsInterpolated(fields: Fields): String = {
    def renderField(field: Field): String = s"${field._1}=$$${field._1}"
    fields.map(renderField).mkString(", ")
  }

  def renderSqlFields(fields: Fields): String = {
    def renderField(field: Field): String = s"${field._1.toLowerCase} ${field._2.sql} NOT NULL"
    fields.map(renderField).mkString(", ")
  }

  /**
   * Render the field names of the entity interpolated eg. for a Person(name: String, age: Int)
   * it would return '$name, $age'
   */
  def renderFieldNamesInterpolated(fields: Fields): String = {
    def renderField(field: Field): String = s"$$${field._1}"
    fields.map(renderField).mkString(", ")
  }

  /**
   * Render only the field names of the entity eg. for a Person(name: String, age: Int)
   * it would return 'name, age'
   */
  def renderFieldNames(fields: Fields): String = {
    def renderField(field: Field): String = field._1
    fields.map(renderField).mkString(", ")
  }

  /**
   * Render only the field names with the field type of the entity eg. for a Person(name: String, age: Int)
   * it would return 'name: String, age: Int'
   */
  def renderFields(fields: Fields): String = {
    def renderField(field: Field): String = s"${field._1}: ${field._2.name}"
    fields.map(renderField).mkString(", ")
  }

  /**
   * Render the entityname and field name eg. for a Person(name: String, age: Int)
   * it would return 'person.name, person.age'
   */
  def renderEntityNameField(className: String, fields: Fields): String = {
    def renderField(entityName: String, field: Field): String = s"$entityName.${field._1}"
    fields.map(renderField(className.toLowerCase, _)).mkString(", ")
  }

  /**
   * Render the whole entity eg. for a Person(name: String, age: Int)
   * it would return 'Person(name: String, age: Int)'
   */
  def renderProductType(className: String, fields: Fields): String = {
    val renderedFields = renderFields(fields)
    s"$className($renderedFields)"
  }
}
