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

import com.visus.infrastructure.exception.HTMLFailedListParserException
import com.visus.infrastructure.exception.HTMLIgnoredNumberParserException
import com.visus.infrastructure.exception.HTMLFailedNumberParserException
import com.visus.infrastructure.extension.parseHTMLFailures
import com.visus.infrastructure.extension.parseHTMLFailedTests
import com.visus.infrastructure.extension.parseHTMLIgnored
import com.visus.infrastructure.extension.encodeBranchName
import com.visus.infrastructure.extension.t
import com.visus.infrastructure.jUnitReportsPlugin


/** task names of tasks used for publishing jUnit artifacts */
internal const val PUBLISH_NORMAL_JUNIT_TASK_NAME   = "publishJUnitNormal"
internal const val PUBLISH_RC_JUNIT_TASK_NAME       = "publishJUnitRC"
internal const val PUBLISH_JUNIT_RESULTS_TASK_NAME  = "publishJUnitResults"


/**
 *  Creates the "publishJUnitNormal" task in given project
 *
 *  @param zipFileName ZIP archive name
 *  @param metadataFileName JSON file name
 *  @param endpointREST REST endpoint URL
 */
internal fun Project.createPublishJUnitNormalTask(zipFileName: String, metadataFileName: String,
                                                  endpointREST: String) = this.tasks.register<Exec>(
    PUBLISH_NORMAL_JUNIT_TASK_NAME
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Publishing jUnit artifacts to REST endpoint depends on creating metadata
    dependsOn(CREATE_JUNIT_METADATA_TASK_NAME)

    // Set necessary task parameters!
    group = TASK_GROUP_ACTUAL_REPORTING
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
 *  TODO: Move generating of "failed_junit_tests.txt" to separate task!
 */
internal fun Project.createPublishJUnitRCTask(productRC: String, endpointDefaultTemplate: String,
                                              productVersionIsPatch: Boolean, endpointPatchTemplate: String,
                                              endpointVersionTemplate: String,
                                              productVersion: String) = this.tasks.register(
    PUBLISH_RC_JUNIT_TASK_NAME
) {
    // Must run again & should only be skipped when RC not valid!
    outputs.upToDateWhen { false }
    onlyIf { productRC.startsWith("RC") && !productRC.endsWith("_build") }

    // Publishing jUnit artifacts to folder depends on creating metadata
    dependsOn(CREATE_JUNIT_METADATA_TASK_NAME)

    // Set necessary task parameters!
    group = TASK_GROUP_ACTUAL_REPORTING

    // Action that will be performed when task gets created!
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
                        this@createPublishJUnitRCTask.logger.info(
                            "[${jUnitReportsPlugin::class.simpleName} -> Project.createPublishJUnitRCTask] Cannot " +
                            "parse list of failed jUnit tests from file '$pathToIndex'! See error: ${err.message}"
                        )

                        content += "This plugin could not list them in this file, so you must do it " +
                                    "yourself: Open junit-qa/jUnit.zip -> index.html and sort them out!"
                    }

                    try {
                        content += "There were ${this@createPublishJUnitRCTask.file(pathToIndex).parseHTMLIgnored()} " +
                                    "ignored jUnit tests as well. You should take a look why they were " +
                                    "skipped / ignored (on purpose?) and maybe reactivate them!"
                    } catch (err: HTMLIgnoredNumberParserException) {
                        this@createPublishJUnitRCTask.logger.info(
                            "[${jUnitReportsPlugin::class.simpleName} -> Project.createPublishJUnitRCTask] Cannot " +
                            "parse number of ignored jUnit tests from file '$pathToIndex'! See error: ${err.message}"
                        )
                    }

                    this@createPublishJUnitRCTask.file(this).absoluteFile.writeText(content, Charset.defaultCharset())
                }
            } catch (ignored: HTMLFailedNumberParserException) {
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
internal fun Project.createPublishJunitResultsTask() = this.tasks.register(PUBLISH_JUNIT_RESULTS_TASK_NAME) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Publishing artifacts depends on publish artifacts to REST endpoint and (optionally) to folder
    dependsOn(PUBLISH_NORMAL_JUNIT_TASK_NAME)
    dependsOn(PUBLISH_RC_JUNIT_TASK_NAME)

    // Set necessary task parameters!
    group = TASK_GROUP_ACTUAL_REPORTING
}
