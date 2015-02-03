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

import io.vertx.core.spi.cluster.ChoosableIterable;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Choosable set implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ChoosableCollection<T> implements ChoosableIterable<T> {
  private final Collection<T> collection;
  private Iterator<T> iterator;

  public ChoosableCollection(Collection<T> collection) {
    this.collection = collection;
  }

  @Override
  public boolean isEmpty() {
    return collection.isEmpty();
  }

  @Override
  public T choose() {
    if (!collection.isEmpty()) {
      if (iterator == null || !iterator.hasNext()) {
        iterator = collection.iterator();
      }
      try {
        return iterator.next();
      } catch (NoSuchElementException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public Iterator<T> iterator() {
    return collection.iterator();
  }

}
