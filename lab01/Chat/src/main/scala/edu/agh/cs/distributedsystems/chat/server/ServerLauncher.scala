package edu.agh.cs.distributedsystems.chat.server

import edu.agh.cs.distributedsystems.chat.AppConfig

object ServerLauncher {
  def main(args: Array[String]): Unit = {
    val server = new Server(port = AppConfig.ServerPort)
    server.run()
  }
}
