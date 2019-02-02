// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats._
import cats.data._
import cats.implicits._
import doobie._
import doobie.implicits._
import demo.model._
import io.chrisdavenport.log4cats.Logger

trait LanguageRepo[F[_]] {
  def fetchByCountryCode(code: String): F[List[Language]]
  def fetchByCountryCodes(codes: List[String]): F[Map[String, List[Language]]]
}

object LanguageRepo {

  def fromTransactor[F[_]: Monad: Logger](xa: Transactor[F]): LanguageRepo[F] =
    new LanguageRepo[F] {

      val select: Fragment =
        fr"""
          SELECT countrycode, language, isOfficial, percentage
          FROM   countrylanguage
        """

      def fetchByCountryCode(code: String): F[List[Language]] =
        Logger[F].info(s"LanguageRepo.fetchByCountryCode($code)") *>
        (select ++ sql"where countrycode = $code").query[Language].to[List].transact(xa)

      def fetchByCountryCodes(codes: List[String]): F[Map[String, List[Language]]] =
        NonEmptyList.fromList(codes) match {
          case None      => Map.empty[String, List[Language]].pure[F]
          case Some(nel) =>
            Logger[F].info(s"LanguageRepo.fetchByCountryCodes(${codes.length} codes)") *>
            (select ++ fr"where" ++ Fragments.in(fr"countrycode", nel))
              .query[Language]
              .to[List]
              .map { ls =>
                // Make sure we include empty lists for countries with no languages
                codes.foldRight(ls.groupBy(_.countryCode)) { (c, m) =>
                  Map(c -> List.empty[Language]) |+| m
                }
              }
              .transact(xa)
        }

    }

}