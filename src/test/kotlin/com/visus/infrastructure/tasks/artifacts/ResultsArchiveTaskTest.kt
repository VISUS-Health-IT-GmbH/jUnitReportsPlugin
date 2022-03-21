/*  ResultsArchiveTaskTest.kt
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

import org.junit.Assert
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_ARTIFACTS


/**
 *  ResultsArchiveTaskTest:
 *  ======================
 *
 *  jUnit test cases on ResultsArchiveTask
 */
open class ResultsArchiveTaskTest {
    companion object {
        // test values used throughout this test class
        internal const val testZipFileName = "testArchive.zip"
    }


    /** 1) test creating task with default value */
    @Test fun testCreateDefaultValue() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<ResultsArchiveTask>(RESULTS_ARCHIVE_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(ResultsArchiveTask::class.java).size)

        val task = project.tasks.findByName(RESULTS_ARCHIVE_TASK_NAME) as ResultsArchiveTask?
        Assert.assertNotNull(task)
        Assert.assertEquals(TASK_GROUP_ARTIFACTS, task!!.group)
        Assert.assertEquals(RESULTS_ARCHIVE_FILE_NAME, task.archiveFileName.get())
        Assert.assertEquals(project.projectDir, task.destinationDirectory.get().asFile)
    }


    /** 2) test creating task with special value */
    @Test fun testCreateSpecialValue() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<ResultsArchiveTask>(RESULTS_ARCHIVE_TASK_NAME) {
            archiveFileName.set(testZipFileName)
        }
        Assert.assertEquals(1, project.tasks.withType(ResultsArchiveTask::class.java).size)

        val task = project.tasks.findByName(RESULTS_ARCHIVE_TASK_NAME) as ResultsArchiveTask?
        Assert.assertNotNull(task)
        Assert.assertEquals(testZipFileName, task!!.archiveFileName.get())
    }
}
