package akka.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Concat, Keep, Merge, Sink, Source}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SourceSpec extends AsyncWordSpec with Matchers {

  implicit val system = ActorSystem("akka-streams-source-playground")

  val oneTwoThreeSource = Source(1 to 3)
  val fourFiveSixSource = Source(4 to 6)
  val sevenEightNineSource = Source(7 to 9)

  "Akka Streams Source" should {
    "provide a context for each element when using asSourceWithContext" in {
      val graph = Source(List("Hello", "Bonjour"))
        .map{ e => println; e }
        .asSourceWithContext { elem =>
          if (elem == "Hello") StringContext(s"English Greeting")
          else StringContext("Non-English Greeting")
        }
        .map(_.toUpperCase)
        .toMat(Sink.seq)(Keep.right)

      val result = graph.run()

      Await.result(result, 1 second) shouldBe Vector(
        ("HELLO", StringContext("English Greeting")),
        ("BONJOUR", StringContext("Non-English Greeting"))
      )
    }

    "combine sources and maintain individual order using CONCAT" in {

      // Source.combine is most useful for more that 2 sources, or custom merging
      // When merging 2 sources use (instance method) source.concat or source.merge

      val sourceCombiner = Source
        .combine(oneTwoThreeSource, fourFiveSixSource, sevenEightNineSource)(Concat(_))
        .toMat(Sink.seq)(Keep.right)

      val result = sourceCombiner.run()

      // Concat completes one source before starting the next
      Await.result(result, 1 second) shouldBe List(1,2,3,4,5,6,7,8,9)
    }

    "combine sources and NOT maintain individual order using MERGE" in {

      // Source.combine is most useful for more that 2 sources, or custom merging
      // When merging 2 sources use source.combine or source.merge

      val sourceCombiner = Source
        .combine(oneTwoThreeSource, fourFiveSixSource, sevenEightNineSource)(Merge(_))
        .toMat(Sink.seq)(Keep.right)

      val futureResult = sourceCombiner.run()

      // Merge does NOT complete one source before starting the next, so mixes elements from each source
      // although elements from each source will maintain order
      val result = Await.result(futureResult, 1 second)
      result should not be List(1,2,3,4,5,6,7,8,9)
      result should contain theSameElementsAs List(1,2,3,4,5,6,7,8,9)
    }

    "use concat instance method for combining 2 sources by completing the first, then starting second" in {

      val result = oneTwoThreeSource.concat(fourFiveSixSource).runWith(Sink.seq)

      Await.result(result, 1 second) shouldBe List(1,2,3,4,5,6)
    }

    "use merge instance method for combining 2 sources by immediately outputting elements from both" in {

      val futureResult = oneTwoThreeSource.merge(fourFiveSixSource).runWith(Sink.seq)

      val result = Await.result(futureResult, 1 second)
      result should not be List(1,2,3,4,5,6)
      result should contain theSameElementsAs  List(1,2,3,4,5,6)
    }

    // TODO Source.completionStage: https://doc.akka.io/docs/akka/current/stream/operators/index.html#source-operators

  }

}
