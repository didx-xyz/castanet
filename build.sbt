lazy val Scala3   = "3.4.2"
lazy val Scala213 = "2.13.8"

Global / scalaVersion         := Scala3
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / version := "0.1.11"

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

lazy val catsVersion      = "2.12.0"
lazy val ceVersion        = "3.5.5"
lazy val fs2Version       = "3.10.2"
lazy val munitVersion     = "1.0.0"
lazy val munitCEVersion   = "2.0.0"
lazy val circeVersion     = "0.14.8"
lazy val circeYamlVersion = "0.14.2"
lazy val monocleVersion   = "3.2.0"
lazy val scodecVersion    = "1.2.0"

lazy val core = project
  .in(file("modules/core"))
  .settings(
    scalafixSettings,
    name                      := "castanet",
    publishConfiguration      := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"         % catsVersion,
      "co.fs2"        %% "fs2-core"          % fs2Version,
      "co.fs2"        %% "fs2-io"            % fs2Version,
      "org.typelevel" %% "cats-effect"       % ceVersion,
      "dev.optics"    %% "monocle-core"      % monocleVersion,
      "org.scodec"    %% "scodec-bits"       % scodecVersion,
      "io.circe"      %% "circe-yaml"        % circeYamlVersion,
      "org.scalameta" %% "munit"             % munitVersion   % Test,
      "org.scalameta" %% "munit-scalacheck"  % munitVersion   % Test,
      "org.typelevel" %% "munit-cats-effect" % munitCEVersion % Test
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )

lazy val scalafixSettings = Seq(semanticdbEnabled := true)
