/*  GatherJUnitTasks.kt
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

import groovy.lang.Closure

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.register

import com.visus.infrastructure.util.FilteringFunction


/** task names of tasks used for gathering results in subprojects */
internal const val gatherJUnitHTMLTaskName  = "gatherJUnitHTMLReports"
internal const val gatherJUnitXMLTaskName   = "gatherJUnitXMLReports"


/**
 *  Creates the "gatherJUnitHTMLReports" task in given project
 *
 *  @param output directory for gathered HTML files
 *  @param filteringFunction filtering function for subprojects
 *  @param filteringFunctionGroovy if filtering function is Groovy closure or Kotlin lambda
 */
internal fun Project.createGatherJUnitHTMLTask(output: String, filteringFunction: Any,
                                               filteringFunctionGroovy: Boolean) = this.tasks.register<TestReport>(
    gatherJUnitHTMLTaskName
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Remove directory if found!
    this@createGatherJUnitHTMLTask.delete("${this@createGatherJUnitHTMLTask.buildDir}${output}")

    // Set necessary task parameters!
    group = taskGroupPreparation
    destinationDir = this@createGatherJUnitHTMLTask.file("${this@createGatherJUnitHTMLTask.buildDir}${output}")
    reportOn(
        this@createGatherJUnitHTMLTask.subprojects.filter {
            when(filteringFunctionGroovy) {
                true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
                false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
            }
        }.map {
            it.tasks.withType(Test::class.java)
        }.flatten()
    )
}


/**
 *  Creates the "gatherJUnitXMLReports" task in given project
 *
 *  @param input where to gather XML files from
 *  @param output directory for gathered XML files
 *  @param filteringFunction filtering function for subprojects
 *  @param filteringFunctionGroovy if filtering function is Groovy closure or Kotlin lambda
 */
internal fun Project.createGatherJUnitXMLTask(input: String, output: String, filteringFunction: Any,
                                              filteringFunctionGroovy: Boolean) = this.tasks.register(
    gatherJUnitXMLTaskName
) {
    // Must run again & should never be skipped!
    outputs.upToDateWhen { false }

    // Gathering XML depends on gathering HTML & combining XML in all subprojects
    dependsOn(gatherJUnitHTMLTaskName)
    dependsOn(
        this@createGatherJUnitXMLTask.subprojects.filter {
            when(filteringFunctionGroovy) {
                true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
                false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
            }
        }.map {
            it.tasks.getByName(combineJUnitXMLSubprojectsTaskName)
        }
    )

    // Set necessary task parameters!
    group = taskGroupPreparation

    // Action that will be performed when task gets created!
    this@createGatherJUnitXMLTask.subprojects.filter {
        when(filteringFunctionGroovy) {
            true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
            false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
        }
    }.forEach {
        this@createGatherJUnitXMLTask.copy {
            includeEmptyDirs = false
            from("${it.buildDir}${input}")
            into("${this@createGatherJUnitXMLTask.buildDir}$output/${it.name}")
        }
    }
}
