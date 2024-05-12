package edu.agh.cs.distributedsystems.chat.server

import edu.agh.cs.distributedsystems.chat.common._
import edu.agh.cs.distributedsystems.chat.util.Logging

import java.net.{ServerSocket, Socket, SocketAddress}
import scala.annotation.tailrec
import scala.collection.mutable

class Server(val port: Int) extends Runnable with Logging {
  private val serverSocket = new ServerSocket(port)
  private val tcpConnections = mutable.ListBuffer.empty[ServerTcpConnection]

  private val udpHandlerThread = new Thread(ServerUdpHandler(server = this))
  udpHandlerThread.start()

  def handleTcpMessage(message: TcpMessage, clientSocket: Socket): Unit = {
    logIncomingMessage(message, address = clientSocket.getRemoteSocketAddress)
    tcpConnections.toList
      .filter(_.clientSocket.getRemoteSocketAddress != clientSocket.getRemoteSocketAddress)
      .foreach {
        _.sendMessage(message)
      }
  }

  def handleUdpMessage(message: UdpMessage, address: SocketAddress): Unit = {
    message match {
      case UdpRegistrationMessage(login) =>
        logger.info(s"User $login ($address) registered via UDP")
      case _: UdpTransmissionMessage =>
        logIncomingMessage(message, address)
    }
  }

  def terminateTcpConnection(connection: ServerTcpConnection): Unit = {
    tcpConnections.filterInPlace(_ != connection)
    logger.info(s"Finished connection with `${connection.clientSocket.getRemoteSocketAddress}`")
  }

  private def logIncomingMessage(message: ProtocolMessage, address: SocketAddress): Unit = {
    logger.info(s"Received ${message.protocolFlag} message '${message.message}' " +
      s"from ${message.senderLogin} ($address)")
  }

  private def establishTcpConnection(clientSocket: Socket): Unit = {
    val newConnection = new ServerTcpConnection(clientSocket, server = this)
    tcpConnections.addOne(newConnection)

    new Thread(newConnection).start()
    logger.info(s"Established TCP connection with '${clientSocket.getRemoteSocketAddress}'")
  }

  @tailrec
  private def listenForNewTcpConnections(): Unit = {
    val clientSocket = serverSocket.accept()
    establishTcpConnection(clientSocket)
    listenForNewTcpConnections()
  }

  override def run(): Unit = try {
    logger.info("SERVER STARTED")

    listenForNewTcpConnections()
  } finally {
    serverSocket.close()
    udpHandlerThread.interrupt()
  }
}
