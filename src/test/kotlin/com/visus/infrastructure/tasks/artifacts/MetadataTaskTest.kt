/*  MetadataTaskTest.kt
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

import java.io.File
import java.nio.file.Files

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties

import com.visus.infrastructure.tasks.TASK_GROUP_ARTIFACTS
import com.visus.infrastructure.util.FilteringFunction


/**
 *  MetadataTaskTest:
 *  ================
 *
 *  jUnit test cases on MetadataTask
 *
 *  TODO: Maybe evaluate jUnit.json file after each test?
 */
open class MetadataTaskTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectProjectDir = File(buildDir, "${MetadataTaskTest::class.simpleName}_jUnit")


        /** 0) Create temporary directories for tests */
        @BeforeClass
        @JvmStatic fun configureTestsuite() {
            // 1) remove directory if exists
            if (projectProjectDir.exists() && projectProjectDir.isDirectory) {
                Files.walk(projectProjectDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            // 2) create directory
            projectProjectDir.mkdirs()
        }
    }


    /** 1) Test creating task and run actions without all system properties */
    @Test(expected = NullPointerException::class)
    fun testCreateSystemPropertyMissing1() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<MetadataTask>(METADATA_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(MetadataTask::class.java).size)

        val task = project.tasks.findByName(METADATA_TASK_NAME) as MetadataTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(TASK_GROUP_ARTIFACTS, task!!.group)
        Assert.assertNull(task.version)
        Assert.assertNull(task.rc)
        Assert.assertEquals(0, task.subprojects.size)
        Assert.assertNull(task.filter)
        Assert.assertNull(task.filterGroovy)
        Assert.assertEquals(METADATA_FILE_NAME, task.metadataFileName)

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 2) Test creating task and run actions without all but 1 system properties */
    @Test(expected = NullPointerException::class)
    fun testCreateSystemPropertyMissing2() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            Assert.assertEquals(1337, System.getProperty("BUILD_NUMBER").toInt())

            val project = ProjectBuilder.builder().build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME)

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 3) Test creating task and run actions without all but 2 system properties */
    @Test(expected = NullPointerException::class)
    fun testCreateSystemPropertyMissing3() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            Assert.assertEquals("develop", System.getProperty("BRANCH_NAME"))

            val project = ProjectBuilder.builder().build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME)

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 4) Test creating task and run actions without filterGroovy set */
    @Test(expected = NullPointerException::class)
    fun testCreateFilterGroovyMissing() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")
            Assert.assertEquals("abcdef", System.getProperty("COMMIT_HASH"))

            val project = ProjectBuilder.builder().build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME)

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 5) Test creating task and run actions without filter set (filterGroovy = false) */
    @Test(expected = NullPointerException::class)
    fun testCreateFilterMissingFalse() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")

            val project = ProjectBuilder.builder().build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME) {
                filterGroovy = false
            }

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 6) Test creating task and run actions without filter set (filterGroovy = true) */
    @Test(expected = NullPointerException::class)
    fun testCreateFilterMissingTrue() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")

            val project = ProjectBuilder.builder().build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME) {
                filterGroovy = true
            }

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 7) Test creating task with correct data and evaluate correct behaviour */
    @Test fun testCreateEmptySubprojects() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")
            System.setProperty("BUILDSERVER", "BOB-THE-BUILDER")

            val project = ProjectBuilder.builder().withProjectDir(projectProjectDir).build()
            project.tasks.register<MetadataTask>(METADATA_TASK_NAME) {
                filterGroovy = false
                filter = { _: String -> true } as FilteringFunction
            }

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }


    /** 8) Test creating task with subprojects and custom metadata filename */
    @Test fun testCreateWithSubprojects() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")
            System.setProperty("BUILDSERVER", "BOB-THE-BUILDER")

            val project = ProjectBuilder.builder().withProjectDir(projectProjectDir).build()
            @Suppress("UNUSED_VARIABLE")
            val subProject = ProjectBuilder.builder().withParent(project).build()

            project.tasks.register<MetadataTask>(METADATA_TASK_NAME) {
                filterGroovy = false
                filter = { _: String -> true } as FilteringFunction
                metadataFileName = "test.json"
            }

            val task = project.tasks.findByName(METADATA_TASK_NAME)!! as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }
}
