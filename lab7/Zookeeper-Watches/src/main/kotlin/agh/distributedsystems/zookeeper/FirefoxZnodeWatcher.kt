package agh.distributedsystems.zookeeper

import org.apache.zookeeper.*
import java.io.File
import kotlin.time.Duration

class FirefoxZnodeWatcher(
    connectionString: String,
    sessionTimeout: Duration,
    private val zNodeName: String,
    private val resultFilePath: String,
    private val resultPageReloadTimeout: Duration,
) : Watcher {
    private val zk = ZooKeeper(connectionString, sessionTimeout.inWholeMilliseconds.toInt(), this)
    private val resultFile = File(resultFilePath)

    private var wasFirefoxOpened = false

    init {
        updateHtmlPage()
    }

    fun start() {
        try {
            zk.addWatch(zNodeName, AddWatchMode.PERSISTENT_RECURSIVE)
        } catch (e: KeeperException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun process(watchedEvent: WatchedEvent) {
        when (watchedEvent.path) {
            // Root znode affected
            zNodeName -> {
                handleRootEvent(watchedEvent)
            }

            // Child znode affected
            else -> {
                updateHtmlPage()
            }
        }
    }

    private fun handleRootEvent(watchedEvent: WatchedEvent) {
        when (watchedEvent.type) {
            Watcher.Event.EventType.NodeCreated -> {
                if (!wasFirefoxOpened) {
                    wasFirefoxOpened = true
                    FirefoxUtils.openNewTab(resultFilePath)
                } else {
                    FirefoxUtils.restoreLastSession()
                }
            }

            Watcher.Event.EventType.NodeDeleted -> {
                FirefoxUtils.closeAllTabs()
            }

            else -> {}
        }
    }

    private fun updateHtmlPage() {
        val newPageSource = HtmlUtils.generateResultHtmlPage(
            descendantsCount = try {
                zk.getAllChildrenNumber(zNodeName)
            } catch (e: KeeperException) {
                0
            },
            reloadTimeout = resultPageReloadTimeout,
            znodeTree = ZookeeperUtil.getZnodeTree(zk, zNodeName),
        )

        resultFile.writeText(newPageSource)
    }
}
