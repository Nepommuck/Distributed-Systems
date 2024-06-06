package agh.distributedsystems.zookeeper

private const val FIREFOX_COMMAND = "firefox"

object FirefoxUtils {
    fun restoreLastSession() {
        Runtime.getRuntime().exec(FIREFOX_COMMAND)
    }

    fun openNewTab(url: String) {
        Runtime.getRuntime().exec(arrayOf(FIREFOX_COMMAND, url))
    }

    fun closeAllTabs() {
        Runtime.getRuntime().exec(arrayOf("pkill", FIREFOX_COMMAND))
    }
}
