package com.hashmapinc.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

public interface ServiceApi {

    Source<ByteString, NotUsed> buildSource(SourceConfig conf) throws Exception;
}
