// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.repo

import cats._
import doobie._
import doobie.implicits._
import fs2.Stream
import demo.model._
import io.chrisdavenport.log4cats.Logger

trait LanguageRepo[F[_]] {
  def fetchByCountryCode(code: String): Stream[F, Language]
}

object LanguageRepo {

  def fromTransactor[F[_]: Monad: Logger](xa: Transactor[F]): LanguageRepo[F] =
    new LanguageRepo[F] {

      val select: Fragment =
        fr"""
          SELECT countrycode, language, isOfficial, percentage
          FROM   countrylanguage
        """

      def fetchByCountryCode(code: String): Stream[F, Language] =
        Stream.eval_(Logger[F].info(s"LanguageRepo.fetchByCountryCode($code)")) ++
        (select ++ sql"where countrycode = $code").query[Language].stream.transact(xa)

    }

}