/*  JUnitXMLResultsTask.kt
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

import java.io.File

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskAction

import com.visus.infrastructure.tasks.TASK_GROUP_COMBINING


/** the default task name for a task of type "JUnitXMLResultsTask" */
internal const val JUNIT_XML_RESULTS_TASK_NAME = "combineJUnitXMLReports"


/**
 *  JUnitXMLResultsTask:
 *  ===================
 *
 *  Task to combine XML results of all tasks of type "Test"
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitXMLResultsTask : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout

    /** Configuration cache issue: project.delete not available in task action at execution time */
    @get:Inject abstract val fs: FileSystemOperations


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_COMBINING
        outputs.upToDateWhen { false }
    }


    // TODO: Don't use "$buildDir/test-results" but each "Test" task output directory instead!
    @TaskAction
    fun combineXMLResults() {
        val input = File("${pl.buildDirectory}/test-results")
        val output = File("${pl.buildDirectory}/jUnit/xmlresults")

        input.listFiles()?.forEach { folder ->
            if (folder.isDirectory && folder.listFiles()!!.isNotEmpty()) {
                fs.copy {
                    from(folder.absolutePath)
                    into(output)
                    exclude("**/binary/**")
                }
            }
        }
    }
}
