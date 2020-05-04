// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats.data._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import demo.model._
import io.chrisdavenport.log4cats.Logger

trait CountryRepo[F[_]] {
  def fetchByCode(code: String): F[Option[Country]]
  def fetchAll: F[List[Country]]
  def fetchByCodes(codes: List[String]): F[List[Country]]
  def update(code: String, newName: String): F[Option[Country]]
}

object CountryRepo {

  def fromTransactor[F[_]: Sync: Logger](xa: Transactor[F]): CountryRepo[F] =
    new CountryRepo[F] {

      val select: Fragment =
        fr"""
          SELECT code, name, continent, region, surfacearea, indepyear, population,
                 lifeexpectancy, gnp, gnpold, localname, governmentform, headofstate,
                 capital, code2
          FROM   Country
        """

      def fetchByCode(code: String): F[Option[Country]] =
        Logger[F].info(s"CountryRepo.fetchByCode($code)") *>
        (select ++ sql"where code = $code").query[Country].option.transact(xa)

      def fetchByCodes(codes: List[String]): F[List[Country]] =
        NonEmptyList.fromList(codes) match {
          case None      => List.empty[Country].pure[F]
          case Some(nel) =>
            Logger[F].info(s"CountryRepo.fetchByCodes(${codes.length} codes)") *>
            (select ++ fr"where" ++ Fragments.in(fr"code", nel)).query[Country].to[List].transact(xa)
        }

      def fetchAll: F[List[Country]] =
        Logger[F].info(s"CountryRepo.fetchAll") *>
        select.query[Country].to[List].transact(xa)

      def update(code: String, newName: String): F[Option[Country]] =
        Logger[F].info(s"CountryRepo.update") *> {
          sql"UPDATE country SET name = $newName WHERE code = $code".update.run *>
          (select ++ sql"where code = $code").query[Country].option
        } .transact(xa)

    }

}
