package agh.distributedsystems.zookeeper

import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.ZooKeeper

class ZnodeTree(val root: Node) {
    data class Node(val path: String, val children: List<Node>) {
        val name = path.substringAfterLast('/')
    }
}

object ZookeeperUtil {
    fun getZnodeTree(zk: ZooKeeper, rootZnodeName: String): ZnodeTree? =
        try {
            ZnodeTree(
                root = znodeAsTreeNode(zk, rootZnodeName),
            )
        } catch (e: KeeperException) {
            null
        }

    private fun znodeAsTreeNode(zk: ZooKeeper, znodeName: String): ZnodeTree.Node =
        ZnodeTree.Node(
            znodeName,
            children = zk.getChildren(znodeName, false)
                .map { "$znodeName/$it" }
                .map { znodeAsTreeNode(zk, it) },
        )
}
