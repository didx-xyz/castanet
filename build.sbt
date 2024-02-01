val Scala3   = "3.3.1"
val Scala213 = "2.13.8"

val catsVersion          = "2.10.0"
val ceVersion            = "3.5.3"
val fs2Version           = "3.9.4"
val munitVersion         = "1.0.0-M10"
val munitCEVersion       = "1.0.7"
val munitCheckEffVersion = "1.0.0-M7"
val googleProtoVersion   = "3.19.1"
val circeVersion         = "0.14.6"
val circeYamlVersion     = "0.14.2"
val monocleVersion       = "3.2.0"
val scodecVersion        = "1.1.38"
val junitVersion         = "0.11"
val refinedVersion       = "0.9.27"

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion      := Scala3
ThisBuild / version           := "0.1.10"

ThisBuild / organization         := "xyz.didx"
ThisBuild / organizationName     := "DIDx"
ThisBuild / organizationHomepage := Some(url("https://didx.co.za/"))

ThisBuild / developers := List(
  Developer(
    id = "iandebeer",
    name = "Ian de Beer",
    email = "ian.debeer@didx.co.za ",
    url = url("https://didx.co.za/")
  )
)

ThisBuild / description := "Coloured Petri for Scala3"
ThisBuild / licenses := List(
  "MIT License" -> new URI("https://tldrlegal.com/license/mit-license#summary").toURL
)
ThisBuild / homepage := Some(url("https://github.com/didx-xyz/castanet"))

// Publishing configuration
ThisBuild / publishTo := Some {
  if (isSnapshot.value)
    "GitHub Package Registry Snapshots" at "https://maven.pkg.github.com/didx-xyz/castanet"
  else
    "GitHub Package Registry Releases" at "https://maven.pkg.github.com/didx-xyz/castanet"
}
ThisBuild / versionScheme    := Some("early-semver")
ThisBuild / githubOwner      := "didx-xyz"
ThisBuild / githubRepository := "castanet"

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource.GitConfig(
  "github.token"
)
pomIncludeRepository := { _ =>
  false
} // Remove all additional repository other than Maven Central from POM
publishMavenStyle := true

lazy val root = project
  .in(file("."))
  .aggregate(core)
  .settings(
    scalafixSettings,
    publish / skip := true
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(
    scalafixSettings,
    name                      := "castanet",
    publishConfiguration      := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    libraryDependencies ++= Seq(
      "org.typelevel"  %% "cats-core"           % catsVersion,
      "co.fs2"         %% "fs2-core"            % fs2Version,
      "co.fs2"         %% "fs2-io"              % fs2Version,
      "org.typelevel"  %% "cats-effect"         % ceVersion,
      "dev.optics"     %% "monocle-core"        % monocleVersion,
      "org.scodec"     %% "scodec-bits"         % scodecVersion,
      "org.scala-lang" %% "scala3-staging"      % Scala3,
      "io.circe"       %% "circe-yaml"          % circeYamlVersion,
      "org.scalameta"  %% "munit"               % munitVersion   % Test,
      "org.scalameta"  %% "munit-scalacheck"    % munitVersion   % Test,
      "org.typelevel"  %% "munit-cats-effect-3" % munitCEVersion % Test
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )

lazy val scalafixSettings = Seq(semanticdbEnabled := true)
