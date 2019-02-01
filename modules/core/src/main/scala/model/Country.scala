// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.model

final case class Country(
  code:            String,
  name:            String,
  continent:       String,
  region:          String,
  surfacearea:     Float,
  indepyear:       Option[Short],
  population:      Int,
  lifeexpectancy:  Option[Float],
  gnp:             Option[BigDecimal],
  gnpold:          Option[BigDecimal],
  localname:       String,
  governmentform:  String,
  headofstate:     Option[String],
  capitalId:       Option[Int],
  code2:           String
)