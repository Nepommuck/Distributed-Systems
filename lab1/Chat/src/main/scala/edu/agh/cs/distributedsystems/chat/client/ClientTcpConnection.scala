package edu.agh.cs.distributedsystems.chat.client

import edu.agh.cs.distributedsystems.chat.common.{ProtocolMessage, TcpMessage}

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ConnectException, Socket}
import scala.annotation.tailrec

class ClientTcpConnection(
                        val client: Client,
                        val serverHostname: String,
                        val serverPort: Int,
                        val onServerConnectionTermination: () => Unit = () => (),
                      ) extends Runnable {
  private val clientSocket: Socket = try {
     new Socket(serverHostname, serverPort)
  } catch {
    case _: ConnectException =>
      println("Connection to server failed.")
      sys.exit(-1)
  }

  private val out = new PrintWriter(clientSocket.getOutputStream, true)
  private val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

  def sendMessage(message: TcpMessage): Unit = {
    message.encode.foreach(out.println)
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val serverRawMessage = in.readLine()

    if (serverRawMessage == null) {
      onServerConnectionTermination()
    } else {
      ProtocolMessage.decodeRawMessage(rawMessage = serverRawMessage) match {
        case Some(protocolMessage: TcpMessage) =>
          client.receiveMessage(protocolMessage)
      }
      listenForMessages()
    }
  }

  override def run(): Unit = try {
    listenForMessages()
  } finally {
    clientSocket.close()
  }
}
