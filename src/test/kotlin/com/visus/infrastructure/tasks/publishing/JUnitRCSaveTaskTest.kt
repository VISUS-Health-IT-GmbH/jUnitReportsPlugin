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

import java.io.File
import java.nio.file.Files

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties

import com.visus.infrastructure.tasks.TASK_GROUP_PUBLISHING
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME


/**
 *  JUnitRCSaveTaskTest:
 *  ===================
 *
 *  jUnit test cases on JUnitRCSaveTask
 */
open class JUnitRCSaveTaskTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectDir = File(buildDir, "${JUnitRCSaveTaskTest::class.simpleName}_jUnit")
        private val metadataFile = File(projectDir, "metadata.json")
        private val zipFile = File(projectDir, "jUnit.zip")
        private val failedJUnitTestsFile = File(projectDir, "failed_junit_tests.txt")

        private val projectDir2 = File(buildDir, "${JUnitRCSaveTaskTest::class.simpleName}_jUnit2")
        private val metadataFile2 = File(projectDir2, "metadata.json")
        private val zipFile2 = File(projectDir2, "jUnit.zip")
        private val failedJUnitTestsFile2 = File(projectDir2, "failed_junit_tests.txt")

        private val projectDir3 = File(buildDir, "${JUnitRCSaveTaskTest::class.simpleName}_jUnit3")
        private val metadataFile3 = File(projectDir3, "metadata.json")
        private val zipFile3 = File(projectDir3, "jUnit.zip")
        private val failedJUnitTestsFile3 = File(projectDir3, "failed_junit_tests.txt")


        /** 0) Create temporary directories for tests */
        @BeforeClass @JvmStatic fun configureTestsuite() {
            // 1) remove directory if exists
            if (projectDir.exists() && projectDir.isDirectory) {
                Files.walk(projectDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            if (projectDir2.exists() && projectDir2.isDirectory) {
                Files.walk(projectDir2.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            if (projectDir3.exists() && projectDir3.isDirectory) {
                Files.walk(projectDir3.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            // 2) create directories
            projectDir.mkdirs()
            metadataFile.createNewFile()
            zipFile.createNewFile()
            failedJUnitTestsFile.createNewFile()

            projectDir2.mkdirs()
            metadataFile2.createNewFile()
            zipFile2.createNewFile()

            projectDir3.mkdirs()
            metadataFile3.createNewFile()
            zipFile3.createNewFile()
        }
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


    /** 5) Test running task wih everything added (patch template) */
    @Test fun testRunTaskPatchTemplate() {
        restoreSystemProperties {
            System.setProperty("BRANCH_NAME", "release/jUnitReportsPlugin/4.2")
            System.setProperty("BUILD_NUMBER", "1337")

            val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
            project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
                productVersionIsPatch = true
                endpointPatchTemplate = "${projectDir.absolutePath}/{VERSION_ABx}/{VERSION_ABCx}/{VERSION}-{RC}"
                metadataFileName = metadataFile.name
                zipFileName = zipFile.name
                failedJUnitTestsFileName = failedJUnitTestsFile.name
                version = "4.2"
                rc = "RC01"
            }

            val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 6) Test running task wih everything added (version template) */
    @Test fun testRunTaskVersionTemplate() {
        restoreSystemProperties {
            System.setProperty("BRANCH_NAME", "release/jUnitReportsPlugin/4.2")
            System.setProperty("BUILD_NUMBER", "1337")

            File("${projectDir2.absolutePath}/VERSION").mkdirs()

            val project = ProjectBuilder.builder().withProjectDir(projectDir2).build()
            project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
                productVersionIsPatch = false
                endpointVersionTemplate = "${projectDir2.absolutePath}/VERSION"
                metadataFileName = metadataFile2.name
                zipFileName = zipFile2.name
                failedJUnitTestsFileName = failedJUnitTestsFile2.name
                version = "4.2"
                rc = "RC01"
            }

            val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 7) Test running task wih everything added (default template) */
    @Test fun testRunTaskDefaultTemplate() {
        restoreSystemProperties {
            System.setProperty("BRANCH_NAME", "release/jUnitReportsPlugin/4.2")
            System.setProperty("BUILD_NUMBER", "1337")

            File("${projectDir2.absolutePath}/VERSION").mkdirs()

            val project = ProjectBuilder.builder().withProjectDir(projectDir3).build()
            project.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
                endpointDefaultTemplate = "${projectDir3.absolutePath}/{BRANCH}/{BUILDID}"
                metadataFileName = metadataFile3.name
                zipFileName = zipFile3.name
                failedJUnitTestsFileName = failedJUnitTestsFile3.name
                version = "4.2"
                rc = "RC01"
            }

            val task = project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME)!! as JUnitRCSaveTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }
}
