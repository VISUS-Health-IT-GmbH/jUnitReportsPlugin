/*  CombineJUnitSubprojectTasks.kt
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

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.register


/** task names of tasks only created in subprojects to combine results */
internal const val combineJUnitHTMLSubprojectsTaskName  = "combineJUnitHTMLReports"
internal const val combineJUnitXMLSubprojectsTaskName   = "combineJUnitXMLReports"


/**
 *  Creates the "combineJUnitHTMLReports" task in given project
 *
 *  @param output directory for combined HTML files
 */
internal fun Project.createCombineJUnitHTMLReportsTask(output: String) = this.tasks.register<TestReport>(
    combineJUnitHTMLSubprojectsTaskName
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Remove directory if found!
    this@createCombineJUnitHTMLReportsTask.delete("${this@createCombineJUnitHTMLReportsTask.buildDir}$output")

    // Set necessary task parameters!
    group = taskGroupPreparation
    destinationDir = this@createCombineJUnitHTMLReportsTask.file(
        "${this@createCombineJUnitHTMLReportsTask.buildDir}$output"
    )
    reportOn(this@createCombineJUnitHTMLReportsTask.tasks.withType(Test::class.java))
}


/**
 *  Creates the "combineJUnitXMLReports" task in given project
 *
 *  @param output directory for copied HTML files
 *
 *  TODO: Don't use "$buildDir/test-results" and each Test task output directory instead!
 */
internal fun Project.createCombineJUnitXMLReportsTask(output: String) = this.tasks.register(
    combineJUnitXMLSubprojectsTaskName
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Combining XML depends on combining HTML
    dependsOn(combineJUnitHTMLSubprojectsTaskName)

    // Set necessary task parameters including "doLast" action!
    group = taskGroupPreparation
    doLast {
        this@createCombineJUnitXMLReportsTask.file(
            "${this@createCombineJUnitXMLReportsTask.buildDir}/test-results"
        ).listFiles()!!.forEach { folder ->
            if (folder.isDirectory && folder.listFiles()!!.isNotEmpty()) {
                this@createCombineJUnitXMLReportsTask.copy {
                    includeEmptyDirs = false
                    from(folder.absolutePath)
                    into("${this@createCombineJUnitXMLReportsTask.buildDir}$output")
                    exclude("**/binary/**")
                }
            }
        }
    }
}
