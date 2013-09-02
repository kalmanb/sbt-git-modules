package com.kalmanb.sbt

import java.io.File

object GitDependencies {

  /**
   * Returns Map or [Sha, Priority] lower priority is most recent
   */
  def sortCommits(shas:Seq[String], repoDir: File = new File(".")): Map[String, Int] = {
    def work(remainingShas: Seq[String], ordered: Stream[String], current: Map[String, Int]): Map[String, Int] = {
      if(remainingShas.isEmpty)
        current
      else if (remainingShas.contains(ordered.head))
        work(remainingShas.filterNot(_ == ordered.head), ordered.tail, current + (ordered.head -> current.size))
      else
        work(remainingShas.filterNot(_ == ordered.head), ordered.tail, current)
    }
    work(shas.distinct, getCommitsInOrder(repoDir), Map.empty)
  }

  def getCommitsInOrder(repoDir: File = new File(".")): Stream[String] = {
    import scala.sys.process._
    Process("git log --format=%H --topo-order", repoDir).lines
  }
  
}
