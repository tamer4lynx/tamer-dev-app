package com.nanofuxion.tamerdevapp

import com.lynx.tasm.LynxGroup

/**
 * Single [LynxGroup] for coordinator + TamerNav stack spokes so they share a JS context group.
 * Used from [MainActivity] (release-style entry) and [ProjectActivity] (open-project entry).
 */
object TamerNavLynxRuntime {
    val group: LynxGroup = LynxGroup.LynxGroupBuilder()
        .setGroupName("TamerNav")
        .setID(LynxGroup.SINGNLE_GROUP)
        .setEnableJSGroupThread(true)
        .build()
}
