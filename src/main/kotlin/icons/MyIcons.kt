package icons

import com.intellij.openapi.util.IconLoader

object MyIcons {
    @JvmField
    val MyAction = IconLoader.getIcon("/icons/myAction.png", javaClass)
    @JvmField
    val MyToolWindow = IconLoader.getIcon("/icons/pluginLogo_dark.svg.png", javaClass)
}
