package akka.streams

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl.{Keep, RunnableGraph, Sink, Source}
import akka.stream.{KillSwitches, UniqueKillSwitch}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class AkkaStreamsSpec extends AsyncWordSpec with Matchers {

  implicit val system = ActorSystem("akka-streams-playground")

  "Akka Streams" should {
    "materialise a graphs result" in {

      val graph: RunnableGraph[Future[String]] = Source
        .single("Hello World!")
        .map(_.toUpperCase)
        .toMat(Sink.head)(Keep.right)

      val result = graph.run()

      Await.result(result, 1 second) shouldBe "HELLO WORLD!"
    }

    "stop a ticking source via a materialized cancellable" in {

      val graph: RunnableGraph[(Cancellable, Future[Seq[String]])] = Source
        .tick(0 second, 1 second, "Hello")
        .toMat(Sink.seq)(Keep.both) // keep both - what we get when run() is called (left is from previous step (tick materialises a Cancellable), right is this toMat i.e. Sink.seq)

      val (cancellable, futureResult): (Cancellable, Future[Seq[String]]) = graph.run()

      futureResult.onComplete {
        case Success(_) => // Future should complete successfully even when cancelled!
        case Failure(e) => fail("Cancelling a ticking source is successful completion. ")
      }

      TimeUnit.SECONDS.sleep(4)

      val isCancelled = cancellable.cancel()

      isCancelled shouldBe true
      Await.result(futureResult, 1 second) shouldBe Seq("Hello", "Hello", "Hello", "Hello")
    }

    "stop a ticking source with a pre-materialized Cancellable value" in {

      val (canx, source): (Cancellable, Source[String, NotUsed]) = Source
        .tick(0 second, 1 second, "Hello")
        .preMaterialize()

      val futureResult = source.runWith(Sink.seq)

      futureResult.onComplete {
        case Success(_) => // Future should complete successfully even when cancelled!
        case Failure(e) => fail("Cancelling a ticking source is successful completion.")
      }

      TimeUnit.SECONDS.sleep(4)

      val isCancelled = canx.cancel()

      isCancelled shouldBe true
      Await.result(futureResult, 1 second) shouldBe Vector("Hello", "Hello", "Hello")
    }

  }

  "KillSwitch" should {
    "stop an infinite Source when shutdown is called" in {
      val infiniteHelloSource = Source.tick(0 seconds, 1 second, "Hello")

      val (killswitch, elements): (UniqueKillSwitch, Future[Seq[String]]) = infiniteHelloSource
        .viaMat(KillSwitches.single)(Keep.right)
        .toMat(Sink.seq)(Keep.both)
        .run()

      TimeUnit.SECONDS.sleep(2)

      killswitch.shutdown()

      Await.result(elements, 1 second).size shouldBe 2
    }

    "stop a finite Source by cancelling its upstream and completing its downstream when shutdown is called" in {
      val infiniteHelloSource = Source.tick(0 seconds, 1 second, "Hello")

      val (killswitch, elements): (UniqueKillSwitch, Future[Seq[String]]) = infiniteHelloSource
        // Delay downstream "processing" (Processing is sequential so waits for 1st element
        // THEN STARTS processing second element ...
        .map { elem => TimeUnit.SECONDS.sleep(2); elem }
        .viaMat(KillSwitches.single)(Keep.right)
        .toMat(Sink.seq)(Keep.both)
        .run()

      // Allow 2 upstream elements to be read from source
      TimeUnit.SECONDS.sleep(2)

      killswitch.shutdown()

      // 2 upstream elements should complete, but no more should be read after shutdown
      Await.result(elements, 3 seconds) shouldBe Vector("Hello", "Hello")
    }

  }

}
