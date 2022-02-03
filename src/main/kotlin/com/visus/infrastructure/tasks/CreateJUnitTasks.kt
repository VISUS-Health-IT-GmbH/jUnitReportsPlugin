/*  CreateJUnitTasks.kt
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
package com.visus.infrastructure.tasks

import java.nio.charset.Charset

import groovy.lang.Closure

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import com.visus.infrastructure.data.jUnitReportsMetadata
import com.visus.infrastructure.extension.t
import com.visus.infrastructure.util.FilteringFunction



/** task names of tasks used for creating artifacts */
internal const val CREATE_JUNIT_ARCHIVE_TASK_NAME = "createJUnitResultsArchive"
internal const val CREATE_JUNIT_METADATA_TASK_NAME = "createJUnitMetadataFile"


/**
 *  Creates the "createJUnitResultsArchive" task in given project
 *
 *  @param input where to gather files from
 *  @param output ZIP archive path
 */
internal fun Project.createCreateJUnitResultsArchiveTask(input: String, output: String) = this.tasks.register<Zip>(
    CREATE_JUNIT_ARCHIVE_TASK_NAME
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Creating ZIP archive depends on gathering XML files
    dependsOn(GATHER_JUNIT_XML_TASK_NAME)

    // Set necessary task parameters!
    group = TASK_GROUP_PREPARATION
    archiveFileName.set(output)
    destinationDirectory.set(this@createCreateJUnitResultsArchiveTask.projectDir)
    from("${this@createCreateJUnitResultsArchiveTask.buildDir}$input")
}


/**
 *  Creates the "createJUnitMetadataFile" task in given project
 *
 *  @param output JSON file path
 *  @param productVersion (optional) product version
 *  @param productRC (optional) product release candidate
 *  @param filteringFunction filtering function for subprojects
 *  @param filteringFunctionGroovy if filtering function is Groovy closure or Kotlin lambda
 */
internal fun Project.createCreateJUnitMetadataFileTask(output: String, productVersion: String?, productRC: String?,
                                                       filteringFunction: Any,
                                                       filteringFunctionGroovy: Boolean) = this.tasks.register(
    CREATE_JUNIT_METADATA_TASK_NAME
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Creating metadata depends on creating ZIP archive
    dependsOn(CREATE_JUNIT_ARCHIVE_TASK_NAME)

    // Set necessary task parameters!
    group = TASK_GROUP_PREPARATION
    val textJSON = jacksonObjectMapper().writeValueAsString(jUnitReportsMetadata(
        this@createCreateJUnitMetadataFileTask.providers.systemProperty("BUILD_NUMBER")
            .forUseAtConfigurationTime().get().toInt(),
        this@createCreateJUnitMetadataFileTask.providers.systemProperty("BRANCH_NAME")
            .forUseAtConfigurationTime().get(),
        this@createCreateJUnitMetadataFileTask.providers.systemProperty("COMMIT_HASH")
            .forUseAtConfigurationTime().get(),
        productVersion,
        productRC,
        this@createCreateJUnitMetadataFileTask.providers.systemProperty("BUILDSERVER").forUseAtConfigurationTime().isPresent
            t this@createCreateJUnitMetadataFileTask.providers.systemProperty("BUILDSERVER").forUseAtConfigurationTime().get()
            ?: null,
        this@createCreateJUnitMetadataFileTask.subprojects.filter {
            when(filteringFunctionGroovy) {
                true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
                false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
            }
        }.map { it.name }
    ))

    this@createCreateJUnitMetadataFileTask.file(
        "${this@createCreateJUnitMetadataFileTask.projectDir}$output"
    ).absoluteFile.writeText(textJSON, Charset.defaultCharset())
}
