Generic akka-streams learning.

Currently going through: https://doc.akka.io/docs/akka/current/stream/operators/index.html#source-operators


Skipped `Source` Methods:

- `asSubscriber`
- `completionStage` (Java DSL - see `Source.future` for scala version)
- `completionStageSource` (Java DSL - see `Source.futureSource` for scala version)
- `fromFuture` (Deprecated see: `Source.future`)
- `fromFutureSource` (Deprecated see: `Source.futureSource`)
- `fromPublisher` (JavaFlowSupport)
- `fromSourceCompletionStage` (Deprecated see: `Source.completionStageSource`)
- `lazily` (Deprecated see: `Source.lazySource`)
- `lazilyAsync` (Deprecated see: `Source.lazyFutureSource`)

TODO Next: `Source.lazyCompletionStage`


