// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.sangria

import cats.effect._
import cats.effect.implicits._
import demo.model._
import demo.repo._
import sangria.execution.deferred.Deferred
import sangria.schema._

object CountryType {

  object Deferred {
    final case class ByCode(code: String) extends Deferred[Country]
  }

  def apply[F[_]: Effect]: ObjectType[MasterRepo[F], Country] =
    ObjectType(
      name     = "Country",
      fieldsFn = () => fields[MasterRepo[F], Country](

        Field(
          name           = "name",
          fieldType      = StringType,
          resolve        = _.value.name
        ),

        Field(
          name           = "continent",
          fieldType      = StringType,
          resolve        = _.value.continent
        ),

        Field(
          name           = "region",
          fieldType      = StringType,
          resolve        = _.value.region
        ),

        Field(
          name           = "surfacearea",
          fieldType      = FloatType,
          resolve        = _.value.surfacearea.toDouble // this is a float
        ),

        Field(
          name           = "indepyear",
          fieldType      = OptionType(IntType), // should be ShortType
          resolve        = _.value.indepyear.map(_.toInt)
        ),

        Field(
          name           = "population",
          fieldType      = IntType,
          resolve        = _.value.population
        ),

        Field(
          name           = "lifeexpectancy",
          fieldType      = OptionType(FloatType),
          resolve        = _.value.lifeexpectancy.map(_.toDouble) //
        ),

        Field(
          name           = "gnp",
          fieldType      = OptionType(BigDecimalType),
          resolve        = _.value.gnp
        ),

        Field(
          name           = "gnpold",
          fieldType      = OptionType(BigDecimalType),
          resolve        = _.value.gnpold
        ),

        Field(
          name           = "localname",
          fieldType      = StringType,
          resolve        = _.value.localname
        ),

        Field(
          name           = "governmentform",
          fieldType      = StringType,
          resolve        = _.value.governmentform
        ),

        Field(
          name           = "headofstate",
          fieldType      = OptionType(StringType),
          resolve        = _.value.headofstate
        ),

        Field(
          name           = "capitalId",
          fieldType      = OptionType(IntType),
          resolve        = _.value.capitalId
        ),

        Field(
          name           = "code2",
          fieldType      = StringType,
          resolve        = _.value.code2
        ),

        Field(
          name           = "cities",
          fieldType      = ListType(CityType[F]),
          resolve        = e => e.ctx.city.fetchByCountryCode(e.value.code).compile.to[List].toIO.unsafeToFuture
        ),

        Field(
          name           = "languages",
          fieldType      = ListType(LanguageType[F]),
          resolve        = e => e.ctx.language.fetchByCountryCode(e.value.code).compile.to[List].toIO.unsafeToFuture
        )

      )
    )


}
