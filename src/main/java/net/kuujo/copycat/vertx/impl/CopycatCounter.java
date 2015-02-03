/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.copycat.vertx.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.Counter;
import net.kuujo.copycat.atomic.AsyncAtomicLong;

/**
 * Copycat counter.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CopycatCounter implements Counter {
  private final AsyncAtomicLong counter;

  public CopycatCounter(AsyncAtomicLong counter) {
    this.counter = counter;
  }

  @Override
  public void get(Handler<AsyncResult<Long>> resultHandler) {
    counter.get().whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void incrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
    counter.incrementAndGet().whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void getAndIncrement(Handler<AsyncResult<Long>> resultHandler) {
    counter.getAndIncrement().whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void decrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
    counter.decrementAndGet().whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler) {
    counter.addAndGet(value).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler) {
    counter.getAndAdd(value).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void compareAndSet(long expect, long update, Handler<AsyncResult<Boolean>> resultHandler) {
    counter.compareAndSet(expect, update).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  /**
   * Handles a CompletableFuture result.
   */
  private <T> void handleResult(T result, Throwable error, Handler<AsyncResult<T>> resultHandler) {
    if (error == null) {
      Future.succeededFuture(result).setHandler(resultHandler);
    } else {
      Future.<T>failedFuture(error).setHandler(resultHandler);
    }
  }

}
