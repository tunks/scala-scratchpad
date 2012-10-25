package com.earldouglas.hector

import scala.collection.JavaConverters.asScalaIteratorConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import me.prettyprint.cassandra.serializers.LongSerializer
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.service.template.ColumnFamilyResult
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate
import me.prettyprint.cassandra.service.ThriftKsDef
import me.prettyprint.hector.api.beans.OrderedRows
import me.prettyprint.hector.api.beans.Row
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition
import me.prettyprint.hector.api.ddl.ComparatorType
import me.prettyprint.hector.api.ddl.KeyspaceDefinition
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.hector.api.query.QueryResult
import me.prettyprint.hector.api.Cluster
import me.prettyprint.hector.api.Keyspace

object HectorDemo extends App {

  val clusterName = "MyCluster"
  val hostIp = "localhost:9160"
  val keyspaceName = "MyKeyspace"
  val columnFamilyName = "MyColumnFamily"

  val cluster: Cluster = HFactory.getOrCreateCluster(clusterName, hostIp)

  val keyspace: Keyspace = HFactory.createKeyspace(keyspaceName, cluster)
  val template = new ThriftColumnFamilyTemplate[String, String](keyspace, columnFamilyName, StringSerializer.get(), StringSerializer.get())

  if (cluster.describeKeyspace(keyspaceName) == null) createSchema()

  save("foo", "www.earldouglas.com")
  println(load("foo"))

  val rangeSlicesQuery =
    HFactory.createRangeSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(), LongSerializer.get())
      .setColumnFamily(columnFamilyName)
      .setRange("", "", false, 0)

  val result: QueryResult[OrderedRows[java.lang.String, java.lang.String, java.lang.Long]] = rangeSlicesQuery.execute()
  val rows: OrderedRows[java.lang.String, java.lang.String, java.lang.Long] = result.get()
  val rowsIterator: Iterator[Row[java.lang.String, java.lang.String, java.lang.Long]] = rows.iterator.asScala

  while (rowsIterator.hasNext) {
    val row: Row[java.lang.String, java.lang.String, java.lang.Long] = rowsIterator.next()
    println(row)
  }

  def save(key: String, value: String) {
    val updater: ColumnFamilyUpdater[String, String] = template.createUpdater(key)
    updater.setString("domain", "www.earldouglas.com")
    updater.setLong("time", System.currentTimeMillis())
    template.update(updater);
  }

  def load(key: String): String = {
    val res: ColumnFamilyResult[String, String] = template.queryColumns(key)
    res.getString("domain")
  }

  def createSchema() {
    val cfDef: ColumnFamilyDefinition = HFactory.createColumnFamilyDefinition(keyspaceName, columnFamilyName, ComparatorType.BYTESTYPE)
    val replicationFactor = 1
    val newKeyspace: KeyspaceDefinition = HFactory.createKeyspaceDefinition(keyspaceName, ThriftKsDef.DEF_STRATEGY_CLASS, replicationFactor, List(cfDef).asJava)
    cluster.addKeyspace(newKeyspace, true)
  }
}

