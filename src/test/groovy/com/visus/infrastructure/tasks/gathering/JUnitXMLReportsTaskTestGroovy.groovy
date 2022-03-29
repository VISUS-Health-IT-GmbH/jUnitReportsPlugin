/*  JUnitXMLReportsTaskTestGroovy.groovy
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

import org.gradle.testfixtures.ProjectBuilder


/**
 *  jUnit test cases on JUnitXMLReportsTask but with Groovy
 */
class JUnitXMLReportsTaskTestGroovy {
    @Test void testCreateWithValues() {
        def project = ProjectBuilder.builder().build()
        @SuppressWarnings("GroovyUnusedDeclaration")
        def subProject = ProjectBuilder.builder().withParent(project).build()

        project.tasks.register(JUnitXMLReportsTaskKt.JUNIT_XML_REPORTS_TASK_NAME, JUnitXMLReportsTask) {
            it.filterGroovy = true
            it.filter = { String projectName -> true}
        }

        def task = project.tasks.findByName(JUnitXMLReportsTaskKt.JUNIT_XML_REPORTS_TASK_NAME) as JUnitXMLReportsTask

        // emulate running task action when task is called
        task.actions.each {
            it.execute(task)
        }
    }
}
