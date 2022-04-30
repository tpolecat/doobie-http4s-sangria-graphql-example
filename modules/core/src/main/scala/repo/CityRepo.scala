// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import demo.model._
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CityRepo[F[_]] {
  def fetchAll(pat: Option[String]): F[List[City]]
  def fetchByCountryCode(code: String): F[List[City]]
}

object CityRepo {

  def fromTransactor[F[_]: Sync](xa: Transactor[F]): CityRepo[F] =
    new CityRepo[F] {

      val select: Fragment = fr"""
          SELECT id, name, countrycode, district, population
          FROM   city
        """

      def fetchAll(pat: Option[String]): F[List[City]] =
        for {
          logger <- Slf4jLogger.create[F]
          result <- logger.info(s"CityRepo.fetchByNamePattern($pat)") *>
                    (select ++ pat
                      .foldMap(p => sql"WHERE name ILIKE $p")).query[City].to[List].transact(xa)
        } yield result

      def fetchByCountryCode(code: String): F[List[City]] =
        for {
          logger <- Slf4jLogger.create[F]
          result <- logger.info(s"CityRepo.fetchByCountryCode($code)") *>
                    (select ++ sql"WHERE countrycode = $code").query[City].to[List].transact(xa)
        } yield result

    }

}
