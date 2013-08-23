package com.kalmanb.sbt

import sbt._
import sbt.Keys._
import scalaz.Success
import scalaz.Failure
import sbt.Load.BuildStructure
import scalaz.NonEmptyList

object GitBuildPlugin extends Plugin {
  import SbtExecutor._

  val kalKey = TaskKey[Unit]("kal")
  val gitModuleSettings = Seq[Setting[_]](
    kalKey <<= (loadedBuild, buildStructure, state) map {
      (level, base, state) ⇒

        val aaaa: Seq[Validation[Seq[ModuleID]]] = for {
          ref ← getStructure(state).allProjectRefs
          project ← Project.getProject(ref, getStructure(state))
        } yield externalDependencies(ref, state)

        val dependencies = aaaa foreach { validation ⇒
          validation match {
            case Success(a)   ⇒ a foreach (module ⇒ println(module.organization + " " + module.name + " " + module.revision))
            case Failure(err) ⇒ println(err)
          }
        }
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

  def setting[A](key: SettingKey[A], state: State): Validation[A] =
    key get getStructure(state).data match {
      case Some(a) ⇒ a.success
      case None    ⇒ "Undefined setting '%s'!".format(key.key).failNel
    }

  def evaluateTask[A](key: TaskKey[A], ref: ProjectRef, state: State): Validation[A] = {
    val result: Validation[A] = EvaluateTask(getStructure(state), key, state, ref, EvaluateTask defaultConfig state) match {
      case Some((_, Value(a))) ⇒ a.success // success from import scalaz.Scalaz._
      case Some((_, Inc(inc))) ⇒ "Error evaluating task '%s': %s".format(key.key, Incomplete.show(inc.tpe)).failNel
      case None                ⇒ "Undefined task '%s' for '%s'!".format(key.key, ref.project).failNel
    }
    result
  }
}
