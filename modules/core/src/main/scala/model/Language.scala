package model

final case class Language(
  countryId:  String,
  language:   String,
  isOfficial: Boolean,
  percentage: Float
)
