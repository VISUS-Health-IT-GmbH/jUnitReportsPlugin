/*  JUnitXMLReportsTaskTest.kt
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

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_GATHERING
import com.visus.infrastructure.util.FilteringFunction


/**
 *  JUnitXMLReportsTaskTest:
 *  =======================
 *
 *  jUnit test cases on JUnitXMLReportsTask
 */
open class JUnitXMLReportsTaskTest {
    /** 1) Test creating task without subprojects */
    @Test fun testCreateWithoutSubprojects() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<JUnitXMLReportsTask>(JUNIT_XML_REPORTS_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(JUnitXMLReportsTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME) as JUnitXMLReportsTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(TASK_GROUP_GATHERING, task!!.group)
        Assert.assertFalse(task.outputs.upToDateSpec.isEmpty)

        Assert.assertTrue(task.subprojects.isEmpty())
        Assert.assertNull(task.filter)
        Assert.assertNull(task.filterGroovy)

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 2) Test creating task with null values (filterGroovy) */
    @Test(expected = NullPointerException::class)
    fun testCreateWithoutValues1() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()
        project.tasks.register<JUnitXMLReportsTask>(JUNIT_XML_REPORTS_TASK_NAME)

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME)!! as JUnitXMLReportsTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 3) Test creating task with null values (filter) */
    @Test(expected = NullPointerException::class)
    fun testCreateWithoutValues2() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()
        project.tasks.register<JUnitXMLReportsTask>(JUNIT_XML_REPORTS_TASK_NAME) {
            filterGroovy = false
        }

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME)!! as JUnitXMLReportsTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 4) Test creating task with correct values */
    @Test fun testCreateWithValues() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()
        project.tasks.register<JUnitXMLReportsTask>(JUNIT_XML_REPORTS_TASK_NAME) {
            filterGroovy = false
            filter = { _: String -> true} as FilteringFunction
        }

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME)!! as JUnitXMLReportsTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }
}
