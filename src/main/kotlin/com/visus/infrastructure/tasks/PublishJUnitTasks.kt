/*  PublishJUnitTasks.kt
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

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

import com.visus.infrastructure.exception.*
import com.visus.infrastructure.extension.*


/** task names of tasks used for publishing jUnit artifacts */
internal const val publishNormalJUnitTaskName   = "publishJUnitNormal"
internal const val publishRCJUnitTaskName       = "publishJUnitRC"
internal const val publishJUnitResultsTaskName  = "publishJUnitResults"


/**
 *  Creates the "publishJUnitNormal" task in given project
 *
 *  @param zipFileName ZIP archive name
 *  @param metadataFileName JSON file name
 *  @param endpointREST REST endpoint URL
 */
internal fun Project.createPublishJUnitNormalTask(zipFileName: String, metadataFileName: String,
                                                  endpointREST: String) = this.tasks.register<Exec>(
    publishNormalJUnitTaskName
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Publishing jUnit artifacts to REST endpoint depends on creating metadata
    dependsOn(createJUnitMetadataTaskName)

    // Set necessary task parameters!
    group = taskGroupActualReporting
    commandLine(
        "cmd", "/C", "curl", "--no-progress-bar",
        "-F", "\"zip_file=@$zipFileName\"", "-F", "\"metadata_file=@$metadataFileName\"",
        "-X", "POST", endpointREST
    )
}


/**
 *  Creates the "publishJUnitRC" task in given project
 *
 *  @param productRC product release candidate
 *  @param endpointDefaultTemplate default folder endpoint path
 *  @param productVersionIsPatch whether product version is a patch not a major / minor version
 *  @param endpointPatchTemplate patch folder endpoint path
 *  @param endpointVersionTemplate version folder endpoint path
 *  @param productVersion product version
 *
 *  TODO: Update hole method!
 */
internal fun Project.createPublishJUnitRCTask(productRC: String, endpointDefaultTemplate: String,
                                              productVersionIsPatch: Boolean, endpointPatchTemplate: String,
                                              endpointVersionTemplate: String,
                                              productVersion: String) = this.tasks.register(publishRCJUnitTaskName) {
    // Must run again & should only be skipped when RC not valid!
    outputs.upToDateWhen { false }
    onlyIf { productRC.startsWith("RC") && !productRC.endsWith("_build") }

    // Publishing jUnit artifacts to folder depends on creating metadata
    dependsOn(createJUnitMetadataTaskName)

    // Set necessary task parameters!
    group = taskGroupActualReporting
    doLast {
        // Check if failed jUnit tests available
        var failedTests = false

        with ("${this@createPublishJUnitRCTask.projectDir}/failed_junit_tests.txt") {
            val pathToIndex = "${this@createPublishJUnitRCTask.buildDir}/jUnit/index.html"

            try {
                val failures = this@createPublishJUnitRCTask.file(pathToIndex).parseHTMLFailures()
                if (failures != 0) {
                    var content = "[${this::class.simpleName}] There were $failures failing jUnit tests " +
                            "overall:\n\n"

                    try {
                        this@createPublishJUnitRCTask.file(pathToIndex).parseHTMLFailedTests().forEach {
                            content += "- $it\n  Explanation:\n\n"
                        }
                    } catch (err: HTMLFailedListParserException) {
                        // TODO: Log
                        content += "This plugin could not list them in this file, so you must do it " +
                                    "yourself: Open junit-qa/jUnit.zip -> index.html and sort them out!"
                    }

                    try {
                        content += "\n\nThere were " +
                                    "${this@createPublishJUnitRCTask.file(pathToIndex).parseHTMLIgnored()} " +
                                    "ignored jUnit tests as well. You should take a look why they were " +
                                    "skipped / ignored (on purpose?) and maybe reactivate them!"
                    } catch (err: HTMLIgnoredNumberParserException) {
                        // TODO: Log
                    }
                }

            } catch (err: HTMLFailedNumberParserException) {
                this@createPublishJUnitRCTask.file(this).absoluteFile.writeText(
                    "[${this::class.simpleName}] This plugin could not parse the jUnit report index.html " +
                    "file and therefore could not determine if there were failing jUnit tests or not. " +
                    "You must check yourself: Open junit-qa/jUnit-zip -> index.html and, if there were " +
                    "failed tests, add them here otherwise delete this file!",
                    Charset.defaultCharset()
                )
            }
        }

        // replace templated path
        val path = (when (failedTests) {
            false   -> endpointDefaultTemplate
            else    -> (productVersionIsPatch t endpointPatchTemplate) ?: endpointVersionTemplate
        }).replace(
            "{VERSION}", productVersion
        ).replace(
            "{RC}", productRC
        ).replace(
            "{BRANCH}", System.getProperty("BRANCH_NAME").encodeBranchName()
        ).replace(
            "{BUILDID}", System.getProperty("BUILD_NUMBER")
        )


        // add folders and copy content
        this@createPublishJUnitRCTask.mkdir(path)
        this@createPublishJUnitRCTask.mkdir("$path/junit-qa")

        if (failedTests) {
            // copy failed_junit_tests.txt
            this@createPublishJUnitRCTask.copy {
                from("${this@createPublishJUnitRCTask.projectDir}/failed_junit_tests.txt")
                into(path)
            }
        }

        // copy jUnit.zip (faster than copying folder)
        this@createPublishJUnitRCTask.copy {
            from("${this@createPublishJUnitRCTask.projectDir}/jUnit.zip")
            into("$path/junit-qa")
        }
    }
}


/**
 *  Creates the "publishJUnitResults" task in given project
 */
internal fun Project.createPublishJunitResultsTask() = this.tasks.register(publishJUnitResultsTaskName) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Publishing artifacts depends on publish artifacts to REST endpoint and (optionally) to folder
    dependsOn(publishNormalJUnitTaskName)
    dependsOn(publishRCJUnitTaskName)

    // Set necessary task parameters!
    group = taskGroupActualReporting
}
