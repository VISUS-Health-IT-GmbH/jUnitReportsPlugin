/*  MetadataTask.kt
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
package com.visus.infrastructure.tasks.artifacts

import java.io.File
import java.nio.charset.Charset

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.Project

import com.visus.infrastructure.data.jUnitReportsMetadata
import com.visus.infrastructure.data.toJSON
import com.visus.infrastructure.tasks.TASK_GROUP_ARTIFACTS


/** the default task name for a task of type "MetadataTask" */
internal const val METADATA_TASK_NAME = "createJUnitMetadataFile"

/** the default file name which is used by a task of type "MetadataTask" */
internal const val METADATA_FILE_NAME = "jUnit.json"


/**
 *  MetadataTask:
 *  ============
 *
 *  Task which creates the "jUnit.json" file
 *
 *  @author Tobias Hahnen
 */
abstract class MetadataTask @Inject constructor(@Internal val subprojects: Set<Project>) : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout


    /** project version (maybe null) */
    @Input var version: String? = null

    /** project release candidate (maybe null) */
    @Input var rc: String? = null

    /** file name of JSON file containing metadata - defaults to METADATA_FILE_NAME */
    @Input var metadataFileName: String = METADATA_FILE_NAME


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_ARTIFACTS
        outputs.upToDateWhen { false }
    }


    /** Can't use "output" as OutputFile due to pl.projectDirectory / pl.buildDirectory! */
    @TaskAction
    fun createMetadata() {
        val textJSON = toJSON(jUnitReportsMetadata(
            System.getProperty("BUILD_NUMBER").toInt(),
            System.getProperty("BRANCH_NAME"),
            System.getProperty("COMMIT_HASH"),
            version,
            rc,
            when {
                System.getProperties().containsKey("BUILDSERVER")   -> System.getProperty("BUILDSERVER")
                else                                                -> null
            },
            subprojects.map { it.name }
        ))

        File("${pl.projectDirectory}/$metadataFileName").writeText(textJSON, Charset.defaultCharset())
    }
}
