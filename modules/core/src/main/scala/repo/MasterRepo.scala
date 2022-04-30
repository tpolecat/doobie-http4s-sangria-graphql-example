// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats.effect._
import doobie._

final case class MasterRepo[F[_]](
  city:     CityRepo[F],
  country:  CountryRepo[F],
  language: LanguageRepo[F])

object MasterRepo {

  def fromTransactor[F[_]: Sync](
    xa: Transactor[F]
  ): MasterRepo[F] =
    MasterRepo(CityRepo.fromTransactor(xa),
               CountryRepo.fromTransactor(xa),
               LanguageRepo.fromTransactor(xa)
              )

}
