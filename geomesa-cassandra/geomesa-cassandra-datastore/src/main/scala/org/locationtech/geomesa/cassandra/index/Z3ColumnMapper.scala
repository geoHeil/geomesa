/***********************************************************************
 * Copyright (c) 2017-2019 IBM
 * Copyright (c) 2013-2019 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package org.locationtech.geomesa.cassandra.index

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.github.benmanes.caffeine.cache.{CacheLoader, Caffeine}
import org.locationtech.geomesa.cassandra.{ColumnSelect, NamedColumn, RowSelect}
import org.locationtech.geomesa.index.api._
import org.locationtech.geomesa.index.index.z3.Z3IndexKey

object Z3ColumnMapper {

  private val cache = Caffeine.newBuilder().build(
    new CacheLoader[Integer, Z3ColumnMapper]() {
      override def load(shards: Integer): Z3ColumnMapper = {
        new Z3ColumnMapper(Seq.tabulate(shards)(i => ColumnSelect(CassandraColumnMapper.ShardColumn, i, i)))
      }
    }
  )

  def apply(shards: Int): Z3ColumnMapper = cache.get(shards)
}

class Z3ColumnMapper(shards: Seq[ColumnSelect]) extends CassandraColumnMapper {

  private val Shard     = CassandraColumnMapper.ShardColumn
  private val Period    = CassandraColumnMapper.binColumn(1)
  private val ZValue    = CassandraColumnMapper.zColumn(2)
  private val FeatureId = CassandraColumnMapper.featureIdColumn(3)
  private val Feature   = CassandraColumnMapper.featureColumn(4)

  override val columns: Seq[NamedColumn] = Seq(Shard, Period, ZValue, FeatureId, Feature)

  override def bind(value: SingleRowKeyValue[_]): Seq[AnyRef] = {
    val shard = Byte.box(if (value.shard.isEmpty) { 0 } else { value.shard.head })
    val Z3IndexKey(bin, z) = value.key
    val fid = new String(value.id, StandardCharsets.UTF_8)
    val Seq(feature) = value.values.map(v => ByteBuffer.wrap(v.value))
    Seq(shard, Short.box(bin), Long.box(z), fid, feature)
  }

  override def bindDelete(value: SingleRowKeyValue[_]): Seq[AnyRef] = {
    val shard = Byte.box(if (value.shard.isEmpty) { 0 } else { value.shard.head })
    val Z3IndexKey(bin, z) = value.key
    val fid = new String(value.id, StandardCharsets.UTF_8)
    Seq(shard, Short.box(bin), Long.box(z), fid)
  }

  override def select(range: ScanRange[_], tieredKeyRanges: Seq[ByteRange]): Seq[RowSelect] = {
    val clause = range.asInstanceOf[ScanRange[Z3IndexKey]] match {
      case BoundedRange(lo, hi)  => Seq(ColumnSelect(Period, lo.bin, hi.bin), ColumnSelect(ZValue, lo.z, hi.z))
      case UnboundedRange(_)     => Seq.empty
      case SingleRowRange(row)   => Seq(ColumnSelect(Period, row.bin, row.bin), ColumnSelect(ZValue, row.z, row.z))
      case LowerBoundedRange(lo) => Seq(ColumnSelect(Period, lo.bin, null), ColumnSelect(ZValue, lo.z, null))
      case UpperBoundedRange(hi) => Seq(ColumnSelect(Period, null, hi.bin), ColumnSelect(ZValue, null, hi.z))
      case PrefixRange(_)        => Seq.empty // not supported
      case _ => throw new IllegalArgumentException(s"Unexpected range type $range")
    }
    if (clause.isEmpty) { Seq(RowSelect(clause)) } else {
      shards.map(s => RowSelect(clause.+:(s)))
    }
  }
}