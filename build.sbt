organization := "com.kalmanb"
            
name := "sbt-git-dependencies"

version := "0.2.0"

sbtPlugin := true

publishMavenStyle := false

publishArtifact in Test := false

// sbt 13
sbtVersion in Global := "0.13.0"

//publishTo := Some(Resolver.url("repo", url("http://"))(Resolver.ivyStylePatterns))

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.0.RC1-SNAP4" % "test",
    "org.scalaz" %% "scalaz-core" % "7.0.3", 
    "junit" % "junit" % "4.11" % "test"
)


