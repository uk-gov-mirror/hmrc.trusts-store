import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "trusts-store"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*Reverse.*;.*.Routes.*;prod.*;testOnlyDoNotUseInProd.*;testOnlyDoNotUseInAppConf.*;" +
      ".*BuildInfo.*;app.*;prod.*;config.*;.*ClaimedTrustsRepository;.*AppConfig;utils.*",
    ScoverageKeys.coverageMinimum := 40,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .configs(IntegrationTest)
  .settings(
    inConfig(IntegrationTest)(itSettings),
    publishingSettings ++ scoverageSettings,
    resolvers += Resolver.jcenterRepo,
    RoutesKeys.routesImport += "models.FeatureFlagName",
    PlayKeys.playDefaultPort := 9783,
    majorVersion := 0,
    scalaVersion := "2.12.12",
    SilencerSettings(),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )

 lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories   := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution            := false,
  fork                         := true,
  javaOptions                  ++= Seq(
    "-Dconfig.resource=it.application.conf"
  )
)

dependencyOverrides ++= AppDependencies.overrides