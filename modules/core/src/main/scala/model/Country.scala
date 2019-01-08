// Copyright (c) 2018 by Rob Norris
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package demo.model

final case class Country(
  id:             String,
  name:            String,
  continent:       String,
  region:          String,
  surfacearea:     Float,
  indepyear:       Short,
  population:      Int,
  lifeexpectancy:  Float,
  gnp:             BigDecimal,
  gnpold:          BigDecimal,
  localname:       String,
  governmentform:  String,
  headofstate:     String,
  capitalId:       Int,
)