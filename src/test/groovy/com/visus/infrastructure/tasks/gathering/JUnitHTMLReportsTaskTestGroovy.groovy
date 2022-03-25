/*  JUnitHTMLReportsTaskTestGroovy.groovy
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
 *  jUnit test cases on JUnitHTMLReportsTask but with Groovy
 */
class JUnitHTMLReportsTaskTestGroovy {
    @Test void testCreateCorrectParametersGroovy() {
        def project = ProjectBuilder.builder().build()
        @SuppressWarnings("UNUSED_VARIABLE")
        def subProject = ProjectBuilder.builder().withParent(project).build()

        project.tasks.register(
            JUnitHTMLReportsTaskKt.JUNIT_HTML_REPORTS_TASK_NAME, JUnitHTMLReportsTask,
            { String projectName -> true}, true
        )
        Assert.assertEquals(1, project.tasks.withType(JUnitHTMLReportsTask).size())
    }
}
