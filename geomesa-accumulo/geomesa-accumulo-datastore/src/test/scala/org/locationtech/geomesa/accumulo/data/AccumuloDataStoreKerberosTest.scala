/** *********************************************************************
  * Crown Copyright (c) 2016 Dstl
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Apache License, Version 2.0
  * which accompanies this distribution and is available at
  * http://www.opensource.org/licenses/apache2.0.php.
  * ************************************************************************/

package org.locationtech.geomesa.accumulo.data

import org.geotools.data.DataStoreFinder
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._

import org.locationtech.geomesa.accumulo.TestWithKerberos

class AccumuloDataStoreKerberosTest extends Specification with TestWithKerberos {


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
      kdc.getKrbClient.requestTgt("user@EXAMPLE.COM", "password")
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "user@EXAMPLE.COM",
        "useToken" -> true,
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must not(beNull)
    }

    "create a token authenticated store with nulls" in {
      kdc.getKrbClient.requestTgt("user@EXAMPLE.COM", "password")
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "user@EXAMPLE.COM",
        "password" -> null,
        "useToken" -> true,
        "keytabPath" -> null,
        "tableName" -> "tableName").asJava).asInstanceOf[AccumuloDataStore]
      ds must not(beNull)
    }

    "create a keytab authenticated store" in {
      val ds = DataStoreFinder.getDataStore(Map(
        "useMock" -> "true",
        "instanceId" -> "my-instance",
        "user" -> "user@EXAMPLE.COM",
        "keytabPath" -> keytabFilename,
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
