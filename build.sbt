lazy val V = new {
  lazy val frees    = "0.4.6"
  lazy val freesRPC = "0.4.0"
  lazy val circe    = "0.9.0-M1"
  lazy val monix    = "3.0.0-M1"
}

lazy val `rpc-test` = project
  .in(file("testservice"))
  .settings(name := "rpc-test")
  .settings(moduleName := "rpc-test")
  .settings(Seq(
    scalaVersion := "2.12.3",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("beyondthelines", "maven")
    ),
    libraryDependencies ++= Seq(
      "io.frees" %% "frees-rpc"               % V.freesRPC,
      "io.frees" %% "frees-async-cats-effect" % V.frees,
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % V.circe)
  ): _*)
  .settings(
    Seq(
      addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
      libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
      scalacOptions += "-Xplugin-require:macroparadise",
      scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise")) // macroparadise plugin doesn't work in repl yet.
    ): _*
  )
