/*
 * Copyright (C) 2017 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.discovery.config

import akka.actor.ActorSystem
import akka.discovery.ServiceDiscovery
import akka.discovery.SimpleServiceDiscovery.ResolvedTarget
import akka.testkit.TestKit
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.concurrent.duration._
import scala.collection.immutable

object ConfigSimpleServiceDiscoverySpec {

  val config: Config = ConfigFactory.parseString("""
akka {
  loglevel = DEBUG
  discovery {
    method = config
    config {
      services = {
        service1 = {
          endpoints = [
            {
              host = "cat"
              port = 1233
            },
            {
              host = "dog"
            }
          ]
        },
        service2 = {
          endpoints = []
        }
      }
    }
  }
}
    """)

}

class ConfigSimpleServiceDiscoverySpec
    extends TestKit(ActorSystem("ConfigSimpleDiscoverySpec", ConfigSimpleServiceDiscoverySpec.config))
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val discovery = ServiceDiscovery(system).discovery

  "Config discovery" must {
    "load from config" in {
      val result = discovery.lookup("service1", 100.millis).futureValue
      result.serviceName shouldEqual "service1"
      result.addresses shouldEqual immutable.Seq(
        ResolvedTarget("cat", Some(1233)),
        ResolvedTarget("dog", None)
      )
    }

    "return no resolved targets if not in config" in {
      val result = discovery.lookup("dontexist", 100.millis).futureValue
      result.serviceName shouldEqual "dontexist"
      result.addresses shouldEqual immutable.Seq.empty
    }
  }
}
