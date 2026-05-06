package com.nanofuxion.tamerdevapp

import android.content.Context
import com.lynx.tasm.LynxBooleanOption
import com.lynx.tasm.LynxGroup
import com.lynx.tasm.LynxViewBuilder
import com.lynx.tasm.group.ILynxViewGroup
import com.lynx.tasm.group.LynxViewGroupBuilder
import com.lynx.xelement.XElementBehaviors

/**
 * Shared [LynxGroup] plus per-bundle LynxViewGroups for coordinator + TamerNav stack spokes.
 * Used from [MainActivity] (release-style entry) and [ProjectActivity] (open-project entry).
 */
object TamerNavLynxRuntime {
    val group: LynxGroup = LynxGroup.LynxGroupBuilder()
        .setGroupName("TamerNav")
        .setID(LynxGroup.SINGNLE_GROUP)
        .setEnableJSGroupThread(true)
        .build()

    private val viewGroups = LinkedHashMap<String, ILynxViewGroup>()

    @Synchronized
    fun viewGroup(context: Context, src: String): ILynxViewGroup {
        val key = src.ifBlank { "main.lynx.bundle" }
        return viewGroups.getOrPut(key) {
            val appContext = context.applicationContext ?: context
            val provider = TemplateProvider(appContext)
            val groupBuilder = LynxViewGroupBuilder()
                .setContext(appContext)
                .setUrl(key)
                .setLynxGroup(group)
                .addBehaviors(XElementBehaviors().create())
            groupBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
            groupBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
            groupBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
            groupBuilder.build()
        }
    }

    fun configureBuilder(context: Context, viewBuilder: LynxViewBuilder, src: String) {
        val provider = TemplateProvider(context)
        viewBuilder.setLynxViewGroup(viewGroup(context, src))
        viewBuilder.setLynxGroup(group)
        viewBuilder.setTemplateProvider(provider)
        viewBuilder.setEnableGenericResourceFetcher(LynxBooleanOption.TRUE)
        viewBuilder.setTemplateResourceFetcher(provider.templateResourceFetcher)
        viewBuilder.setGenericResourceFetcher(provider.genericResourceFetcher)
    }
}
