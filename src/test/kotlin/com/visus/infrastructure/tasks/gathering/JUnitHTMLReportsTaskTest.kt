/*  JUnitHTMLReportsTaskTest.kt
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
package com.visus.infrastructure.tasks.gathering

import org.junit.Assert
import org.junit.Test

import org.gradle.api.tasks.TaskInstantiationException
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_GATHERING
import com.visus.infrastructure.util.FilteringFunction


/**
 *  JUnitHTMLReportsTaskTest:
 *  ========================
 *
 *  jUnit test cases on JUnitHTMLReportsTask
 */
open class JUnitHTMLReportsTaskTest {
    /** 1) Test creating task without necessary parameters */
    @Test fun testCreateWithoutParameters() {
        val project = ProjectBuilder.builder().build()
        project.tasks.register<JUnitHTMLReportsTask>(JUNIT_HTML_REPORTS_TASK_NAME)

        try {
            project.tasks.getByName(JUNIT_HTML_REPORTS_TASK_NAME) as JUnitHTMLReportsTask
        } catch (e: Exception) {
            Assert.assertEquals(TaskInstantiationException::class, e.cause!!::class)
        }
    }


    /** 2) Test creating task with wrong parameters (Groovy closure) */
    @Test fun testCreateWrongParameters1() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()

        project.tasks.register<JUnitHTMLReportsTask>(JUNIT_HTML_REPORTS_TASK_NAME, 3, true)

        try {
            project.tasks.getByName(JUNIT_HTML_REPORTS_TASK_NAME) as JUnitHTMLReportsTask
        } catch (e: Exception) {
            Assert.assertEquals(TaskInstantiationException::class, e.cause!!::class)
            Assert.assertEquals(ClassCastException::class, e.cause!!.cause!!::class)
        }
    }


    /** 3) Test creating task with wrong parameters (Kotlin FilteringFunction) */
    @Test fun testCreateWrongParameters2() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()

        project.tasks.register<JUnitHTMLReportsTask>(JUNIT_HTML_REPORTS_TASK_NAME, "curry", false)

        try {
            project.tasks.getByName(JUNIT_HTML_REPORTS_TASK_NAME) as JUnitHTMLReportsTask
        } catch (e: Exception) {
            Assert.assertEquals(TaskInstantiationException::class, e.cause!!::class)
            Assert.assertEquals(ClassCastException::class, e.cause!!.cause!!::class)
        }
    }


    /** 4) Test creating task with correct parameters */
    @Test fun testCreateCorrectParameters() {
        val project = ProjectBuilder.builder().build()
        val subproject = ProjectBuilder.builder().withParent(project).build()
        subproject.tasks.register("test123", org.gradle.api.tasks.testing.Test::class) {
            /** INFO: IntelliJ IDEA false-positive */
            binaryResultsDirectory.set(subproject.projectDir)
        }

        project.tasks.register<JUnitHTMLReportsTask>(
            JUNIT_HTML_REPORTS_TASK_NAME, { _: String -> true } as FilteringFunction, false
        )
        Assert.assertEquals(1, project.tasks.withType(JUnitHTMLReportsTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_HTML_REPORTS_TASK_NAME) as JUnitHTMLReportsTask?
        Assert.assertNotNull(task)
        Assert.assertEquals(TASK_GROUP_GATHERING, task!!.group)
        Assert.assertFalse(task.outputs.upToDateSpec.isEmpty)
        Assert.assertEquals(
            "${project.buildDir.absolutePath.replace("\\", "/")}/jUnit",
            task.destinationDir.absolutePath.replace("\\", "/")
        )
        Assert.assertEquals(1, task.testResultDirs.files.size)
        Assert.assertEquals(subproject.projectDir, task.testResultDirs.files.toList()[0])
    }
}
