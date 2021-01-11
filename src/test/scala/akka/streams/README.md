Generic akka-streams learning.

Currently going through: https://doc.akka.io/docs/akka/current/stream/operators/index.html#source-operators

This is from the main page: https://doc.akka.io/docs/akka/current/stream/index.html


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
- `lazyCompletionStage` (Doesn't exist in source code!)
- `lazyCompletionStageSource` (Doesn't exist in source code!)

TODO Next: `Source.unfoldResourceAsync`


