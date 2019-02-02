// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect.Effect
import demo.model._
import demo.repo._
import sangria.execution.deferred.Deferred
import sangria.schema._

object LanguageType {

  object Deferred {
    case class ByCountryCode(code: String) extends Deferred[List[Language]]
  }

  def apply[F[_]: Effect]: ObjectType[MasterRepo[F], Language] =
    ObjectType(
      name     = "Language",
      fieldsFn = () => fields(

        Field(
          name      = "language",
          fieldType =  StringType,
          resolve   = _.value.language
        ),

        Field(
          name      = "isOfficial",
          fieldType =  BooleanType,
          resolve   = _.value.isOfficial
        ),

        Field(
          name      = "percentage",
          fieldType =  FloatType,
          resolve   = _.value.percentage.toDouble
        ),

      )
    )

}
