lazy val catsEffectVersion    = "2.5.4"
lazy val catsVersion          = "2.6.1"
lazy val circeVersion         = "0.14.1"
lazy val doobieVersion        = "0.13.4"
lazy val fs2Version           = "2.5.10"
lazy val kindProjectorVersion = "0.13.2"
lazy val log4catsVersion      = "1.1.1"
lazy val sangriaCirceVersion  = "1.3.0"
lazy val sangriaVersion       = "2.1.6"
lazy val scala13Version       = "2.13.8"
lazy val http4sVersion        = "0.21.33"
lazy val slf4jVersion         = "1.7.30"

ThisBuild / scalaVersion := scala13Version
//ThisBuild / scalacOptions += "-P:semanticdb:synthetics:on"

lazy val scalacSettings = Seq(
  scalacOptions ++=
    Seq(
      "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8",                // Specify character encoding used by source files.
      "-explaintypes",                     // Explain type errors in more detail.
      "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
      "-language:postfixOps",
      "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds",             // Allow higher-kinded types
      "-language:implicitConversions",     // Allow definition of implicit functions called views
      "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code",                  // Warn when dead code is identified.
      "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen",              // Warn when numerics are widened.
      "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",              // Warn if a local definition is unused.
//      "-Ywarn-unused:params",              // Warn if a value parameter is unused.
      "-Ywarn-unused:privates",            // Warn if a private member is unused.
      "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
//      "-Ywarn-macros:before", // via som
      "-Ymacro-annotations",
      "-Yrangepos" // for longer squiggles
//      "-Ypartial-unification"
    )
  ,
  (Compile / console / scalacOptions) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports"),
)

lazy val commonSettings = scalacSettings ++ Seq(
  organization := "org.tpolecat",
  licenses ++= Seq(("MIT", url("http://opensource.org/licenses/MIT"))),
  scalaVersion := scala13Version,
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerLicense  := Some(HeaderLicense.Custom(
    """|Copyright (c) 2018 by Rob Norris
       |This software is licensed under the MIT License (MIT).
       |For more information see LICENSE or https://opensource.org/licenses/MIT
       |""".stripMargin
  )),
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
)

lazy val doobie_sangria = project.in(file("."))
  .settings(commonSettings)
  .dependsOn(core)
  .aggregate(core)

lazy val core = project
  .in(file("modules/core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(
    name := "doobie-sangria-core",
    description := "Sangria example with doobie backend.",
    libraryDependencies ++= Seq(
      "org.typelevel"        %% "cats-core"           % catsVersion,
      "org.typelevel"        %% "cats-effect"         % catsEffectVersion,
      "co.fs2"               %% "fs2-core"            % fs2Version,
      "co.fs2"               %% "fs2-io"              % fs2Version,
      "org.sangria-graphql"  %% "sangria"             % sangriaVersion,
      "org.sangria-graphql"  %% "sangria-circe"       % sangriaCirceVersion,
      "org.tpolecat"         %% "doobie-core"         % doobieVersion,
      "org.tpolecat"         %% "doobie-postgres"     % doobieVersion,
      "org.tpolecat"         %% "doobie-hikari"       % doobieVersion,
      "org.http4s"           %% "http4s-dsl"          % http4sVersion,
      "org.http4s"           %% "http4s-blaze-server" % http4sVersion,
      "org.http4s"           %% "http4s-circe"        % http4sVersion,
      "io.circe"             %% "circe-optics"        % circeVersion,
      "io.chrisdavenport"    %% "log4cats-slf4j"      % log4catsVersion,
      "org.slf4j"            %  "slf4j-simple"        % slf4jVersion,
    )
  )
