package repo

import cats._
import doobie._
import doobie.implicits._
import fs2._
import model._

trait CityRepo[F[_]] {
  def fetchById(id: Int): F[Option[City]]
  def fetchAll: Stream[F, City]
}

object CityRepo {

  def fromTransactor[F[_]: Monad](xa: Transactor[F]): CityRepo[F] =
    new CityRepo[F] {

      val select: Fragment =
        fr"""
          select id, name, countrycode, district, population
          from city
        """

      def fetchById(id: Int): F[Option[City]] =
        (select ++ sql"where id = $id").query[City].option.transact(xa)

      def fetchAll: Stream[F, City] =
        select.query[City].stream.transact(xa)

    }

}