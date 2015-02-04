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

import net.kuujo.copycat.Copycat;
import net.kuujo.copycat.cluster.Member;
import net.kuujo.copycat.cluster.MembershipEvent;
import net.kuujo.copycat.vertx.impl.CopycatAsyncMap;
import net.kuujo.copycat.vertx.impl.CopycatAsyncMultiMap;
import net.kuujo.copycat.vertx.impl.CopycatMap;
import org.vertx.java.core.spi.VertxSPI;
import org.vertx.java.core.spi.cluster.AsyncMap;
import org.vertx.java.core.spi.cluster.AsyncMultiMap;
import org.vertx.java.core.spi.cluster.ClusterManager;
import org.vertx.java.core.spi.cluster.NodeListener;

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

  public CopycatClusterManager(VertxSPI vertx) {
    Objects.requireNonNull(vertx);
    this.copycat = Copycat.create();
  }

  @Override
  public <K, V> AsyncMultiMap<K, V> getAsyncMultiMap(String name) {
    try {
      return new CopycatAsyncMultiMap<>(copycat.<K, V>multiMap(name).open().get());
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <K, V> AsyncMap<K, V> getAsyncMap(String name) {
    try {
      return new CopycatAsyncMap<>(copycat.<K, V>map(name).open().get());
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
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
  public synchronized void join() {
    try {
      copycat.open().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    copycat.cluster().addMembershipListener(this::handleMembershipEvent);
  }

  @Override
  public void leave() {
    copycat.cluster().removeMembershipListener(this::handleMembershipEvent);
    try {
      copycat.close().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

}
