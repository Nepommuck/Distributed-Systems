package edu.agh.cs.distributedsystems.chat

import scala.io.StdIn.readLine

class Client extends Runnable {
  private val connection = new ClientConnection(
    client = this,
    serverHostname = AppConfig.ServeHostname,
    serverPort = AppConfig.ServerPort,
    onServerConnectionTermination = handleServerConnectionTermination,
  )
  private val clientThread = Thread.currentThread()
  private val connectionThread = new Thread(connection)
  connectionThread.start()

  def receiveMessage(message: String): Unit = {
    println()
    println(s"somebody: $message")
    print("> ")
  }

  private def handleServerConnectionTermination(): Unit = {
    println()
    println("Connection with server terminated.")
    clientThread.interrupt()
  }

  override def run(): Unit = try {
    while (true) {
      print("> ")
      val newMessage = readLine()
      connection.sendMessage(newMessage)
      Thread.currentThread()
      //    Thread.sleep(500)
    }
  } finally {
    connectionThread.interrupt()
  }
}
