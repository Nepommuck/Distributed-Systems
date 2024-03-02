package edu.agh.cs.distributedsystems.chat

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket
import scala.annotation.tailrec

class ServerConnection(val clientSocket: Socket, val server: Server) extends Runnable {
  val out = new PrintWriter(clientSocket.getOutputStream, true)
  val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

  def sendMessage(message: String): Unit = {
    out.println(message)
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val receivedMessage = in.readLine()

    if (receivedMessage != null) {
      server.handleMessage(receivedMessage, clientSocket)
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
