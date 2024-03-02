package edu.agh.cs.distributedsystems.chat.server

import edu.agh.cs.distributedsystems.chat.common.ProtocolMessage

import java.net.{ServerSocket, Socket}
import scala.annotation.tailrec
import scala.collection.mutable

class Server(val port: Int) extends Runnable {
  private val serverSocket = new ServerSocket(port)
  private val connections = mutable.ListBuffer.empty[ServerConnection]

  def handleMessage(message: ProtocolMessage, clientSocket: Socket): Unit = {
    println(s"Received ${message.flag} message `${message.message}` " +
      s"from ${message.senderLogin} (${clientSocket.getRemoteSocketAddress})")
    connections.toList
      .filter(_.clientSocket.getRemoteSocketAddress != clientSocket.getRemoteSocketAddress)
      .foreach {
      _.sendMessage(message)
    }
  }

  private def establishConnection(clientSocket: Socket): Unit = {
    val newConnection = new ServerConnection(clientSocket, server = this)
    connections.addOne(newConnection)

    new Thread(newConnection).start()
    println(s"Established connection with `${clientSocket.getRemoteSocketAddress}`")
  }

  def terminateConnection(connection: ServerConnection): Unit = {
    connections.filterInPlace(_ != connection)
    println(s"Finished connection with `${connection.clientSocket.getRemoteSocketAddress}`")
  }

  @tailrec
  private def listenForNewConnections(): Unit = {
    val clientSocket = serverSocket.accept()
    establishConnection(clientSocket)
    listenForNewConnections()
  }

  override def run(): Unit = try {
    println("=== Server started ===")

    listenForNewConnections()
  } finally {
    serverSocket.close()
  }
}
