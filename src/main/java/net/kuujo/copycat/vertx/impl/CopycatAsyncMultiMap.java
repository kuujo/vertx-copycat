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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ChoosableIterable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copycat asynchronous multimap.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CopycatAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
  private final net.kuujo.copycat.collections.AsyncMultiMap<K, V> map;

  public CopycatAsyncMultiMap(net.kuujo.copycat.collections.AsyncMultiMap<K, V> map) {
    this.map = map;
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> resultHandler) {
    map.put(k, v).whenComplete((result, error) -> {
      if (error == null) {
        new DefaultFutureResult<Void>((Void) null).setHandler(resultHandler);
      } else {
        new DefaultFutureResult<Void>(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> resultHandler) {
    map.get(k).whenComplete((result, error) -> {
      if (error == null) {
        new DefaultFutureResult<ChoosableIterable<V>>(new ChoosableCollection<>(result)).setHandler(resultHandler);
      } else {
        new DefaultFutureResult<ChoosableIterable<V>>(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Void>> resultHandler) {
    map.remove(k, v).whenComplete((result, error) -> {
      if (error == null) {
        new DefaultFutureResult<Void>((Void) null).setHandler(resultHandler);
      } else {
        new DefaultFutureResult<Void>(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> resultHandler) {
    map.entrySet().whenComplete((result, error) -> {
      if (error == null) {
        AtomicInteger counter = new AtomicInteger();
        for (Map.Entry<K, V> entry : result) {
          map.remove(entry.getKey(), entry.getValue()).whenComplete((removeResult, removeError) -> {
            if (removeError == null) {
              if (counter.incrementAndGet() == result.size()) {
                new DefaultFutureResult<Void>((Void) null).setHandler(resultHandler);
              }
            } else {
              new DefaultFutureResult<Void>(removeError).setHandler(resultHandler);
            }
          });
        }
      } else {
        new DefaultFutureResult<Void>(error).setHandler(resultHandler);
      }
    });
  }

}
