// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.schema

import cats.effect.Async
import cats.effect.std.Dispatcher
import demo.model._
import demo.repo._
import sangria.schema._

object CityType {

  def apply[F[_]: Async](dispatcher: Dispatcher[F]): ObjectType[MasterRepo[F], City] =
    ObjectType(
      name = "City",
      fieldsFn = () =>
        fields(
          Field(name        = "name",
                fieldType   = StringType,
                description = Some("City name."),
                resolve     = _.value.name
               ),
          Field(
            name        = "country",
            fieldType   = CountryType[F](dispatcher),
            description = Some("Country in which this city resides."),
            resolve     = e => CountryType.Deferred.ByCode(e.value.countryCode)
          ),
          Field(name        = "district",
                fieldType   = StringType,
                description = Some("District in which this city resides."),
                resolve     = _.value.district
               ),
          Field(name        = "population",
                fieldType   = IntType,
                description = Some("City population."),
                resolve     = _.value.population
               )
        )
    )

}
