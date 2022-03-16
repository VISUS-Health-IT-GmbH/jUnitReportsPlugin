/*  FailedJUnitTestsTask.kt
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
import org.gradle.api.tasks.TaskAction

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.HTMLFailedListParserException
import com.visus.infrastructure.exception.HTMLFailedNumberParserException
import com.visus.infrastructure.exception.HTMLIgnoredNumberParserException
import com.visus.infrastructure.extension.parseHTMLFailedTests
import com.visus.infrastructure.extension.parseHTMLFailures
import com.visus.infrastructure.extension.parseHTMLIgnored
import com.visus.infrastructure.tasks.TASK_GROUP_ARTIFACTS


/** the default task name for a task of type "FailedJUnitTestsTask" */
internal const val FAILED_JUNIT_TESTS_TASK_NAME = "createJUnitFailedTestsTXT"

/** the default file name which is used by a task of type "FailedJUnitTestsTask" */
internal const val FAILED_JUNIT_TESTS_FILE_NAME = "failed_junit_tests.txt"


/**
 *  FailedJUnitTestsTask:
 *  ====================
 *
 *  Task which creates the "failed_junit_tests.txt" file
 *
 *  @author Tobias Hahnen
 */
abstract class FailedJUnitTestsTask : DefaultTask() {
    /** Configuration cache issue: project.buildDir not available in task action at execution time */
    @get:Inject abstract val pl: ProjectLayout


    /** file name of text file containing failed jUnit tests - defaults to FAILED_JUNIT_TESTS_FILE_NAME */
    @Input var failedJUnitTestsFileName : String = FAILED_JUNIT_TESTS_FILE_NAME


    /** Constructor */
    init {
        // Set group and never skip but always run!
        group = TASK_GROUP_ARTIFACTS
        outputs.upToDateWhen { false }
    }


    /** Can't use "index" as InputFile and "output" as OutputFile due to pl.projectDirectory / pl.buildDirectory! */
    @TaskAction
    fun createFailedJunitTestsTXT() {
        val output = File("${pl.projectDirectory}/$failedJUnitTestsFileName")
        val index = File("${pl.buildDirectory}/jUnit/index.html")

        try {
            val numberOfFailures = index.parseHTMLFailures()
            if (numberOfFailures > 0) {
                var content = "[${jUnitReportsPlugin::class.simpleName}] There were $numberOfFailures failing jUnit " +
                                "tests overall:\n\n"

                try {
                    index.parseHTMLFailedTests().forEach {
                        content += "- $it\n  Explanation:\n\n"
                    }
                } catch (err: HTMLFailedListParserException) {
                    content += "This plugin could not list them in this file, so you must do it yourself: Open " +
                                "junit-qa/jUnit.zip -> index.html and sort them out!"

                    logger.warn(
                        "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] " +
                        "Parsing list of failed jUnit tests threw an exception: ${err.message}"
                    )
                    logger.warn(
                        "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] This " +
                        "is not a critical issue but disturbs the developer experience for quality assurance!"
                    )
                }

                try {
                    val numberOfSkipped = index.parseHTMLIgnored()
                    if (numberOfSkipped > 0) {
                        content += "There were $numberOfSkipped ignored jUnit tests as well. You should take a look " +
                                    "why they were skipped / ignored (on purpose?) and maybe reactivate them!"
                    }
                } catch (err: HTMLIgnoredNumberParserException) {
                    logger.warn(
                        "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] " +
                        "Parsing number of skipped jUnit tests threw an exception: ${err.message}"
                    )
                    logger.warn(
                        "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] This " +
                        "is not a critical issue but disturbs the developer experience for quality assurance!"
                    )
                }

                output.writeText(content, Charset.defaultCharset())
            }
        } catch (err: HTMLFailedNumberParserException) {
            output.writeText(
                "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] This " +
                "plugin could not parse the jUnit report index.html file and therefore could not determine if there " +
                "were failing jUnit tests or not. You must check yourself: Open junit-qa/jUnit-zip -> index.html " +
                "and, if there were failed tests, add them here otherwise delete this file!",
                Charset.defaultCharset()
            )

            logger.warn(
                "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] Parsing " +
                "number of failed jUnit tests threw an exception: ${err.message}"
            )
            logger.warn(
                "[${jUnitReportsPlugin::class.simpleName} -> ${this.name} (${this::class.simpleName})] This is not a " +
                "critical issue but disturbs the developer experience for quality assurance!"
            )
        }
    }
}
