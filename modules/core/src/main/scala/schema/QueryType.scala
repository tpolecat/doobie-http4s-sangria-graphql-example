// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect._
import cats.effect.std.Dispatcher
import demo.repo._
import sangria.schema._

object QueryType {

  val NamePattern: Argument[String] = Argument(name = "namePattern",
                                               argumentType = OptionInputType(StringType),
                                               description =
                                                 "SQL-style pattern for city name, like \"San %\".",
                                               defaultValue = "%"
                                              )

  val Code: Argument[String] =
    Argument(name = "code", argumentType = StringType, description = "Unique code of a country.")

  def apply[F[_]: Async](dispatcher: Dispatcher[F]): ObjectType[MasterRepo[F], Unit] =
    ObjectType(
      name = "Query",
      fields = fields(
        Field(
          name        = "cities",
          fieldType   = ListType(CityType[F](dispatcher)),
          description = Some("Returns cities with the given name pattern, if any."),
          arguments   = List(NamePattern),
          resolve     = c => dispatcher.unsafeToFuture(c.ctx.city.fetchAll(c.argOpt(NamePattern)))
        ),
        Field(
          name        = "country",
          fieldType   = OptionType(CountryType[F](dispatcher)),
          description = Some("Returns the country with the given code, if any."),
          arguments   = List(Code),
          resolve     = c => dispatcher.unsafeToFuture(c.ctx.country.fetchByCode(c.arg(Code)))
        ),
        Field(
          name        = "countries",
          fieldType   = ListType(CountryType[F](dispatcher)),
          description = Some("Returns all countries."),
          resolve     = c => dispatcher.unsafeToFuture(c.ctx.country.fetchAll)
        )
      )
    )

  def schema[F[_]: Async](dispatcher: Dispatcher[F]): Schema[MasterRepo[F], Unit] =
    Schema(QueryType[F](dispatcher))

}
