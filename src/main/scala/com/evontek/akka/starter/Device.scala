package com.evontek.akka.starter

import akka.actor.{Actor, ActorLogging, Props}

object Device {
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  final case class ReadTemperature(requestId: Long)

  final case class RespondTemperature(requestId: Long, value: Option[Double])

  final case class RecordTemperature(requestId: Long, value: Double)

  final case class TemperatureRecorded(requestId: Long)

}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  import Device.{ReadTemperature, RespondTemperature, RecordTemperature, TemperatureRecorded}
  import DeviceManager.{RequestTrackDevice, DeviceRegistered}

  var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stopped", groupId, deviceId)

  override def receive: Receive = {

    case RequestTrackDevice(requestId, `groupId`, `deviceId`) =>
      sender() ! DeviceRegistered(requestId)

    case RequestTrackDevice(requestId, groupId, deviceId) =>
      log.warning(s"Ignoring TrackDevice request for {}-{} using $requestId. This actor is responsible for {}-{}.", groupId, deviceId, this.groupId, this.deviceId)

    case RecordTemperature(requestId, value) =>
      log.info("Recorded temperature reading {} with {}", value, requestId)
      lastTemperatureReading = Some(value)
      sender() ! TemperatureRecorded(requestId)

    case ReadTemperature(requestId) =>
      sender() ! RespondTemperature(requestId, lastTemperatureReading)

  }
}
