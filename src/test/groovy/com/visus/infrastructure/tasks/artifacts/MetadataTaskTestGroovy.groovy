/*  MetadataTaskTestGroovy.groovy
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

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties

import org.junit.Test

import org.gradle.testfixtures.ProjectBuilder


/**
 *  jUnit test cases on MetadataTaskTest but with Groovy
 */
class MetadataTaskTestGroovy {
    /** 1) Test creating task with subprojects and custom metadata filename */
    @Test void testCreateWithSubprojects() {
        restoreSystemProperties {
            System.setProperty("BUILD_NUMBER", "1337")
            System.setProperty("BRANCH_NAME", "develop")
            System.setProperty("COMMIT_HASH", "abcdef")
            System.setProperty("BUILDSERVER", "BOB-THE-BUILDER")

            def project = ProjectBuilder.builder().build()
            @SuppressWarnings("UNUSED_VARIABLE")
            def subProject = ProjectBuilder.builder().withParent(project).build()

            project.tasks.register(MetadataTaskKt.METADATA_TASK_NAME, MetadataTask) {
                it.filterGroovy = true
                it.filter = { String projectName -> true }
            }

            def task = project.tasks.findByName(MetadataTaskKt.METADATA_TASK_NAME) as MetadataTask

            // emulate running task action when task is called
            task.actions.forEach {
                it.execute(task)
            }
        }
    }
}
