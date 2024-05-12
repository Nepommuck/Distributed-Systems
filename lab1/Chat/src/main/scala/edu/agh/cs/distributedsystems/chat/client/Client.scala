package edu.agh.cs.distributedsystems.chat.client

import edu.agh.cs.distributedsystems.chat.AppConfig
import edu.agh.cs.distributedsystems.chat.common.{ProtocolFlag, ProtocolMessage, TcpMessage, TcpTransmission, UdpTransmissionMessage, UdpTransmission}

import scala.annotation.tailrec
import scala.io.StdIn.readLine

case class Client(login: String) extends Runnable {

  private val tcpConnection = new ClientTcpConnection(
    client = this,
    serverHostname = AppConfig.ServeHostname,
    serverPort = AppConfig.ServerPort,
    onServerConnectionTermination = handleServerTcpConnectionTermination
  )
  private val tcpConnectionThread = new Thread(tcpConnection)

  private val udpConnection = new ClientUdpConnection(
    client = this,
    serverHostname = AppConfig.ServeHostname,
    serverPort = AppConfig.ServerPort,
  )

  tcpConnectionThread.start()
  new Thread(udpConnection).start()

  def receiveMessage(protocolMessage: ProtocolMessage): Unit = {
    val startLine = "  | "
    val messageToDisplay = protocolMessage.message
      .split('\n')
      .mkString(start = startLine, sep = s"\n$startLine", end = "")

    println(s"${protocolMessage.senderLogin}:\n$messageToDisplay")
    printCommandPrompt()
  }

  private def handleServerTcpConnectionTermination(): Unit = {
    println("\n" + "Connection with server was terminated.")
    sys.exit(-1)
  }

  private def printCommandPrompt(): Unit = {
    print("> ")
  }

  @tailrec
  private def readUserInput(
                             previousLines: List[String] = List.empty,
                             protocolFlag: ProtocolFlag = TcpTransmission
                           ): (String, ProtocolFlag) = {
    previousLines match {
      case Nil =>
        printCommandPrompt()
      case _ =>
        print("| ")
    }
    val userInputLine = readLine()
    userInputLine match {
      case "" => (previousLines.mkString(sep = "\n"), protocolFlag)
      case nextLine
        if nextLine.trim.startsWith(s"-${UdpTransmission.encodedValue}") && previousLines.isEmpty =>
        val restOfLine = nextLine
          .split(s"-${UdpTransmission.encodedValue}")
          .tail
          .mkString(sep = "")
        val allLines = if(restOfLine.isBlank) previousLines else previousLines :+ restOfLine
        readUserInput(allLines, protocolFlag = UdpTransmission)

      case nextLine => readUserInput(previousLines :+ nextLine, protocolFlag)
    }
  }

  override def run(): Unit = try {
    while (true) {
      readUserInput() match {
        case (newMessage, TcpTransmission) =>
          tcpConnection.sendMessage(TcpMessage(senderLogin = login, message = newMessage))

        case (newMessage, UdpTransmission) =>
          udpConnection.sendMessage(UdpTransmissionMessage(senderLogin = login, message = newMessage))
      }
    }
  } finally {
    tcpConnectionThread.interrupt()
  }
}
