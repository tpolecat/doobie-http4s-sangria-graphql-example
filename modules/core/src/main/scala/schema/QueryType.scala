// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect._
import cats.effect.implicits._
import demo.repo._
import sangria.schema._

object QueryType {

  val Id: Argument[Int] =
    Argument(
      name         = "id",
      argumentType = IntType,
      description  = "Unique id of a city."
    )

  val Code: Argument[String] =
    Argument(
      name         = "code",
      argumentType = StringType,
      description  = "Unique code of a country."
    )

  def apply[F[_]: Effect] =
    ObjectType(
      name  = "Query",
      fields = fields[MasterRepo[F], Unit](

        Field(
          name        = "city",
          fieldType   = OptionType(CityType[F]),
          description = Some("Returns the City with the given id, if any."),
          arguments   = List(Id),
          resolve     = c => c.ctx.city.fetchById(c.arg(Id)).toIO.unsafeToFuture
        ),

        Field(
          name        = "cities",
          fieldType   = ListType(CityType[F]),
          description = Some("Returns all cities."),
          resolve     = c => c.ctx.city.fetchAll.toIO.unsafeToFuture
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
