package edu.agh.cs.distributedsystems.chat.server

import edu.agh.cs.distributedsystems.chat.common.{ProtocolMessage, UdpMessage, UdpTransmissionMessage}
import edu.agh.cs.distributedsystems.chat.util.Logging

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import scala.annotation.tailrec
import scala.collection.mutable

case class UdpClient(address: InetAddress, port: Int)

object UdpClient {
  def apply(receivedPacket: DatagramPacket): UdpClient =
    UdpClient(address = receivedPacket.getAddress, port = receivedPacket.getPort)
}

case class ServerUdpHandler(server: Server) extends Runnable with Logging {
  private val socket = new DatagramSocket(server.port)
  private val registeredClients = mutable.ListBuffer.empty[UdpClient]

  private def sendMessage(udpMessage: UdpTransmissionMessage, sender: UdpClient): Unit = {
    udpMessage.encode match {
      case None => logger.error(s"Failed to encode an UDP message: '$udpMessage'")
      case Some(encodedMessage) =>
        registeredClients.toList
          .filter(_ != sender)
          .foreach { client =>
            val datagramPacket = new DatagramPacket(
              encodedMessage.getBytes, encodedMessage.length, client.address, client.port,
            )
            socket.send(datagramPacket)
          }
    }
  }

  private def registerClient(newClient: UdpClient): Unit = {
    if (!registeredClients.contains(newClient)) {
      registeredClients.addOne(newClient)
    }
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val receiveBuffer = Array.ofDim[Byte](1024)
    val receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length)
    socket.receive(receivedPacket)
    val receivedRawMessage = new String(receivedPacket.getData.toList.filter(_ > 0).toArray)

    ProtocolMessage.decodeRawMessage(rawMessage = receivedRawMessage) match {
      case Some(udpMessage: UdpMessage) =>
        val client = UdpClient(receivedPacket)
        registerClient(client)
        server.handleUdpMessage(udpMessage, receivedPacket.getSocketAddress)

        udpMessage match {
          case transmissionMessage: UdpTransmissionMessage => sendMessage(transmissionMessage, sender = client)
          case _ => ()
        }
      case _ =>
        logger.error(s"Received invalid UDP message: '$receivedRawMessage'")
    }
    listenForMessages()
  }

  override def run(): Unit = try {
    listenForMessages()
  } finally {
    socket.close()
  }
}
