/*  CleanJUnitArtifacts.kt
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
package com.visus.infrastructure.tasks

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskAction


/** the default task name for a task of type "CleanJUnitArtifactsTask" */
internal const val CLEAN_ARTIFACT_TASK_NAME = "cleanJUnitArtifacts"


/**
 *  CleanJUnitArtifactsTask:
 *  =======================
 *
 *  Custom task to clean the artifacts produced by this plugin
 *
 *  @author Tobias Hahnen
 */
abstract class CleanJUnitArtifactsTask : DefaultTask() {

    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout

    /** Configuration cache issue: project.delete not available in task action at execution time */
    @get:Inject abstract val fs: FileSystemOperations


    @TaskAction
    fun removeArtifacts() {
        fs.delete {
            delete(
                "${pl.projectDirectory}/jUnit.zip",
                "${pl.projectDirectory}/jUnit.json",
                "${pl.projectDirectory}/failed_junit_tests.txt"
            )
        }
    }
}
