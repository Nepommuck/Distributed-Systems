package agh.distributedsystems.zookeeper

import kotlin.time.Duration.Companion.seconds


const val RESULT_FILE_PATH = "result.html"
const val CONNECTION_STRING = "localhost:2181"

fun main() {
    val watcher = FirefoxZnodeWatcher(
        zNodeName = "/a",
        resultFilePath = RESULT_FILE_PATH,
        resultPageReloadTimeout = 1.seconds,
        connectionString = CONNECTION_STRING,
        sessionTimeout = 4.seconds,
    )

    watcher.start()
    Thread.sleep(Long.MAX_VALUE)
}
