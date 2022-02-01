/*  jUnitReportsPluginTestGroovy.groovy
 *
 *  Copyright (C) 2021, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.infrastructure

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder

@SuppressWarnings("unused")
import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.tasks.CombineJUnitSubprojectTasksKt
import com.visus.infrastructure.tasks.CreateJUnitTasksKt
import com.visus.infrastructure.tasks.GatherJUnitTasksKt


/**
 *  jUnit test cases on jUnitReportsPlugin but with Groovy
 */
class jUnitReportsPluginTestGroovy {
    // path for properties file
    private static String projectPropertiesPath = jUnitReportsPluginTestGroovy.class.classLoader.getResource(
        "project/1.properties"
    ).path.replace("%20", " ")
    private static String reportingPropertiesPath = jUnitReportsPluginTestGroovy.class.classLoader.getResource(
        "reporting/correct/1.properties"
    ).path.replace("%20", " ")

    // properties object for necessary project properties
    private static Properties projectProperties = new Properties()


    /** 0) Configuration to read properties once before running multiple tests using them */
    @BeforeClass static void configureTestsuite() {
        projectProperties.load(new FileInputStream(projectPropertiesPath))
    }


    /** 1) integration test for Groovy matching:
     *      - jUnitReportsPluginTest.testEvaluateSubProjectTasks
     *      - jUnitReportsPluginTest.testEvaluateRootProjectTasksNoBuildServer
     */
    @Test void testEvaluateProjectTasks() {
        def project = ProjectBuilder.builder().build()
        def subProject = ProjectBuilder.builder().withParent(project).build()

        project.pluginManager.apply(JavaPlugin)

        def propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension)
        projectProperties.each {
            propertiesExtension.set(it.key as String, it.value)
        }

        propertiesExtension.set(jUnitReportsPlugin.KEY_PATH, reportingPropertiesPath)

        propertiesExtension.VISUS = [
            "filterJUnitProjects" : { String projectName -> true },
            "version" : "5.3.1",
            "rc" : "RC01",
            "patch" : true
        ]

        project.pluginManager.apply(jUnitReportsPlugin)

        Assert.assertNotNull(project.tasks.findByName(GatherJUnitTasksKt.GATHER_JUNIT_HTML_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(GatherJUnitTasksKt.GATHER_JUNIT_XML_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(CreateJUnitTasksKt.CREATE_JUNIT_ARCHIVE_TASK_NAME))
        Assert.assertNotNull(
            subProject.tasks.findByName(CombineJUnitSubprojectTasksKt.COMBINE_JUNIT_HTML_SUBPROJECTS_TASK_NAME)
        )
        Assert.assertNotNull(
            subProject.tasks.findByName(CombineJUnitSubprojectTasksKt.COMBINE_JUNIT_XML_SUBPROJECTS_TASK_NAME)
        )
    }
}
