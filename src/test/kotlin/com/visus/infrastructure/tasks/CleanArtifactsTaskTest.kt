/*  CleanArtifactsTaskTest.kt
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
package com.visus.infrastructure.tasks

import org.junit.Assert
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/**
 *  CleanArtifactsTaskTest:
 *  ======================
 *
 *  jUnit test cases on CleanArtifactsTask
 */
class CleanArtifactsTaskTest {
    /** 1) Test creating task and running action using default values */
    @Test fun testCreateDefaultValues() {
        val project = ProjectBuilder.builder().build().also { it.group = "testGroup" }

        project.tasks.register<CleanArtifactsTask>(CLEAN_ARTIFACT_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(CleanArtifactsTask::class.java).size)

        val task = project.tasks.findByName(CLEAN_ARTIFACT_TASK_NAME) as CleanArtifactsTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(FAILED_JUNIT_TESTS_FILE_NAME, task!!.failedJUnitTestsFileName)
        Assert.assertEquals(METADATA_FILE_NAME, task.metadataFileName)
        Assert.assertEquals(RESULTS_ARCHIVE_FILE_NAME, task.zipFileName)

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 2) Test creating task and running action using special values */
    @Test fun testCreateSpecialValues() {
        val project = ProjectBuilder.builder().build()
        project.tasks.register<CleanArtifactsTask>(CLEAN_ARTIFACT_TASK_NAME) {
            failedJUnitTestsFileName = "failed.txt"
            metadataFileName = "data.json"
            zipFileName = "results.zip"
        }

        val task = project.tasks.findByName(CLEAN_ARTIFACT_TASK_NAME) as CleanArtifactsTask
        Assert.assertEquals("failed.txt", task.failedJUnitTestsFileName)
        Assert.assertEquals("data.json", task.metadataFileName)
        Assert.assertEquals("results.zip", task.zipFileName)

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }
}
