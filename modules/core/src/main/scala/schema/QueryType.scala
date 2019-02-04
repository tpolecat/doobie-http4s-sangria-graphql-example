// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect._
import cats.effect.implicits._
import demo.repo._
import sangria.schema._

object QueryType {

  val NamePattern: Argument[String] =
    Argument(
      name         = "namePattern",
      argumentType = OptionInputType(StringType),
      description  = "SQL-style pattern for city name, like \"San %\".",
      defaultValue = "%"
    )

  val Code: Argument[String] =
    Argument(
      name         = "code",
      argumentType = StringType,
      description  = "Unique code of a country."
    )

  def apply[F[_]: Effect]: ObjectType[MasterRepo[F], Unit] =
    ObjectType(
      name  = "Query",
      fields = fields(

        Field(
          name        = "cities",
          fieldType   = ListType(CityType[F]),
          description = Some("Returns cities with the given name pattern, if any."),
          arguments   = List(NamePattern),
          resolve     = c => c.ctx.city.fetchAll(c.argOpt(NamePattern)).toIO.unsafeToFuture
        ),

        Field(
          name        = "country",
          fieldType   = OptionType(CountryType[F]),
          description = Some("Returns the country with the given code, if any."),
          arguments   = List(Code),
          resolve     = c => c.ctx.country.fetchByCode(c.arg(Code)).toIO.unsafeToFuture
        ),

        Field(
          name        = "countries",
          fieldType   = ListType(CountryType[F]),
          description = Some("Returns all countries."),
          resolve     = c => c.ctx.country.fetchAll.toIO.unsafeToFuture
        ),

      )
    )

  def schema[F[_]: Effect]: Schema[MasterRepo[F], Unit] =
    Schema(QueryType[F])

}
