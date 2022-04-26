// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect._
import cats.effect.implicits._
import demo.repo._
import sangria.schema._

object MutationType {

  val NewName: Argument[String] =
    Argument(
      name         = "newName",
      argumentType = StringType,
      description  = "New name for the specified country.",
    )

  val Code: Argument[String] =
    Argument(
      name         = "code",
      argumentType = StringType,
      description  = "Unique code of a country."
    )

  def apply[F[_]: Effect]: ObjectType[MasterRepo[F], Unit] =
    ObjectType(
      name  = "Mutation",
      fields = fields(

        Field(
          name        = "updateCountry",
          fieldType   = OptionType(CountryType[F]),
          description = Some("Update the specified Country, if it exists."),
          arguments   = List(Code, NewName),
          resolve     = c => c.ctx.country.update(c.arg(Code), c.arg(NewName)).toIO.unsafeToFuture()
        ),

      )
    )

}
