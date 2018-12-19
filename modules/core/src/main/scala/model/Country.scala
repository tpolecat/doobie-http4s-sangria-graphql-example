package model

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