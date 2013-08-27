import sbt._
import sbt.Project._
import Keys._
import com.kalmanb.sbt.GitBuildPlugin._

object SbtJenkins extends Build {
  val projectName = "sbt-git-modules"
  val buildVersion = "0.1.0-SNAPSHOT"
  val Org = "com.kalmanb"

  lazy val root = Project(
    id = projectName,
    base = file("."),
    settings = Seq(
      version := buildVersion,
      commands ++= Seq(comm),
      libraryDependencies ++= Seq()
    ) ++ Project.defaultSettings ++ gitModuleSettings)

  lazy val moduleOne = Project(
    id = "moduleone",
    base = file("moduleone"),
    settings = Seq(
      organization := Org,
      version := buildVersion
    ) ++ defaultSettings
  )

  lazy val moduleTwo = Project(
    id = "moduletwo",
    base = file("moduletwo"),
    settings = Seq(
      organization := Org,
      version := buildVersion,  
      description := "testme",
      libraryDependencies ++= Seq(Org %% "moduleone" % buildVersion)
    ) ++ defaultSettings
  )

  def comm = Command.command("hi") { state ⇒
    println("hi")
    println(state)
    state
  }

}

