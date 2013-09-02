package com.kalmanb.sbt

import sbt._
import sbt.Keys._
import scalaz.Success
import scalaz.Failure
import sbt.Load.BuildStructure
import scalaz.NonEmptyList

object GitModulesPlugin extends Plugin {
  import SbtExecutor._

  case class LocalProject(name: String, organization: String, version: String, dependencies: Seq[LocalProject])

  val kalKey = TaskKey[Unit]("kal")
  val gitModuleSettings = Seq[Setting[_]](
    kalKey <<= (loadedBuild, buildStructure, state) map {
      (level, structure, state) ⇒

        val modules =  for {
          ref ← structure.allProjectRefs
        } yield {
          val dependencies: Validation[Seq[ModuleID]] = externalDependencies(ref, state)
          dependencies map { validation ⇒
              validation  map (module ⇒ LocalProject(module.name, module.organization, module.revision, List.empty))
          }
        }
        println(modules)

        println("***************** Getting Projects")
        val projects = for {
          ref ← getStructure(state).allProjectRefs // ProjectRef
          project ← Project.getProject(ref, getStructure(state)) // ResolvedProject
        } yield project.id
        println(projects)

        val localProjects = for {
          ref ← structure.allProjectRefs // ProjectRef
          project ← Project.getProject(ref, structure) // ResolvedProject
        } yield {
          project.settings map { setting ⇒
            val label = setting.key.key.label
            if (label == "version") {
              val value = Project.extract(state).getOpt(SettingKey(setting.key.key))
              value match {
                case Some(version: String) ⇒ Some(LocalProject(project.id, "com.kalmanb", version, List.empty))
                case None                  ⇒ None
              }
            } else None
          }
        }
        val tt = localProjects.flatten.flatten
        println(tt)
    }
  )

  def getCurrentBranch = {
    import scala.sys.process._
    "git rev-parse --abbrev-ref HEAD".!!
  }

  def getCurrentTicket = """([0-9]+)""".r.findFirstMatchIn(getCurrentBranch).map(_ group 1).get

  def externalDependencies(ref: ProjectRef, state: State): Validation[Seq[ModuleID]] = {
    evaluateTask(Keys.update in configuration, ref, state) map { updateReport ⇒
      for {
        configurationReport ← (updateReport configuration "test").toSeq
        moduleReport ← configurationReport.modules
      } yield moduleReport.module
    }
  }
}

object SbtExecutor {
  import scalaz.Scalaz._

  type Validation[A] = scalaz.Validation[NonEmptyList[String], A]
  def getStructure(state: State): BuildStructure = Project.extract(state).structure

  //def setting[A](key: SettingKey[A], state: State): Validation[A] =
  //key get getStructure(state).data match {
  //case Some(a) ⇒ a.success
  //case None    ⇒ "Undefined setting '%s'!".format(key.key).failNel
  //}

  def evaluateTask[A](key: TaskKey[A], ref: ProjectRef, state: State): Validation[A] = {
    val result: Validation[A] = EvaluateTask(getStructure(state), key, state, ref, EvaluateTask defaultConfig state) match {
      case Some((_, Value(a))) ⇒ a.success // success from import scalaz.Scalaz._
      case Some((_, Inc(inc))) ⇒ "Error evaluating task '%s': %s".format(key.key, Incomplete.show(inc.tpe)).failNel
      case None                ⇒ "Undefined task '%s' for '%s'!".format(key.key, ref.project).failNel
    }
    result
  }
}

