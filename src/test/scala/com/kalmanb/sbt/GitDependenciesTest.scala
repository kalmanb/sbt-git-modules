package com.kalmanb.sbt

import java.io.File
import scala.sys.process._

import org.scalatest._

class GitDependenciesTest extends FunSpec with BeforeAndAfterAll with ShouldMatchers {
  import GitDependencies._

  /**
   *     C---        <-- two
   *    /    \
   *   B--D--F       <-- one
   *  /       \
   * A------E--G--H  <-- master
   *
   * Creation Order : A-B-C-D-E-F-G-H
   *
   * Order of precidence on master should be:
   * H-G-F-C-D-B-E-A
   */

  val Dir = "git-tmp-test"
  var mergeCommitSha = ""
  var A, B, C, D, E, H = ""

  override def beforeAll() {
    "mkdir -p %s".format(Dir).!!
    doo("git init")
    commit("A")
    A = doo("git log --format=%H -1").trim
    doo("git checkout -b one")
    commit("B")
    B = doo("git log --format=%H -1").trim
    doo("git checkout -b two")
    commit("C")
    C = doo("git log --format=%H -1").trim
    doo("git checkout one")
    commit("D")
    D = doo("git log --format=%H -1").trim
    doo("git checkout master")
    commit("E")
    E = doo("git log --format=%H -1").trim
    doo("git checkout one")
    doo("git merge two")
    doo("git checkout master")
    doo("git merge one")
    mergeCommitSha = doo("git log --format=%H -1").trim
    commit("H")
    H = doo("git log --format=%H -1").trim
  }

  def commit(ver: String) = {
    doo("touch " + ver)
    doo("git add .")
    doo("git commit -a -m " + ver)
    doo("git tag " + ver)
  }

  def doo(cmd: String): String = {
    implicit def stringToProcess(cmd: String) = Process(cmd, Some(new File(Dir)))
    cmd.!!
  }

  describe("git commit on the current branch") {
    it("should select the correct newer branch") {
      // Should be : H-G-F-C-D-B-E-A
      val shas = getCommitsInOrder(new File(Dir)).toList
      shas(0) should be(getShaForTag("H"))
      shas(1) should be(mergeCommitSha)
      shas(3) should be(getShaForTag("C"))
      shas(4) should be(getShaForTag("D"))
      shas(5) should be(getShaForTag("B"))
      shas(6) should be(getShaForTag("E"))
    }

    it("Should sort given commits") {
      val sorted = sortCommits(List(A, B, C, D, E, H), new File(Dir))

      // Should be : H-G-F-C-D-B-E-A
      sorted(H) should be(0)
      sorted(C) should be(1)
      sorted(D) should be(2)
      sorted(B) should be(3)
      sorted(E) should be(4)
      sorted(A) should be(5)
    }
  }

  def getShaForTag(tag: String): String =
    doo("git rev-parse %s".format(tag)).trim

  override def afterAll() {
    "rm -rf %s".format(Dir).!!
  }
}
