# akka-streams-example

## Service API
Unified API, will return Source which consumes ByteString. Developer can add Flow and/or Sink to it as a Subscriber. It accepts SourceConfig which is a placeholder for configurations needed for underlying source actors.

## Message Source
It's a GraphStage which acts as a Source. It receives messages from Actor e.g. Tcp Server actor or File Actor and then StageActor will publish those messages.

## TCP
It has TCP server actor which can accept connection from other TCP clients and receive data. Handler Actor will receive data from client which will post it to Stage Actor added as channel from Message source.
Ideally these raw ByteString from client needs to be converted to serialized Objects for which in TCP service we can attach a Flow<ByteString, MeaningfulObject>. Again to serialize these objects we can add a Flow<MeaningfulObject, ByteString> to convert Object to JSON to ByteString.

## File
File processor actor can get a file path from SourceConfig and then read a file using FileIO source which will publish all read ByteString to stage actor added as Sink. If it's a CSV then in service we can add Flow e.g. FramingDelimeter to convert it to meaningful objects and then again to ByteString.


Developers need to instantiate ServiceApi depending upon need and use it in their application where they can attach their own sink e.g. Database, Actor, Kafka etc. to get processed data.
There can be multiple sinks attached to same source.
