package edu.agh.cs.distributedsystems.chat

object ServerLauncher {
  def main(args: Array[String]): Unit = {
    val server = new Server(port = AppConfig.ServerPort)
    new Thread(server).start()
  }
}
