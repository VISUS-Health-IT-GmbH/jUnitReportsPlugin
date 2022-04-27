/*  JUnitHTMLReportsTask.kt
 *
 *  Copyright (C) 2021, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure.tasks.gathering

import javax.inject.Inject

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport

import com.visus.infrastructure.tasks.TASK_GROUP_GATHERING


/** the default task name for a task of type "JUnitHTMLReportsTask" */
internal const val JUNIT_HTML_REPORTS_TASK_NAME = "gatherJUnitHTMLReports"


/**
 *  JUnitHTMLReportsTask:
 *  ====================
 *
 *  Task to gather jUnit HTML reports from subprojects
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitHTMLReportsTask @Inject constructor(subprojects: Set<Project>) : TestReport() {
    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_GATHERING
        outputs.upToDateWhen { false }

        // Delete output if already exists
        project.delete("${project.buildDir}/jUnit")

        // Necessary task parameters
        destinationDir = project.file("${project.buildDir}/jUnit")
        this.reportOn(
            subprojects.map {
                it.tasks.withType(Test::class.java)
            }.flatten()
        )
    }
}
