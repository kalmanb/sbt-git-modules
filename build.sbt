organization := "com.kalmanb"
            
name := "sbt-git-modules"

version := "0.1.0-SNAPSHOT"

sbtPlugin := true

publishMavenStyle := false

publishArtifact in Test := false

// sbt 13
//sbtVersion in Global := "0.13.0-RC5"

// sbt 13
// scalaVersion in Global := "2.10.2"
scalaVersion in Global := "2.9.2"


libraryDependencies ++= Seq(
  "org.scalaz"  %% "scalaz-core"  % "6.0.4", 
  "org.scalatest" %% "scalatest" % "2.0.M6-SNAP3" % "test",
  "junit" % "junit" % "4.11" % "test"
)
// sbt 13 - dependencies
// "org.scalatest" %% "scalatest" % "2.0.RC1-SNAP4" % "test",
// "net.databinder.dispatch" %% "dispatch-core" % "0.11.0"


