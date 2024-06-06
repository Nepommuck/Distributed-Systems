package agh.distributedsystems.zookeeper

import kotlin.time.Duration

object HtmlUtils {
    fun generateResultHtmlPage(descendantsCount: Int, reloadTimeout: Duration, znodeTree: ZnodeTree?): String = """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport"
                  content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
            <meta http-equiv="X-UA-Compatible" content="ie=edge">
            <title>Zookeeper-Watches</title>
            <script>
                function reload() {
                    setTimeout(() => {
                        window.location.replace(window.location.href);
                    }, ${reloadTimeout.inWholeMilliseconds})
                }
            </script>
        </head>
        <body onload="reload()">
        
        <h2>Number of descendants: $descendantsCount</h2>
        
        <h2>Full tree:</h2>
        <ul>
        ${znodeTree?.let { treeNodeToHtml(znodeTree.root) }.orEmpty()}
        </ul>
        
        </body>
        </html>
    """.trimIndent()

    private fun treeNodeToHtml(node: ZnodeTree.Node): String = """
        <li>
            ${node.name}
            <ul>
                ${node.children.joinToString(separator = "\n") { treeNodeToHtml(it) }}
            </ul>
        </li>
    """.trim()
}
