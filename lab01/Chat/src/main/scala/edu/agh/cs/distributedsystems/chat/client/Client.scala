package edu.agh.cs.distributedsystems.chat.client

import edu.agh.cs.distributedsystems.chat.AppConfig

import scala.io.StdIn.readLine

class Client extends Runnable {
  private val connection = new ClientConnection(
    client = this,
    serverHostname = AppConfig.ServeHostname,
    serverPort = AppConfig.ServerPort,
    onServerConnectionTermination = handleServerConnectionTermination
  )
  private val connectionThread = new Thread(connection)
  connectionThread.start()

  def receiveMessage(message: String): Unit = {
    println()
    println(s"somebody: $message")
    print("> ")
  }

  private def handleServerConnectionTermination(): Unit = {
    println()
    println("Connection with server was terminated.")
    sys.exit(-1)
  }

  override def run(): Unit = try {
    while (true) {
      print("> ")
      val newMessage = readLine()
      connection.sendMessage(newMessage)
    }
  } finally {
    connectionThread.interrupt()
  }
}
