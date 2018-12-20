package org.scalasteward.core.github.data

import cats.effect.IO
import io.circe.parser
import org.http4s.Uri
import org.scalasteward.core.git.Branch
import org.scalatest.{FunSuite, Matchers}
import scala.io.Source
import org.scalasteward.core.application.ConfigTest

class RepoOutTest extends FunSuite with Matchers {
  val parent =
    RepoOut(
      "base.g8",
      UserOut("ChristopherDavenport"),
      None,
      Uri.uri("https://github.com/ChristopherDavenport/base.g8.git"),
      Branch("master")
    )

  val fork =
    RepoOut(
      "base.g8-1",
      UserOut("scala-steward"),
      Some(parent),
      Uri.uri("https://github.com/scala-steward/base.g8-1.git"),
      Branch("master")
    )

  test("decode") {
    val input = Source.fromResource("create-fork.json").mkString
    parser.decode[RepoOut](input) shouldBe Right(fork)
  }

  test("parentOrRaise") {
    fork.parentOrRaise[IO](ConfigTest.dummyConfig).unsafeRunSync() shouldBe parent
  }

  test("parentOrRaise should not raise an error if forking is disabled") {
    parent.parentOrRaise[IO](ConfigTest.dummyConfig.copy(doNotFork = true)).unsafeRunSync()
  }

  test("repo") {
    fork.repo shouldBe Repo("scala-steward", "base.g8-1")
  }
}
