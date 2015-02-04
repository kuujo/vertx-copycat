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
package net.kuujo.copycat.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.core.spi.cluster.VertxSPI;
import net.kuujo.copycat.Copycat;
import net.kuujo.copycat.cluster.ClusterConfig;
import net.kuujo.copycat.cluster.Member;
import net.kuujo.copycat.cluster.MembershipEvent;
import net.kuujo.copycat.vertx.impl.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Copycat Vert.x cluster manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class CopycatClusterManager implements ClusterManager {
  private final Copycat copycat;
  private NodeListener listener;

  public CopycatClusterManager(ClusterConfig cluster) {
    Objects.requireNonNull(cluster);
    this.copycat = Copycat.create(cluster);
  }

  @Override
  public void setVertx(VertxSPI vertx) {
    // We don't need to use the blocking API since Copycat is asynchronous.
  }

  @Override
  public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> resultHandler) {
    copycat.<K, V>multiMap(name).open().whenComplete((result, error) -> {
      if (error == null) {
        Future.<AsyncMultiMap<K, V>>succeededFuture(new CopycatAsyncMultiMap<>(result)).setHandler(resultHandler);
      } else {
        Future.<AsyncMultiMap<K, V>>failedFuture(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> resultHandler) {
    copycat.<K, V>map(name).open().whenComplete((result, error) -> {
      if (error == null) {
        Future.<AsyncMap<K, V>>succeededFuture(new CopycatAsyncMap<K, V>(result)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public <K, V> Map<K, V> getSyncMap(String name) {
    try {
      return new CopycatMap<>(copycat.<K, V>map(name).open().get());
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> resultHandler) {
    copycat.lock(name).open().whenComplete((lock, error) -> {
      if (error == null) {
        lock.lock().whenComplete((lockResult, lockError) -> {
          if (lockError == null) {
            Future.<Lock>succeededFuture(new CopycatLock(lock)).setHandler(resultHandler);
          } else {
            Future.<Lock>failedFuture(lockError).setHandler(resultHandler);
          }
        });
      } else {
        Future.<Lock>failedFuture(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void getCounter(String name, Handler<AsyncResult<Counter>> resultHandler) {
    copycat.atomicLong(name).open().whenComplete((counter, error) -> {
      if (error == null) {
        Future.<Counter>succeededFuture(new CopycatCounter(counter)).setHandler(resultHandler);
      } else {
        Future.<Counter>failedFuture(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public String getNodeID() {
    return copycat.cluster().member().uri();
  }

  @Override
  public List<String> getNodes() {
    return copycat.cluster().members().stream().map(Member::uri).collect(Collectors.toList());
  }

  @Override
  public void nodeListener(NodeListener listener) {
    this.listener = listener;
  }

  /**
   * Handles a membership event.
   */
  private void handleMembershipEvent(MembershipEvent event) {
    if (event.type() == MembershipEvent.Type.JOIN) {
      listener.nodeAdded(event.member().uri());
    } else if (event.type() == MembershipEvent.Type.LEAVE) {
      listener.nodeLeft(event.member().uri());
    }
  }

  @Override
  public void join(Handler<AsyncResult<Void>> resultHandler) {
    copycat.open().whenComplete((result, error) -> {
      if (error == null) {
        copycat.cluster().addMembershipListener(this::handleMembershipEvent);
        Future.<Void>succeededFuture().setHandler(resultHandler);
      } else {
        Future.<Void>failedFuture(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void leave(Handler<AsyncResult<Void>> resultHandler) {
    copycat.cluster().removeMembershipListener(this::handleMembershipEvent);
    copycat.close().whenComplete((result, error) -> {
      if (error == null) {
        Future.<Void>succeededFuture().setHandler(resultHandler);
      } else {
        Future.<Void>failedFuture(error).setHandler(resultHandler);
      }
    });
  }

  @Override
  public boolean isActive() {
    return copycat.isOpen();
  }

}
