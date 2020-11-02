package akka.streams

import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Concat, Keep, Merge, Sink, Source}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class SourceSpec extends AsyncWordSpec with Matchers {

  implicit val system = ActorSystem("akka-streams-source-playground")

  val oneTwoThreeSource = Source(1 to 3)
  val fourFiveSixSource = Source(4 to 6)
  val sevenEightNineSource = Source(7 to 9)

  "Akka Streams Source" should {
    "use Source.asSourceWithContext to provide a context for each element" in {
      val graph = Source(List("Hello", "Bonjour"))
        .map { e => println; e }
        .asSourceWithContext { elem =>
          if (elem == "Hello") StringContext(s"English Greeting")
          else StringContext("Non-English Greeting")
        }
        .map(_.toUpperCase)
        .toMat(Sink.seq)(Keep.right)

      val futureResult = graph.run()

      Await.result(futureResult, 1 second) shouldBe Vector(
        ("HELLO", StringContext("English Greeting")),
        ("BONJOUR", StringContext("Non-English Greeting"))
      )
    }

    "use Source.combine with CONCAT to combine sources and maintain individual order of each" in {

      // Source.combine is most useful for more that 2 sources, or custom merging
      // When merging 2 sources use (instance method) source.concat or source.merge

      val sourceCombiner = Source
        .combine(oneTwoThreeSource, fourFiveSixSource, sevenEightNineSource)(Concat(_))
        .toMat(Sink.seq)(Keep.right)

      val futureResult = sourceCombiner.run()

      // Concat completes one source before starting the next
      Await.result(futureResult, 1 second) shouldBe List(1, 2, 3, 4, 5, 6, 7, 8, 9)
    }

    "use Source.combine with MERGE to combine sources and NOT maintain individual order of each" in {

      // Source.combine is most useful for more that 2 sources, or custom merging
      // When merging 2 sources use source.combine or source.merge

      val sourceCombiner = Source
        .combine(oneTwoThreeSource, fourFiveSixSource, sevenEightNineSource)(Merge(_))
        .toMat(Sink.seq)(Keep.right)

      val futureResult = sourceCombiner.run()

      // Merge does NOT complete one source before starting the next, so mixes elements from each source
      // although elements from each source will maintain order
      val result = Await.result(futureResult, 1 second)
      result should not be List(1, 2, 3, 4, 5, 6, 7, 8, 9)
      result should contain theSameElementsAs List(1, 2, 3, 4, 5, 6, 7, 8, 9)
    }

    "use Source concat instance method for combining 2 sources by completing the first, then starting the second" in {

      val futureResult = oneTwoThreeSource.concat(fourFiveSixSource).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe List(1, 2, 3, 4, 5, 6)
    }

    "use Source merge instance method for combining 2 sources by immediately outputting elements from both" in {

      val futureResult = oneTwoThreeSource.merge(fourFiveSixSource).runWith(Sink.seq)

      val result = Await.result(futureResult, 1 second)
      result should not be List(1, 2, 3, 4, 5, 6)
      result should contain theSameElementsAs List(1, 2, 3, 4, 5, 6)
    }

    "use Source.cycle to repeat stream elements" in {
      val futureResult = Source.cycle(() => List(1, 2, 3).iterator)
        .grouped(9)
        .runWith(Sink.head)

      Await.result(futureResult, 1 second) shouldBe List(1, 2, 3, 1, 2, 3, 1, 2, 3)
    }

    "use Source.cycle to repeat stream elements but fail for empty iterator" in {
      val futureResult = Source.cycle(() => Iterator.empty)
        .grouped(9)
        .runWith(Sink.head)

      futureResult.failed.futureValue shouldBe a[IllegalArgumentException]
      futureResult.failed.futureValue.getMessage shouldBe "empty iterator"
    }

    "use Source.empty to complete immediately without emitting any elements (use to satisfy API's that need a Source)" in {
      val futureResult = Source.empty[Int]
        .runWith(Sink.lastOption)

      Await.result(futureResult, 1 second) shouldBe None
    }

    "use Source.failed to fail the source directly with the given exception" in {
      val futureFailure = Source.failed(new RuntimeException("Something bit ME!!"))
        .runWith(Sink.head)

      futureFailure.failed.futureValue shouldBe a[RuntimeException]
      futureFailure.failed.futureValue.getMessage shouldBe "Something bit ME!!"
    }

    "use Source.apply to stream the values of an immutable Seq" in {
      // Source(Seq(1, 2, 3)) calls apply()
      val futureResult = Source(Seq(1, 2, 3)).runWith(Sink.seq)

      Await.result(futureResult, 10 second) shouldBe Seq(1,2,3)
    }

    "use Source.fromIterator to emit next value from iterator and complete at end of the iterator" in {
      val futureResult = Source.fromIterator(() => ('A' to 'C').iterator).runWith(Sink.seq)

      Await.result(futureResult, 10 second) shouldBe Seq('A','B','C')

    }

    "use Source.fromJavaStream to stream values from a Java 8 Stream" in {
      val futureResult = Source.fromJavaStream(() => IntStream.rangeClosed(1, 3)).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(1,2,3)
    }

    "use Source.future to send the single value of the future when it completes successfully and there is demand" in {
      val futureResult = Source.future(Future.successful(27)).runWith(Sink.head)

      Await.result(futureResult, 1 seconds) shouldBe 27
//      futureResult.futureValue shouldBe 27 // TODO update all test with this syntax???
    }

    "use Source.future to send the exception thrown by the future when it fails to complete" in {
      val futureResult = Source.future(Future.failed(new RuntimeException("Bang!"))).runWith(Sink.ignore)

      futureResult.failed.futureValue shouldBe a [RuntimeException]
      futureResult.failed.futureValue.getMessage shouldBe "Bang!"
    }

    "use Source.futureSource to stream the elements of the future source when it completes successfully" in {
      val futureResult = Source.futureSource(Future.successful(Source(List(10,9,8,7)))).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(10,9,8,7)
    }

    "use Source.futureSource to send the exception thrown by the future source when it fails to complete" in {
      val futureResult = Source.futureSource(Future.failed(new RuntimeException("Busted!"))).runWith(Sink.ignore)

      futureResult.failed.futureValue shouldBe a [RuntimeException]
      futureResult.failed.futureValue.getMessage shouldBe "Busted!"
    }

    // TODO NEXT lazyCompletionStage: https://doc.akka.io/docs/akka/current/stream/operators/Source/lazyCompletionStage.html
  }
}
