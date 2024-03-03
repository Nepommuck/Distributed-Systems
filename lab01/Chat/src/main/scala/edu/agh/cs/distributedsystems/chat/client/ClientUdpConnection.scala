package edu.agh.cs.distributedsystems.chat.client

import edu.agh.cs.distributedsystems.chat.common.{ProtocolMessage, UdpRegistrationMessage, UdpTransmissionMessage, UdpMessage}

import java.net.{DatagramPacket, DatagramSocket, InetAddress}
import scala.annotation.tailrec

class ClientUdpConnection(
                           val client: Client,
                           val serverHostname: String,
                           val serverPort: Int,
                         ) extends Runnable {
  private val clientSocket: DatagramSocket = new DatagramSocket()
  private val serverAddress: InetAddress = InetAddress.getByName(serverHostname)

  // Empty message for registration to server
  sendMessage(UdpRegistrationMessage(client.login))

  def sendMessage(message: UdpMessage): Unit = {
    message.encode.foreach { messageStr =>
      val datagramPacket = new DatagramPacket(messageStr.getBytes, messageStr.length, serverAddress, serverPort)
      clientSocket.send(datagramPacket);
    }
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val receiveBuffer = Array.ofDim[Byte](1024)
    val receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length)
    clientSocket.receive(receivedPacket)
    val serverRawMessage = new String(receivedPacket.getData.toList.filter(_ > 0).toArray)

    ProtocolMessage.decodeRawMessage(rawMessage = serverRawMessage) match {
      case Some(protocolMessage: UdpTransmissionMessage) =>
        client.receiveMessage(protocolMessage)
      case _ => ()
    }
    listenForMessages()
  }

  override def run(): Unit = try {
    listenForMessages()
  } finally {
    clientSocket.close()
  }
}
