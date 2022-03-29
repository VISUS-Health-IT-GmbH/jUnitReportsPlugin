/*  JUnitXMLResultsTaskTest.kt
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

import java.io.File
import java.nio.file.Files

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.TASK_GROUP_COMBINING
import com.visus.infrastructure.tasks.gathering.JUNIT_XML_REPORTS_TASK_NAME


/**
 *  JUnitXMLResultsTaskTest:
 *  =======================
 *
 *  jUnit test cases on JUnitXMLResultsTask
 */
open class JUnitXMLResultsTaskTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectProjectDir = File(buildDir, "${JUnitXMLResultsTaskTest::class.simpleName}_jUnit")
        private val projectBuildDir = File(projectProjectDir, "build")
        private val projectTestResultsDir = File(projectBuildDir, "test-results")
        private val projectJUnitDir = File(projectBuildDir, "jUnit")
        private val projectXMLResultsDir = File(projectJUnitDir, "xmlresults")

        private val projectProjectDir2 = File(buildDir, "${JUnitXMLResultsTaskTest::class.simpleName}_jUnit2")
        private val projectBuildDir2 = File(projectProjectDir2, "build")
        private val projectTestResultsDir2 = File(projectBuildDir2, "test-results")
        private val projectJUnitDir2 = File(projectBuildDir2, "jUnit")
        private val projectXMLResultsDir2 = File(projectJUnitDir2, "xmlresults")


        /** 0) Create temporary directories for tests */
        @BeforeClass @JvmStatic fun configureTestsuite() {
            // 1) remove directory if exists
            if (projectProjectDir.exists() && projectProjectDir.isDirectory) {
                Files.walk(projectProjectDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            if (projectProjectDir2.exists() && projectProjectDir2.isDirectory) {
                Files.walk(projectProjectDir2.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map { it.toFile() }
                    .forEach { it.delete() }
            }

            // 2) create directories
            projectProjectDir.mkdirs()
            projectBuildDir.mkdirs()
            projectTestResultsDir.mkdirs()
            projectJUnitDir.mkdirs()
            projectXMLResultsDir.mkdirs()

            projectProjectDir2.mkdirs()
            projectBuildDir2.mkdirs()
            projectTestResultsDir2.mkdirs()
            projectJUnitDir2.mkdirs()
            projectXMLResultsDir2.mkdirs()
        }
    }


    /** 1) Test creating task without any values */
    @Test fun testCreateNoValues() {
        val testFolder = File(projectTestResultsDir, "testFolder")
        testFolder.mkdirs()

        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir).build()

        project.tasks.register<JUnitXMLResultsTask>(JUNIT_XML_REPORTS_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(JUnitXMLResultsTask::class.java).size)

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME) as JUnitXMLResultsTask?
        Assert.assertNotNull(task)

        Assert.assertEquals(TASK_GROUP_COMBINING, task!!.group)
        Assert.assertFalse(task.outputs.upToDateSpec.isEmpty)

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 2) Test creating task with test values */
    @Test fun teCreateWithValues() {
        val testFolder = File(projectTestResultsDir2, "testFolder")
        val testFile = File(testFolder, "testFile")
        testFolder.mkdirs()
        testFile.createNewFile()

        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir2).build()
        project.tasks.register<JUnitXMLResultsTask>(JUNIT_XML_REPORTS_TASK_NAME)

        val task = project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME)!! as JUnitXMLResultsTask

        // emulate running task action when task is called
        task.actions.forEach {
            it.execute(task)
        }
    }
}
