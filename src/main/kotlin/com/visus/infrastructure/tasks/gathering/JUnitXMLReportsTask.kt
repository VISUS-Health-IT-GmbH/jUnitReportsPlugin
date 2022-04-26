/*  JUnitXMLReportsTask.kt
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

import java.io.File

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import com.visus.infrastructure.tasks.TASK_GROUP_GATHERING


/** the default task name for a task of type "JUnitXMLReportsTask" */
internal const val JUNIT_XML_REPORTS_TASK_NAME = "gatherJUnitXMLReports"


/**
 *  JUnitXMLReportsTask:
 *  ===================
 *
 *  Task to gather jUnit XML reports from subprojects
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitXMLReportsTask @Inject constructor(val subprojects: Set<Project>) : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout

    /** Configuration cache issue: project.delete not available in task action at execution time */
    @get:Inject abstract val fs: FileSystemOperations


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_GATHERING
        outputs.upToDateWhen { false }
    }


    /** Can't use "output" as OutputFile due to pl.buildDirectory! */
    @TaskAction
    fun combineXMLReports() {
        subprojects.forEach {
            fs.copy {
                from(File("${it.buildDir}/jUnit"))
                into(File("${pl.buildDirectory}/jUnit/projects/${it.name}"))
            }
        }
    }
}
