package com.github.sebastianbarrozo.etendoideaplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.sebastianbarrozo.etendoideaplugin.services.MyProjectService
import javax.swing.JButton
import javax.swing.JComboBox


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()
        private val project = toolWindow.project

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val label = JBLabel("Task");
            var cmb = JComboBox<String>(arrayOf("expand", "setup", "install", "smartbuild", "update.database"))
            add(label)
            add(cmb)
            add(JButton("Run").apply {
                addActionListener {
                    service.runGradleTask(project, cmb.selectedItem.toString())
                }
            })
        }
    }
}
