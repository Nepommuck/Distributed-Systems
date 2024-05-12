package edu.agh.cs.distributedsystems.chat.util

import com.typesafe.scalalogging.Logger

trait Logging {
  protected val logger: Logger = Logger(getClass.getName)
}
