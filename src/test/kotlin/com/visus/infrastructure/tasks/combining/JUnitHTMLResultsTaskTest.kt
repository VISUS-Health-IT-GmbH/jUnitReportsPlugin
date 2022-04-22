/*  JUnitHTMLResultsTaskTest.kt
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
package com.visus.infrastructure.tasks.combining

import org.junit.Assert
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_COMBINING


/**
 *  JUnitHTMLResultsTaskTest:
 *  ========================
 *
 *  jUnit test cases on JUnitHTMLResultsTask
 */
open class JUnitHTMLResultsTaskTest {
    /** 1) test creating task */
    @Test fun testCreate() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<JUnitHTMLResultsTask>(JUNIT_HTML_RESULTS_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(JUnitHTMLResultsTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_HTML_RESULTS_TASK_NAME) as JUnitHTMLResultsTask?
        Assert.assertNotNull(task)
        Assert.assertEquals(TASK_GROUP_COMBINING, task!!.group)
        Assert.assertEquals(
            "${project.buildDir.absolutePath.replace("\\", "/")}/jUnit",
            task.destinationDir.absolutePath.replace("\\", "/")
        )
    }
}
