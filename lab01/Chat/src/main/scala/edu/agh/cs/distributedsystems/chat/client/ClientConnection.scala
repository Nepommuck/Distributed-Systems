package edu.agh.cs.distributedsystems.chat.client

import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{ConnectException, Socket}
import scala.annotation.tailrec

class ClientConnection(
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
