/*  JUnitRCSaveTaskTest.kt
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
package com.visus.infrastructure.tasks.publishing

import org.junit.Assert
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_PUBLISHING
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/**
 *  JUnitRCSaveTaskTest:
 *  ===================
 *
 *  jUnit test cases on JUnitRCSaveTask
 *
 *  TODO: Add more test classes!
 */
open class JUnitRCSaveTaskTest {
    companion object {
        // TODO: Fill with special folders for evaluating copy action!
    }


    /** 1) Test creating task (but not emulating running it) without any values passed */
    @Test fun testCreateWithoutAnything() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(JUnitRCSaveTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME) as JUnitRCSaveTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(TASK_GROUP_PUBLISHING, task!!.group)
        Assert.assertFalse(task.outputs.upToDateSpec.isEmpty)
        Assert.assertNull(task.version)
        Assert.assertNull(task.rc)
        Assert.assertNull(task.productVersionIsPatch)
        Assert.assertNull(task.endpointDefaultTemplate)
        Assert.assertNull(task.endpointVersionTemplate)
        Assert.assertNull(task.endpointPatchTemplate)
        Assert.assertEquals(FAILED_JUNIT_TESTS_FILE_NAME, task.failedJUnitTestsFileName)
        Assert.assertEquals(METADATA_FILE_NAME, task.metadataFileName)
        Assert.assertEquals(RESULTS_ARCHIVE_FILE_NAME, task.zipFileName)
    }


    /** 2) Test running task (emulated) without default template path */
    @Test(expected = NullPointerException::class)
    fun testCreateEndpointDefaultTemplateNull() {
        val project = ProjectBuilder.builder().build()
        project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME)

        val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 3) Test running task (emulated) without version template path */
    @Test(expected = NullPointerException::class)
    fun testCreateEndpointVersionTemplateNull() {
        val project = ProjectBuilder.builder().build()
        project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
            productVersionIsPatch = false
        }

        val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 4) Test running task (emulated) without patch template path */
    @Test(expected = NullPointerException::class)
    fun testCreateEndpointPatchTemplateNull() {
        val project = ProjectBuilder.builder().build()
        project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
            productVersionIsPatch = true
        }

        val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }
}
