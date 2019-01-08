// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats._
import doobie._
import doobie.implicits._
import fs2._
import demo.model._

trait CityRepo[F[_]] {
  def fetchById(id: Int): F[Option[City]]
  def fetchAll: Stream[F, City]
}

object CityRepo {

  def fromTransactor[F[_]: Monad](xa: Transactor[F]): CityRepo[F] =
    new CityRepo[F] {

      val select: Fragment =
        fr"""
          select id, name, country_id, district, population
          from cities
        """

      def fetchById(id: Int): F[Option[City]] =
        (select ++ sql"where id = $id").query[City].option.transact(xa)

      def fetchAll: Stream[F, City] =
        select.query[City].stream.transact(xa)

    }

}