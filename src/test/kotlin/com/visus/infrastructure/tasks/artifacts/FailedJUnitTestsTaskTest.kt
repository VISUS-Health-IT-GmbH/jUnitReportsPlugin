/*  FailedJUnitTestsTaskTest.kt
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
import java.nio.file.Path

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.jUnitReportsPlugin


/**
 *  FailedJUnitTestsTaskTest:
 *  ========================
 *
 *  jUnit test cases on FailedJUnitTestsTask
 *
 *  TODO: Maybe evaluate failed_junit_tests.txt file after each test?
 *  TODO: Add more tests for partly correct HTML files (number of failed / ignored given but no list) with log!
 */
class FailedJUnitTestsTaskTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // test temporary directories
        private val projectProjectDir = File(buildDir, "${FailedJUnitTestsTaskTest::class.simpleName}_jUnit")
        private val projectBuildDir = File(projectProjectDir, "build")
        private val projectJUnitDir = File(projectBuildDir, "jUnit")

        private val projectProjectDir2 = File(buildDir, "${FailedJUnitTestsTaskTest::class.simpleName}_jUnit2")
        private val projectBuildDir2 = File(projectProjectDir2, "build")
        private val projectJUnitDir2 = File(projectBuildDir2, "jUnit")

        // correct HTML file
        private val correctHTML = resource("html/correct.html")
        private val correct2HTML = resource("html/correct2.html")


        /** Simple helper method for resources */
        private fun resource(path: String) : String = this::class.java.classLoader.getResource(path)!!.path
                                                        .replace("%20", " ")


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
            projectJUnitDir.mkdirs()

            projectProjectDir2.mkdirs()
            projectBuildDir2.mkdirs()
            projectJUnitDir2.mkdirs()

            // 3) copy HTML and rename
            Files.copy(Path.of(correctHTML), File(projectJUnitDir, "index.html").toPath())
            Files.copy(Path.of(correct2HTML), File(projectJUnitDir2, "index.html").toPath())
        }
    }


    /** 1) Test task (actual action) where everything is wrong (index.html missing, no project layout, etc.) */
    @Test fun testWrongConditions() {
        val project = ProjectBuilder.builder().build()

        project.tasks.register<FailedJUnitTestsTask>(FAILED_JUNIT_TESTS_TASK_NAME)
        Assert.assertEquals(1, project.tasks.withType(FailedJUnitTestsTask::class.java).size)

        val task = project.tasks.getByName(FAILED_JUNIT_TESTS_TASK_NAME) as FailedJUnitTestsTask?
        Assert.assertNotNull(task)

        // set listener to evaluate output logged by plugin
        project.logging.addStandardOutputListener { message ->
            Assert.assertTrue(
                message.contains(
                    "[${jUnitReportsPlugin::class.simpleName} -> " +
                    "${FailedJUnitTestsTask::createFailedJunitTestsTXT.name} " +
                    "(${FailedJUnitTestsTask::class.simpleName})] Parsing number of failed jUnit tests threw an " +
                    "exception: [jUnitReportsPlugin -> File.parseHTMLFailures] Cannot parse number of failed jUnit " +
                    "tests from file '${project.buildDir}/jUnit/index.html'! See error: " +
                    "${project.buildDir}/jUnit/index.html (Datei oder Verzeichnis nicht gefunden)"
                ) && message.contains(
                    "[${jUnitReportsPlugin::class.simpleName} -> " +
                    "${FailedJUnitTestsTask::createFailedJunitTestsTXT.name} " +
                    "(${FailedJUnitTestsTask::class.simpleName})] This is not a critical issue but disturbs the " +
                    "developer experience for quality assurance!"
                )
            )
        }

        // emulate running task action when task is called
        task!!.actions.forEach {
            it.execute(task)
        }
    }


    /** 2) Test task (actual action) with correct HTML and specific project directory */
    @Test fun testCorrectHTMLDefaultValue() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir).build()
        project.tasks.register<FailedJUnitTestsTask>(FAILED_JUNIT_TESTS_TASK_NAME)

        // emulate running task action when task is called
        val task = project.tasks.getByName(FAILED_JUNIT_TESTS_TASK_NAME) as FailedJUnitTestsTask
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 3) Test task (actual action) with correct HTML and specific project directory (specific output file name) */
    @Test fun testCorrectHTMLSpecialValue() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir).build()
        project.tasks.register<FailedJUnitTestsTask>(FAILED_JUNIT_TESTS_TASK_NAME) {
            failedJUnitTestsFileName = "failed.txt"
        }

        // emulate running task action when task is called
        val task = project.tasks.getByName(FAILED_JUNIT_TESTS_TASK_NAME) as FailedJUnitTestsTask
        task.failedJUnitTestsFileName = "failed.txt"
        task.actions.forEach {
            it.execute(task)
        }
    }


    /** 4) Test task (actual action) with partly correct HTML and specific project directory */
    @Test fun testPartlyCorrectHTML() {
        val project = ProjectBuilder.builder().withProjectDir(projectProjectDir2).build()
        project.tasks.register<FailedJUnitTestsTask>(FAILED_JUNIT_TESTS_TASK_NAME)

        // emulate running task action when task is called
        val task = project.tasks.getByName(FAILED_JUNIT_TESTS_TASK_NAME) as FailedJUnitTestsTask
        task.failedJUnitTestsFileName = FAILED_JUNIT_TESTS_FILE_NAME
        task.actions.forEach {
            it.execute(task)
        }
    }
}
