package edu.agh.cs.distributedsystems.chat.server

import edu.agh.cs.distributedsystems.chat.common.ProtocolMessage

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket
import scala.annotation.tailrec

class ServerConnection(val clientSocket: Socket, val server: Server) extends Runnable {
  val out = new PrintWriter(clientSocket.getOutputStream, true)
  val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

  def sendMessage(protocolMessage: ProtocolMessage): Unit = {
    protocolMessage.encode match {
      case Some(encodedMessage) => out.println(encodedMessage)
      case None => println(s"Failed to encode message: $protocolMessage")
    }
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val receivedRawMessage = in.readLine()

    if (receivedRawMessage != null) {
      ProtocolMessage.decodeRawMessage(rawMessage = receivedRawMessage) match {
        case Some(protocolMessage) =>
          server.handleMessage(protocolMessage, clientSocket)
        case None =>
          println(s"Received invalid message: `$receivedRawMessage`")
      }
      listenForMessages()
    }
  }

  override def run(): Unit = try {
    listenForMessages()
  } finally {
    server.terminateConnection(connection = this)
    clientSocket.close()
  }
}
