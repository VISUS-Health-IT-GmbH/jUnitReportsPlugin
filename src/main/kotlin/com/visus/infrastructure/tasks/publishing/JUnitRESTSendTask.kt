/*  JUnitRESTSendTask.kt
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

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input

import com.visus.infrastructure.tasks.TASK_GROUP_PUBLISHING
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/** task names of tasks used for publishing jUnit artifacts using REST API */
internal const val JUNIT_REST_SEND_TASK_NAME = "publishJUnitREST"


/**
 *  JUnitRESTSendTask:
 *  =================
 *
 *  Task which sends the required files to a specific REST endpoint
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitRESTSendTask : Exec() {
    /** file name of text file containing failed jUnit tests - defaults to FAILED_JUNIT_TESTS_FILE_NAME */
    @Input var failedJUnitTestsFileName : String = FAILED_JUNIT_TESTS_FILE_NAME

    /** file name of JSON file containing metadata - defaults to METADATA_FILE_NAME */
    @Input var metadataFileName: String = METADATA_FILE_NAME

    /** file name of ZIP archive containing test artifacts - defaults to RESULTS_ARCHIVE_FILE_NAME */
    @Input var zipFileName: String = RESULTS_ARCHIVE_FILE_NAME

    /** REST endpoint URL */
    @Input var endpointRESTURL: String? = null


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_PUBLISHING
        outputs.upToDateWhen { false }

        // Necessary task parameters
        commandLine = listOf(
            "curl", "--no-progress-bar", "-F", "\"failed_junit_tests=@$failedJUnitTestsFileName\"", "-F",
            "\"zip_file=@$zipFileName\"", "-F", "\"metadata_file=@$metadataFileName\"", "-X", "POST", endpointRESTURL!!
        )
    }
}
