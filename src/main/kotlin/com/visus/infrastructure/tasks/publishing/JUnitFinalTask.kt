/*  JUnitFinalTask.kt
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure.tasks.publishing

import org.gradle.api.DefaultTask

import com.visus.infrastructure.tasks.TASK_GROUP_PUBLISHING


/** task names of tasks used for publishing jUnit artifacts */
internal const val JUNIT_FINAL_TASK_NAME = "publishJUnitResults"


/**
 *  JUnitFinalTask:
 *  ==============
 *
 *  @author Tobias Hahnen
 */
internal abstract class JUnitFinalTask : DefaultTask() {
    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_PUBLISHING
        outputs.upToDateWhen { false }

        // Specify dependencies
        dependsOn(project.tasks.withType(JUnitRESTSendTask::class.java))
        dependsOn(project.tasks.withType(JUnitRCSaveTask::class.java))
    }
}
