package com.github.sebastianbarrozo.etendoideaplugin.services

import com.github.sebastianbarrozo.etendoideaplugin.MyBundle
import com.intellij.execution.RunManagerEx
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.model.execution.ExternalTaskExecutionInfo
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory
import com.intellij.openapi.externalSystem.service.notification.NotificationData
import com.intellij.openapi.externalSystem.service.notification.NotificationSource
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import org.gradle.cli.CommandLineArgumentException
import org.gradle.cli.ParsedCommandLine
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.gradle.service.execution.cmd.GradleCommandLineOptionsConverter
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.util.concurrent.Executor
import org.gradle.cli.CommandLineParser;
import com.intellij.openapi.util.text.StringUtil;


@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    @Throws(CommandLineArgumentException::class)
    private fun buildTaskInfo(
        @NotNull projectPath: String, @NotNull fullCommandLine: String,
        @Nullable executor: Executor?
    ): ExternalTaskExecutionInfo? {
        val gradleCmdParser = CommandLineParser()
        val commandLineConverter = GradleCommandLineOptionsConverter()
        commandLineConverter.configure(gradleCmdParser)
        val parsedCommandLine: ParsedCommandLine =
            gradleCmdParser.parse(ParametersListUtil.parse(fullCommandLine, true, true))
        val optionsMap = commandLineConverter.convert(parsedCommandLine, HashMap())
        val systemProperties = optionsMap.remove("system-prop")
        val vmOptions =
            if (systemProperties == null) "" else StringUtil.join(systemProperties, { entry -> "-D$entry" }, " ")
        val scriptParameters: String = StringUtil.join(optionsMap.entries, { entry ->
            val values: List<String> = entry.value
            val longOptionName: String = entry.key
            if (values != null && !values.isEmpty()) {
                return@join StringUtil.join(values, { entry1 -> "--$longOptionName $entry1" }, " ")
            } else {
                return@join "--$longOptionName"
            }
        }, " ")
        val tasks = parsedCommandLine.extraArguments
        val settings = ExternalSystemTaskExecutionSettings()
        settings.externalProjectPath = projectPath
        settings.taskNames = tasks
        settings.scriptParameters = scriptParameters
        settings.vmOptions = vmOptions
        settings.externalSystemIdString = GradleConstants.SYSTEM_ID.toString()
        settings.executionName = tasks.toString()
        return ExternalTaskExecutionInfo(
            settings,
            DefaultRunExecutor.EXECUTOR_ID
        )
    }

    private fun runGradle(
        project: Project,
        fullCommandLine: String,
        workDirectory: String,
        executor: Executor?
    ) {
        val taskExecutionInfo: ExternalTaskExecutionInfo?
        try {
            taskExecutionInfo = buildTaskInfo(workDirectory, fullCommandLine, executor)
        } catch (ex: CommandLineArgumentException) {
            val notificationData = NotificationData(
                "<b>Command-line arguments cannot be parsed</b>",
                "<i>" + fullCommandLine + "</i> \n" + ex.message,
                NotificationCategory.WARNING, NotificationSource.TASK_EXECUTION
            )
            notificationData.isBalloonNotification = true
            ExternalSystemNotificationManager.getInstance(project)
                .showNotification(GradleConstants.SYSTEM_ID, notificationData)
            return
        }
        if (taskExecutionInfo != null) {
            ExternalSystemUtil.runTask(
                taskExecutionInfo.settings,
                taskExecutionInfo.executorId,
                project,
                GradleConstants.SYSTEM_ID
            )
        }
        val configuration = taskExecutionInfo?.let {
            ExternalSystemUtil.createExternalSystemRunnerAndConfigurationSettings(
                it.settings,
                project, GradleConstants.SYSTEM_ID
            )
        } ?: return
        val runManager = RunManagerEx.getInstanceEx(project)
        val existingConfiguration = runManager.findConfigurationByName(configuration.name)
        if (existingConfiguration == null) {
            runManager.setTemporaryConfiguration(configuration)
        } else {
            runManager.selectedConfiguration = existingConfiguration
        }
    }

    fun runGradleTask(project: Project, action: String): String {
        runGradle(project, action, project.basePath.toString(), null)
        return "";
    }
}
