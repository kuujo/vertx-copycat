## Vert.x 3 Copycat Cluster Manager

This project provides a [Copycat](http://github.com/kuujo/copycat) based `ClusterManager` implementation for
[Vert.x 3](http://github.com/eclipse/vert.x).

Copycat is a cluster coordination framework that provides a variety of strongly-consistent log-based data structures.
The strong consistency of Copycat's data structures is an excellent fit for cluster management for Vert.x in certain
scenarios. Whereas the default [Hazelcast](http://hazelcast.org/) cluster manager provides significant availability
guarantees at the expense of consistency, Copycat guarantees that data will not be lost during a catastrophic network
failure. This means any node's view of Vert.x distributed maps, locks, counters, and other data structures are guaranteed not to
diverge from one another even in the face of a network partition.

Of course, there are trade-offs in all distributed systems. In contrast to Hazelcast, the Copycat cluster manager
requires that at least three to five nodes - known as *active* members - be permanent members of the Vert.x cluster.
However, much like Hazelcast, Copycat supports arbitrary configurations of *passive* members beyond the core *active*
cluster and allows passive members to join and leave the cluster at will.

### Usage

To use the Copycat cluster manager simply configure the `VertxOptions` specifying a `CopycatClusterManager` instance as
the Vert.x cluster manager. The `CopycatClusterManager` takes the URI of the local node and Copycat `ClusterConfig`
identifying the core active members of the cluster.

```java
ClusterConfig cluster = new ClusterConfig()
  .withProtocol(new VertxTcpProtocol())
  .addMember("tcp://123.456.789.0:1234")
  .addMember("tcp://123.456.789.1:1234")
  .addMember("tcp://123.456.789.2:1234");

VertxOptions options = new VertxOptions()
  .setClustered(true)
  .setClusterManager(new CopycatClusterManager("tcp://123.456.789.20:1234", cluster));

Vertx.clusteredVertx(options, result -> {
  if (result.succeeded()) {
    Vertx vertx = result.result();
  }
});
```
