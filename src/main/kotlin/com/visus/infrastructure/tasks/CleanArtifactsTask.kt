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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/** the default task name for a task of type "CleanArtifactsTask" */
internal const val CLEAN_ARTIFACT_TASK_NAME = "cleanJUnitArtifacts"


/**
 *  CleanArtifactsTask:
 *  ==================
 *
 *  Custom task to clean the artifacts produced by this plugin
 *
 *  @author Tobias Hahnen
 */
abstract class CleanArtifactsTask : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout

    /** Configuration cache issue: project.delete not available in task action at execution time */
    @get:Inject abstract val fs: FileSystemOperations


    /** file name of text file containing failed jUnit tests - defaults to FAILED_JUNIT_TESTS_FILE_NAME */
    @Input var failedJUnitTestsFileName : String = FAILED_JUNIT_TESTS_FILE_NAME

    /** file name of JSON file containing metadata - defaults to METADATA_FILE_NAME */
    @Input var metadataFileName: String = METADATA_FILE_NAME

    /** file name of ZIP archive containing test artifacts - defaults to RESULTS_ARCHIVE_FILE_NAME */
    @Input var zipFileName: String = RESULTS_ARCHIVE_FILE_NAME


    /** Constructor */
    init {
        // Set group!
        group = TASK_GROUP_CLEANING
    }


    @TaskAction
    fun removeArtifacts() {
        fs.delete {
            delete(
                "${pl.projectDirectory}/$failedJUnitTestsFileName",
                "${pl.projectDirectory}/$metadataFileName",
                "${pl.projectDirectory}/$zipFileName"
            )
        }
    }
}
