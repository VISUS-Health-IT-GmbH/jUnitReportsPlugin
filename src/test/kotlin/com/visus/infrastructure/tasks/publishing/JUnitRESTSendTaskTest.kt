/*  JUnitRESTSendTaskTest.kt
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
 *  JUnitRESTSendTaskTest:
 *  =====================
 *
 *  jUnit test cases on JUnitRESTSendTask
 */
open class JUnitRESTSendTaskTest {
    /** 1) Test creating task with REST endpoint URL missing */
    // TODO: Fix test see JUnitHTMLReportsTaskTest.testCreateWithoutParameters!
    @Test fun testCreateEndpointRESTMissing() {
        val project = ProjectBuilder.builder().build()

        try {
            project.tasks.register<JUnitRESTSendTask>(JUNIT_REST_SEND_TASK_NAME)
        } catch (e: Exception) {
            Assert.assertEquals(NullPointerException::class, e.cause)
        }
    }


    /** 2) Create test using standard parameters */
    @Test fun testCreateDefaultValues() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<JUnitRESTSendTask>(
            JUNIT_REST_SEND_TASK_NAME, "www.google.com",
            FAILED_JUNIT_TESTS_FILE_NAME, METADATA_FILE_NAME, RESULTS_ARCHIVE_FILE_NAME
        )
        Assert.assertEquals(1, project.tasks.withType(JUnitRESTSendTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_REST_SEND_TASK_NAME) as JUnitRESTSendTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(TASK_GROUP_PUBLISHING, task!!.group)
        println(task.commandLine)
        Assert.assertTrue(task.commandLine.contains("\"failed_junit_tests=@$FAILED_JUNIT_TESTS_FILE_NAME\""))
        Assert.assertTrue(task.commandLine.contains("\"metadata_file=@$METADATA_FILE_NAME\""))
        Assert.assertTrue(task.commandLine.contains("\"zip_file=@$RESULTS_ARCHIVE_FILE_NAME\""))
    }


    /** 3) Create test using specialized parameters */
    @Test fun testCreateSpecializedValues() {
        val project = ProjectBuilder.builder().build()

        // INFO: IntelliJ IDEA false-positive
        project.tasks.register<JUnitRESTSendTask>(
            JUNIT_REST_SEND_TASK_NAME, "www.google.com", "failed.txt", "result.json", "result.zip"
        )

        Assert.assertEquals(1, project.tasks.withType(JUnitRESTSendTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_REST_SEND_TASK_NAME) as JUnitRESTSendTask
        println(task.commandLine)
        Assert.assertTrue(task.commandLine.contains("\"failed_junit_tests=@failed.txt\""))
        Assert.assertTrue(task.commandLine.contains("\"metadata_file=@result.json\""))
        Assert.assertTrue(task.commandLine.contains("\"zip_file=@result.zip\""))
        Assert.assertTrue(task.commandLine.contains("www.google.com"))
    }
}
