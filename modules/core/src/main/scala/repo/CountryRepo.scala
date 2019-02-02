// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats._
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import fs2.Stream
import demo.model._
import io.chrisdavenport.log4cats.Logger

trait CountryRepo[F[_]] {
  def fetchByCode(code: String): F[Option[Country]]
  def fetchAll: Stream[F, Country]
  def fetchByCodes(codes: List[String]): Stream[F, Country]
}

object CountryRepo {

  def fromTransactor[F[_]: Monad: Logger](xa: Transactor[F]): CountryRepo[F] =
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

      def fetchByCodes(codes: List[String]): Stream[F, Country] =
        Stream.eval_(Logger[F].info(s"CountryRepo.fetchByCodes(${codes.length} codes)")) ++ {
          NonEmptyList.fromList(codes) match {
            case Some(nel) => (select ++ fr"where" ++ Fragments.in(fr"code", nel)).query[Country].stream.transact(xa)
            case None      => Stream.empty[F]
          }
        }

      def fetchAll: Stream[F, Country] =
        Stream.eval_(Logger[F].info(s"CountryRepo.fetchAll")) ++
        select.query[Country].stream.transact(xa)

    }

}