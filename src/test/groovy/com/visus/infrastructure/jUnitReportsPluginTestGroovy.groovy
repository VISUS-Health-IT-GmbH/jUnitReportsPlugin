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
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.tasks.CleanArtifactsTask
import com.visus.infrastructure.tasks.CleanArtifactsTaskKt
import com.visus.infrastructure.tasks.combining.JUnitHTMLResultsTaskKt
import com.visus.infrastructure.tasks.combining.JUnitXMLResultsTaskKt
import com.visus.infrastructure.tasks.gathering.JUnitHTMLReportsTaskKt
import com.visus.infrastructure.tasks.gathering.JUnitXMLReportsTaskKt
import com.visus.infrastructure.tasks.artifacts.MetadataTaskKt
import com.visus.infrastructure.tasks.artifacts.FailedJUnitTestsTaskKt


/**
 *  jUnit test cases on jUnitReportsPlugin but with Groovy
 */
class jUnitReportsPluginTestGroovy {
    // path for properties file
    private static String projectPropertiesPath = resource("project/1.properties")
    private static String reportingPropertiesPath = resource("reporting/correct/1.properties")

    // properties object for necessary project properties
    private static Properties projectProperties = new Properties()


    /** Simple helper method for resources */
    private static String resource(String path) {
        return jUnitReportsPluginTestGroovy.class.classLoader.getResource(path).path.replace("%20", " ")
    }


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

        // evaluate subprojects for correctness
        Assert.assertNotNull(subProject.tasks.findByName(JUnitHTMLResultsTaskKt.JUNIT_HTML_RESULTS_TASK_NAME))
        Assert.assertNotNull(subProject.tasks.findByName(JUnitXMLResultsTaskKt.JUNIT_XML_RESULTS_TASK_NAME))

        // evaluate root project for correctness
        Assert.assertNotNull(project.tasks.findByName(JUnitHTMLReportsTaskKt.JUNIT_HTML_REPORTS_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(JUnitXMLReportsTaskKt.JUNIT_XML_REPORTS_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(MetadataTaskKt.METADATA_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(FailedJUnitTestsTaskKt.FAILED_JUNIT_TESTS_TASK_NAME))
    }


    /** 2) Evaluates that "clean" task depends on "cleanJUnitArtifacts" */
    @Test void testEvaluateCleanDependsOn() {
        def project = ProjectBuilder.builder().build()
        @SuppressWarnings("GroovyUnusedDeclaration")
        def ignored = ProjectBuilder.builder().withParent(project).build()

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

        def clean = project.tasks.getByPath("clean")
        def cleanArtifact = project.tasks.getByName(
            CleanArtifactsTaskKt.CLEAN_ARTIFACT_TASK_NAME
        ) as CleanArtifactsTask

        // evaluate cleaning tasks for correctness
        Assert.assertEquals(1, clean.dependsOn.size())
        Assert.assertEquals(cleanArtifact, (clean.dependsOn[0] as TaskProvider).get())
    }
}
