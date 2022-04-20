/*  JUnitRCSaveTask.kt
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

import java.io.File

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import com.visus.infrastructure.extension.encodeBranchName
import com.visus.infrastructure.extension.versionABCx
import com.visus.infrastructure.extension.versionABx
import com.visus.infrastructure.extension.versionAx
import com.visus.infrastructure.tasks.TASK_GROUP_PUBLISHING
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/** task names of tasks used for publishing jUnit artifacts to file on RC */
internal const val JUNIT_RC_SAVE_TASK_NAME = "publishJUnitRC"


/**
 *  JUnitRCSaveTask:
 *  ===============
 *
 *  Task which saves the required files to a specific file endpoint when RC is correct
 *
 *  @author Tobias Hahnen
 */
abstract class JUnitRCSaveTask : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout

    /** Configuration cache issue: project.delete not available in task action at execution time */
    @get:Inject abstract val fs: FileSystemOperations


    /** project version (maybe null) */
    @Input var version: String? = null

    /** project release candidate (maybe null) */
    @Input var rc: String? = null

    /** whether product version is a patch not a major / minor version */
    @Input var productVersionIsPatch: Boolean? = null

    /** default folder endpoint path */
    @Input var endpointDefaultTemplate: String? = null

    /** version folder endpoint path */
    @Input var endpointVersionTemplate: String? = null

    /** patch folder endpoint path */
    @Input var endpointPatchTemplate: String? = null

    /** file name of text file containing failed jUnit tests - defaults to FAILED_JUNIT_TESTS_FILE_NAME */
    @Input var failedJUnitTestsFileName : String = FAILED_JUNIT_TESTS_FILE_NAME

    /** file name of JSON file containing metadata - defaults to METADATA_FILE_NAME */
    @Input var metadataFileName: String = METADATA_FILE_NAME

    /** file name of ZIP archive containing test artifacts - defaults to RESULTS_ARCHIVE_FILE_NAME */
    @Input var zipFileName: String = RESULTS_ARCHIVE_FILE_NAME


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_PUBLISHING
        outputs.upToDateWhen { false }
    }


    /** Task action performed when task is running */
    @TaskAction
    fun saveRCResults() {
        // get template path
        val path = productVersionIsPatch?.let {
            when (productVersionIsPatch) {
                true -> endpointPatchTemplate!!
                else -> endpointVersionTemplate!!
            }
        } ?: endpointDefaultTemplate!!

        // fill template with necessary information
        path.replace("{VERSION}", version!!)
            .replace("{VERSION_ABCx}", version!!.versionABCx())
            .replace("{VERSION_ABx}", version!!.versionABx())
            .replace("{VERSION_Ax}", version!!.versionAx())
            .replace("{RC}", rc!!)
            .replace("{BRANCH}", System.getProperty("BRANCH_NAME").encodeBranchName())
            .replace("{BUILDID}", System.getProperty("BUILD_NUMBER"))

        // create necessary folders on file endpoint
        with (File(path))               { if (!this.exists()) this.mkdir() }
        with (File("$path/junit-qa"))   { if (!this.exists()) this.mkdir() }

        // copy necessary files to folders
        with (File("${pl.projectDirectory}/$failedJUnitTestsFileName")) {
            if (this.exists()) {
                fs.copy {
                    from(this@with.absolutePath)
                    into(path)
                }
            }
        }

        fs.copy {
            from("${pl.projectDirectory}/$metadataFileName")
            into(path)
        }

        fs.copy {
            from("${pl.projectDirectory}/$zipFileName")
            into("$path/junit-qa")
        }
    }
}
