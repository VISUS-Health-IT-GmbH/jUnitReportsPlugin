/*  JUnitHTMLResultsTask.kt
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
package com.visus.infrastructure.tasks.combining

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport

import com.visus.infrastructure.tasks.TASK_GROUP_COMBINING


/** the default task name for a task of type "JUnitHTMLResultsTask" */
internal const val JUNIT_HTML_RESULTS_TASK_NAME = "combineJUnitHTMLReports"


/**
 *  JUnitHTMLResultsTask:
 *  ====================
 *
 *  Task to combine reports of all tasks of type "Test"
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitHTMLResultsTask : TestReport() {
    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_COMBINING
        outputs.upToDateWhen { false }

        // Delete output if already exists
        project.delete("${project.buildDir}/jUnit")

        // Necessary task parameters
        destinationDir = project.file("${project.buildDir}/jUnit")
        this.reportOn(project.tasks.withType(Test::class.java))
    }
}
