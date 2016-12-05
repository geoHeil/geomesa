package org.locationtech.geomesa.accumulo.data

import org.geotools.data.DataStoreFinder
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._

/**
  * Created by James Srinivasan on 04/12/2016.
  */
class AccumuloDataStoreKerberosTest extends Specification {


  "AccumuloDataStore" should {

    sequential

    "create a password authenticated store" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "password" -> "secret",
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must not(beNull)
    }

    "create a token authenticated store" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "useToken" -> true,
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must not(beNull)
    }

    "create a keytab authenticated store" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "keytabPath" -> "/path/to/keytab",
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must not(beNull)
    }

    "not accept password and token" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "password" -> "password",
        "useToken" -> true,
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must (beNull)
    }

    "not accept password and keytab" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "password" -> "password",
        "keytabPath" -> "/mypath/keytab",
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must (beNull)
    }

    "not accept token and keytab" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "useToken" -> true,
        "keytabPath" -> "/mypath/keytab",
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must (beNull)
    }

    "not accept password and token and keytab" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "me",
        "password" -> "secret",
        "useToken" -> true,
        "keytabPath" -> "/mypath/keytab",
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must (beNull)
    }
  }

}
