package edu.agh.cs.distributedsystems.chat

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.Socket
import scala.annotation.tailrec

class ClientConnection(
                        val client: Client,
                        val serverHostname: String,
                        val serverPort: Int,
                        val onServerConnectionTermination: () => Unit = () => (),
                      ) extends Runnable {
  private val clientSocket = new Socket(serverHostname, serverPort)

  private val out = new PrintWriter(clientSocket.getOutputStream, true)
  private val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))

  def sendMessage(message: String): Unit = {
    out.println(message)
  }

  @tailrec
  private def listenForMessages(): Unit = {
    val serverMessage = in.readLine()

    if (serverMessage == null) {
      onServerConnectionTermination()
    } else {
      client.receiveMessage(serverMessage)
      listenForMessages()
    }
  }

  override def run(): Unit = try {
    listenForMessages()
  } finally {
    clientSocket.close()
  }
}
