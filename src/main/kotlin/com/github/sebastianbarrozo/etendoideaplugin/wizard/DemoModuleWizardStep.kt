package com.github.sebastianbarrozo.etendoideaplugin.wizard

import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.AbstractNewProjectWizardBuilder
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep
import com.intellij.ide.wizard.chain
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.UIBundle
import com.intellij.util.ui.EmptyIcon
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import javax.swing.Icon
import javax.swing.ImageIcon


class DemoModuleWizardStep : AbstractNewProjectWizardBuilder() {
    override fun getPresentableName() = "New Etendo project"
    override fun getDescription() = UIBundle.message("label.project.wizard.project.generator.description")
    override fun getNodeIcon(): Icon = EmptyIcon.ICON_13
    override fun getGroupName(): String = "Etendo"

    override fun createStep(context: WizardContext) =
            RootNewProjectWizardStep(context).chain(
                    ::newProjectWizardBaseStepWithoutGap,
            )

    fun newProjectWizardBaseStepWithoutGap(parent: NewProjectWizardStep): NewProjectWizardBaseStep {
        return NewProjectWizardBaseStep(parent).apply {  }
    }

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {

        // Call the super method first to ensure the module is set up correctly
        super.setupRootModel(modifiableRootModel)

        // Get the module's project and directory
        val moduleContentRoot = modifiableRootModel.contentRoots[0]

        // Now you can perform additional setup, such as generating a build.gradle file
        // from a template and placing it in the module's directory

        // For example:
        val inputStream: InputStream = javaClass.getResourceAsStream("/templates/build.gradle")
        val buildGradleFile: File = VfsUtil.virtualToIoFile(moduleContentRoot).toPath().resolve("build.gradle").toFile()
        try {
            Files.copy(inputStream, buildGradleFile.toPath())
        } catch (e: IOException) {
            // Handle error
        }

        // Or you can use the Gradle API to create a Gradle build script programmatically

        // Refresh the directory to show the new file in IntelliJ
        moduleContentRoot.refresh(false, true)
    }

}
