package akka.streams

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import akka.actor.ActorSystem
import akka.stream.{BufferOverflowException, OverflowStrategy}
import akka.stream.scaladsl.{Concat, Keep, Merge, RunnableGraph, Sink, Source}
import cats.implicits.catsSyntaxOptionId
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.{Await, Future, Promise, TimeoutException}
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

    "use Source.lazyFuture to defer creation of single element source until there is demand" in {
      val testStartTime = ZonedDateTime.now()

      val lazySourceFuture = Source.lazyFuture(() => Future.successful(ZonedDateTime.now()))

      TimeUnit.SECONDS.sleep(2)

      val futureResult = lazySourceFuture.runWith(Sink.head)

      val lazyFutureTime = Await.result(futureResult, 1 second)

      ChronoUnit.SECONDS.between(testStartTime, lazyFutureTime) should be >= 2L
    }

    "use Source.lazyFutureSource to defer creation & materialization of a Source until there is demand" in {
      val testStartTime = ZonedDateTime.now()

      val lazyFutureSource = Source.lazyFutureSource(() => Future.successful(Source(Seq(ZonedDateTime.now()))))

      TimeUnit.SECONDS.sleep(2)

      val futureResult = lazyFutureSource.runWith(Sink.head)

      val lazyFutureSourceHeadTime = Await.result(futureResult, 1 second)

      ChronoUnit.SECONDS.between(testStartTime, lazyFutureSourceHeadTime) should be >= 2L
    }

    "use Source.lazySingle to defer creation of a single element Source until there is demand" in {
      val testStartTime = ZonedDateTime.now()

      val lazySingleSource = Source.lazySingle(() => ZonedDateTime.now())

      TimeUnit.SECONDS.sleep(2)

      val futureResult = lazySingleSource.runWith(Sink.head)

      val lazySingleSourceTime = Await.result(futureResult, 1 second)

      ChronoUnit.SECONDS.between(testStartTime, lazySingleSourceTime) should be >= 2L
    }

    "use Source.lazySource to defer creation & materialization of a source until there is demand" in {
      val testStartTime = ZonedDateTime.now()

      val lazySource = Source.lazySource(() => Source(Seq(ZonedDateTime.now())))

      TimeUnit.SECONDS.sleep(2)

      val futureResult = lazySource.runWith(Sink.head)

      val lazySourceHeadTime = Await.result(futureResult, 1 second)

      ChronoUnit.SECONDS.between(testStartTime, lazySourceHeadTime) should be >= 2L
    }

    "use Source.maybe to inject a value into the stream after creation by completing the materialized Promise" in {
      val testStartTime = ZonedDateTime.now()

      TimeUnit.SECONDS.sleep(2)

      val sourceMaybeGraph: RunnableGraph[(Promise[Option[ZonedDateTime]], Future[ZonedDateTime])] = Source.maybe[ZonedDateTime].toMat(Sink.head)(Keep.both)

      val (promise, sink) = sourceMaybeGraph.run()

      // Now we complete the promise
      // The result is calculated at this point & passed to the sink
      promise.success(ZonedDateTime.now().some)

      val sourceMaybeResult = Await.result(sink, 1 second)

      ChronoUnit.SECONDS.between(testStartTime, sourceMaybeResult) should be >= 2L
    }

    "use Source.maybe to materialise a new promise each time run is called, to inject a value into the stream after creation" in {
      val sourceMaybeGraph = Source.maybe[Int].toMat(Sink.head)(Keep.both)

      // New promise materialised for each run call, and can be completed
      // this contrasts with Source.fromFuture - the future passed to this can only be completed once
      val (promiseOne, sinkOne) = sourceMaybeGraph.run()
      val (promiseTwo, sinkTwo) = sourceMaybeGraph.run()

      promiseOne.success(7.some)
      promiseTwo.success(11.some)

      Await.result(sinkOne, 1 second) shouldBe 7
      Await.result(sinkTwo, 1 second) shouldBe 11
    }

    "use Source.never for a source that never completes or emits any elements, useful for testing" in {
      val sourceNeverGraph = Source.never[Int] //.initialTimeout(1 second)

      val futureResult: Future[Int] = sourceNeverGraph.runWith(Sink.head)

      // Odd, but illustrates that source never emits or completes so Await.result times out...
      intercept[TimeoutException] { Await.result(futureResult, 2 seconds) } shouldBe a[TimeoutException]
    }

    "use Source.queue to materialize a SourceQueue that can be used to push elements to be emitted from the Source" in {
      val sourceQueueGraph = Source.queue[Int](1, OverflowStrategy.backpressure).toMat(Sink.seq)(Keep.both)

      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)
      materializedQueue.complete()

      Await.result(sink, 1 second) shouldBe Seq(1,2)
    }

    "use Source.queue with throttle to rate limit elements sent downstream in a given window" in {
      val bufferSize = 1
      val elementsToProcess = 1
      val threeSecondWindow = 3 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.backpressure)
        .throttle(elementsToProcess, threeSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      // TODO test function to wrap functionality and calculate time taken to complete...
      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(8)
      materializedQueue.offer(9)
      materializedQueue.complete()

      Await.result(sink, 4 seconds) shouldBe Seq(8,9)

      val sinkReceivingCompleteTime = ZonedDateTime.now()

      // 1 element is sent downstream every 3 seconds, so sink will take at least 3 seconds to receive 2 elements
      ChronoUnit.SECONDS.between(streamStartTime, sinkReceivingCompleteTime) should be >= 3L
    }

    "use Source.queue with throttle and OverflowStrategy.backpressure to pause publisher until buffer has space" in {
      val bufferSize = 1
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.backpressure)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Buffer now full, back-pressure applied until space opens in buffer
      materializedQueue.offer(6)
      materializedQueue.complete()

      // Should get all elements as none are rejected, just delayed until buffer has space again
      Await.result(sink, 5 seconds) shouldBe Seq(4,5,6)

      // Backpressure keeps elements out of buffer until it has space, elements are consumed 1 per 2 seconds
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= 4L
    }

    "use Source.queue with throttle and OverflowStrategy.dropBuffer to drop older elements in buffer when full to process newer elements" in {
      val bufferSize = 3
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.dropBuffer)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)

      // Next 3 elements buffered
      materializedQueue.offer(3)
      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Next element offered and whole buffer dropped to process this one
      materializedQueue.offer(6)
      materializedQueue.complete()

      // Should get elements processed before buffer became full, and the one offered when the buffer was full
      Await.result(sink, 5 seconds) shouldBe Seq(1,2,6)

      // 1 element processed every 2 seconds (0, 2, 4 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= 4L
    }

    "use Source.queue with throttle and OverflowStrategy.dropHead to drop oldest element in buffer when full to process newer offered element" in {
      val bufferSize = 3
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.dropHead)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)

      // Next 3 elements buffered at time of drop
      materializedQueue.offer(3)
      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Next element offered and first (oldest) into buffer dropped to process this one
      materializedQueue.offer(6)
      materializedQueue.complete()

      // Should only drop oldest element in FULL buffer (3) when another element is offered
      Await.result(sink, 10 seconds) shouldBe Seq(1,2,4,5,6)

      // 1 element processed every 2 seconds (0, 2, 4, 6, 8 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= 8L
    }

    "use Source.queue with throttle and OverflowStrategy.dropTail to drop newest element in buffer when full to process newer offered element" in {
      val bufferSize = 3
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.dropTail)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)

      // Next 3 elements buffered at time of drop
      materializedQueue.offer(3)
      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Next element offered and last (newest) into buffer dropped to process this one
      materializedQueue.offer(6)
      materializedQueue.complete()

      // Should only drop oldest element in FULL buffer (3) when another element is offered
      Await.result(sink, 10 seconds) shouldBe Seq(1,2,3,4,6)

      // 1 element processed every 2 seconds (0, 2, 4, 6, 8 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= 8L
    }

    "use Source.queue with throttle and OverflowStrategy.dropNew to drop offered element when buffer us full" in {
      val bufferSize = 3
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.dropNew)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val streamStartTime = ZonedDateTime.now()
      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)

      // Next 3 elements buffered at time of drop
      materializedQueue.offer(3)
      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Next element offered and dropped as buffer is full
      materializedQueue.offer(6)
      materializedQueue.complete()

      // Should only drop element offered when buffer is full (6)
      Await.result(sink, 10 seconds) shouldBe Seq(1,2,3,4,5)

      // 1 element processed every 2 seconds (0, 2, 4, 6, 8 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= 8L
    }

    "use Source.queue with throttle and OverflowStrategy.fail to fail stream when new element is offered and buffer is full" in {
      val bufferSize = 3
      val elementsToProcess = 1
      val twoSecondWindow = 2 seconds

      val sourceQueueGraph = Source
        .queue[Int](bufferSize, OverflowStrategy.fail)
        .throttle(elementsToProcess, twoSecondWindow)
        .toMat(Sink.seq)(Keep.both)

      val (materializedQueue, sink) = sourceQueueGraph.run()

      materializedQueue.offer(1)
      materializedQueue.offer(2)

      // Next 3 elements buffered at time of drop
      materializedQueue.offer(3)
      materializedQueue.offer(4)
      materializedQueue.offer(5)

      // Next element offered and causes failure
      materializedQueue.offer(6)
      materializedQueue.complete()

      val failure = sink.failed.futureValue
      failure shouldBe a[BufferOverflowException]
      failure.getMessage shouldBe "Buffer overflow (max capacity was: 3)!"
    }

    "use Source.range (via apply method) to create a source emitting elements in the range (inclusive)" in {
      val futureResult = Source(1 to 5).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(1,2,3,4,5)
    }

    "use Source.single to emit a single element" in {
      val futureResult = Source.single(0).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(0)
    }

    "use Source.tick to periodically emit elements forever" in {
      val delayBetweenElements = 1 second

      val graph = Source.tick(0 seconds, delayBetweenElements, "tick").take(3).toMat(Sink.seq)(Keep.right)

      val streamStartTime = ZonedDateTime.now()
      val futureSink = graph.run()

      Await.result(futureSink, 3 seconds) shouldBe Seq("tick","tick","tick")

      // 3 elements with 1 second delay between each takes at least 2 seconds (emit at 0, 1, 2 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= (delayBetweenElements.toSeconds * 2)
    }

    "use Source.tick to periodically emit elements forever, with an initial delay" in {
      val initialDelay = 2 seconds
      val delayBetweenElements = 1 second

      val graph = Source.tick(initialDelay, delayBetweenElements, "tick").take(1).toMat(Sink.seq)(Keep.right)

      val streamStartTime = ZonedDateTime.now()
      val futureSink = graph.run()

      Await.result(futureSink, 3 seconds) shouldBe Seq("tick")

      // 1 element with 2 second delay before first element takes at least 2 seconds (emit at 2 seconds)
      ChronoUnit.SECONDS.between(streamStartTime, ZonedDateTime.now()) should be >= (initialDelay.toSeconds)
    }

    "use Source.tick to periodically emit elements forever, materialize a Cancellable, and use to cancel stream" in {
      val delayBetweenElements = 1 second

      val graph = Source.tick(0 seconds, delayBetweenElements, "tick").toMat(Sink.seq)(Keep.both)

      val (materializedCancellable, futureSink) = graph.run()

      // Emit 3 elements at 0, 1, 2 seconds (fourth element isn't quite reached before stream cancelled)
      TimeUnit.SECONDS.sleep(3)
      materializedCancellable.cancel()

      // Stream is cancelled, we do not get infinite elements
      Await.result(futureSink, 4 seconds) shouldBe Seq("tick", "tick", "tick")
    }

    "use Source.unfold to emit elements as long as the result is a Some (self-terminating)" in {
      val futureResult = Source.unfold(3) { number =>
        if (number == 0) None
        else (number - 1, number).some // Emitted tuple is (next iteration element, element to emit)
      }.runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(3,2,1)
    }

    "use Source.unfold to emit elements (non-terminating so combine with take(n))" in {
      val futureResult = Source.unfold(0, 1) {
        case (a, b) => ((b, a + b), a).some
      }.take(8).runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(0,1,1,2,3,5,8,13)
    }

    "use Source.unfoldAsync to emit a future that causes the source to emit or complete when it completes" in {

      val countdownSource = Source.unfoldAsync(10) { number =>
        if (number == 0) Future.successful(None) // source completes on this condition
        else Future.successful((number -1, number).some)
      }

      val futureResult = countdownSource.runWith(Sink.seq)

      Await.result(futureResult, 1 second) shouldBe Seq(10,9,8,7,6,5,4,3,2,1)
    }

    "use Source.unfoldResource to safely extract stream elements from blocking resources, by providing 3 functions" in {

      /** Example blocking resource traits and Impl */
      trait Database { def doBlockingQuery(): QueryResult }
      trait QueryResult {
        def hasMore: Boolean
        // potentially blocking retrieval of each element
        def nextEntry(): DatabaseEntry
        def close(): Unit
      }
      trait DatabaseEntry { val data: String }

      val databaseEntry = new DatabaseEntry { override val data = "Any Result" }
      val db = new Database {
        override def doBlockingQuery(): QueryResult = new QueryResult {
          def hasMore: Boolean = true
          def nextEntry(): DatabaseEntry = databaseEntry
          def close(): Unit = ()
        }
      }

      val queryResultSource = Source.unfoldResource[DatabaseEntry, QueryResult](
        // Create
        { () => db.doBlockingQuery() },
        // Read
        { query => if (query.hasMore) query.nextEntry().some else None},
        // Close
        { query => query.close() }
      )

      val futureResult = queryResultSource.runWith(Sink.head)

      Await.result(futureResult, 1 second) shouldBe databaseEntry
    }

    // TODO NEXT Source.unfoldResourceAsync: https://doc.akka.io/docs/akka/current/stream/operators/Source/unfoldResourceAsync.html
  }
}
