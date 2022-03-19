/*  ResultsArchiveTask.kt
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
package com.visus.infrastructure.tasks.artifacts

import org.gradle.api.tasks.bundling.Zip

import com.visus.infrastructure.tasks.TASK_GROUP_ARTIFACTS


/** the default task name for a task of type "ResultsArchiveTask" */
internal const val RESULTS_ARCHIVE_TASK_NAME = "createJUnitResultsArchive"

/** the default file name which is used by a task of type "ResultsArchiveTask" */
internal const val RESULTS_ARCHIVE_FILE_NAME = "jUnit.zip"


/**
 *  ResultsArchiveTask:
 *  ==================
 *
 *  Task which creates the "jUnit.zip" file
 *
 *  @author Tobias Hahnen
 */
abstract class ResultsArchiveTask : Zip() {
    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_ARTIFACTS
        outputs.upToDateWhen { false }

        // Necessary task parameters
        archiveFileName.convention(RESULTS_ARCHIVE_FILE_NAME)
        destinationDirectory.set(project.projectDir)
        this.from("${project.buildDir}/jUnit")
    }
}
