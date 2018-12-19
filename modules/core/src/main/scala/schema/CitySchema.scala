// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package schema

import cats.effect._
import cats.effect.implicits._
import model._
import repo._
import sangria.schema._

object CitySchema {

  val CityType: ObjectType[Unit, City] =
    ObjectType(
      name = "City",
      fields[Unit, City](
        Field("id",         IntType,    resolve = _.value.id),
        Field("name",       StringType, resolve = _.value.name),
        Field("district",   StringType, resolve = _.value.district),
        Field("population", IntType,    resolve = _.value.population)
      )
    )

  val Id = Argument("id", IntType)

  def queryType[F[_]: Effect] =
    ObjectType(
      name  = "Query",
      fields = fields[CityRepo[F], Unit](

        Field(
          name        = "city",
          fieldType   = OptionType(CityType),
          description = Some("Returns the City with the given id, if any."),
          arguments   = List(Id),
          resolve     = c => c.ctx.fetchById(c.arg(Id)).toIO.unsafeToFuture
        ),

        Field(
          name        = "cities",
          fieldType   = ListType(CityType),
          description = Some("Returns all cities."),
          resolve     = c => c.ctx.fetchAll.compile.to[List].toIO.unsafeToFuture
        ),

      )
    )

  def schema[F[_]: Effect]: Schema[CityRepo[F], Unit] =
    Schema(queryType[F])

}
