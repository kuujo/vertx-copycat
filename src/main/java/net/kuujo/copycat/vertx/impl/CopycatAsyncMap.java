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
import io.vertx.core.shareddata.AsyncMap;

/**
 * Copycat asynchronous map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CopycatAsyncMap<K, V> implements AsyncMap<K, V> {
  private final net.kuujo.copycat.collections.AsyncMap<K, V> map;

  public CopycatAsyncMap(net.kuujo.copycat.collections.AsyncMap<K, V> map) {
    this.map = map;
  }

  @Override
  public void get(K key, Handler<AsyncResult<V>> resultHandler) {
    map.get(key).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void put(K key, V value, Handler<AsyncResult<Void>> resultHandler) {
    map.put(key, value).whenComplete((result, error) -> handleResult(null, error, resultHandler));
  }

  @Override
  public void put(K key, V value, long l, Handler<AsyncResult<Void>> resultHandler) {
    map.put(key, value).whenComplete((result, error) -> handleResult(null, error, resultHandler));
  }

  @Override
  public void putIfAbsent(K key, V value, Handler<AsyncResult<V>> resultHandler) {
    map.putIfAbsent(key, value).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void putIfAbsent(K key, V value, long timeout, Handler<AsyncResult<V>> resultHandler) {
    map.putIfAbsent(key, value).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void remove(K key, Handler<AsyncResult<V>> resultHandler) {
    map.remove(key).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void removeIfPresent(K key, V value, Handler<AsyncResult<Boolean>> resultHandler) {
    map.remove(key).whenComplete((result, error) -> handleResult(result != null, error, resultHandler));
  }

  @Override
  public void replace(K key, V value, Handler<AsyncResult<V>> resultHandler) {
    map.replace(key, value).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void replaceIfPresent(K key, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
    map.replace(key, oldValue, newValue).whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    map.clear().whenComplete((result, error) -> handleResult(result, error, resultHandler));
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    map.size().whenComplete((result, error) -> handleResult(result, error, resultHandler));
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
