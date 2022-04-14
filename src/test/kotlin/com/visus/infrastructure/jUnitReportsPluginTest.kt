/*  jUnitReportsPluginTest.kt
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

import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

import com.visus.infrastructure.exception.JavaPluginMissingException
import com.visus.infrastructure.exception.PluginWrongAppliedException
import com.visus.infrastructure.exception.FilteringFunctionNotFoundException
import com.visus.infrastructure.exception.FilteringFunctionNotGivenException
import com.visus.infrastructure.exception.ProductVersionNotFoundException
import com.visus.infrastructure.exception.ProductVersionNotGivenException
import com.visus.infrastructure.exception.ProductRCNotFoundException
import com.visus.infrastructure.exception.ProductRCNotGivenException
import com.visus.infrastructure.exception.ProductVersionIsPatchNotFoundException
import com.visus.infrastructure.exception.ProductVersionIsPatchNotGivenException
import com.visus.infrastructure.exception.EndpointRESTNotGivenException
import com.visus.infrastructure.exception.EndpointDefaultTemplateNotGivenException
import com.visus.infrastructure.exception.EndpointVersionTemplateNotGivenException
import com.visus.infrastructure.exception.EndpointPatchTemplateNotGivenException
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_TASK_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_TASK_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_TASK_NAME
import com.visus.infrastructure.tasks.combining.JUNIT_HTML_RESULTS_TASK_NAME
import com.visus.infrastructure.tasks.combining.JUNIT_XML_RESULTS_TASK_NAME
import com.visus.infrastructure.tasks.gathering.JUNIT_HTML_REPORTS_TASK_NAME
import com.visus.infrastructure.tasks.gathering.JUNIT_XML_REPORTS_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUNIT_FINAL_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUNIT_RC_SAVE_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUNIT_REST_SEND_TASK_NAME


/**
 *  jUnitReportsPluginTest:
 *  ======================
 *
 *  jUnit test cases on jUnitReportsPlugin
 */
open class jUnitReportsPluginTest {
    companion object {
        // path to example properties files in "resources" folder
        private val projectPropertiesPath           = resource("project/1.properties")
        private val project2PropertiesPath          = resource("project/2.properties")
        private val project3PropertiesPath          = resource("project/3.properties")
        private val reportingPropertiesPath         = resource("reporting/correct/1.properties")
        private val reporting2PropertiesPath        = resource("reporting/correct/2.properties")
        private val reporting3PropertiesPath        = resource("reporting/correct/3.properties")
        private val reportingWrong1PropertiesPath   = resource("reporting/wrong/1.properties")
        private val reportingWrong2PropertiesPath   = resource("reporting/wrong/2.properties")
        private val reportingWrong3PropertiesPath   = resource("reporting/wrong/3.properties")
        private val reportingWrong4PropertiesPath   = resource("reporting/wrong/4.properties")
        private val reportingWrong5PropertiesPath   = resource("reporting/wrong/5.properties")
        private val reportingWrong6PropertiesPath   = resource("reporting/wrong/6.properties")
        private val reportingWrong7PropertiesPath   = resource("reporting/wrong/7.properties")
        private val reportingWrong8PropertiesPath   = resource("reporting/wrong/8.properties")
        private val reportingWrong9PropertiesPath   = resource("reporting/wrong/9.properties")

        // properties containing file content
        private val projectProperties = Properties()
        private val project2Properties = Properties()
        private val project3Properties = Properties()
        private val reportingProperties = Properties()
        private val reporting2Properties = Properties()
        private val reporting3Properties = Properties()
        private val reportingWrong1Properties = Properties()
        private val reportingWrong2Properties = Properties()
        private val reportingWrong3Properties = Properties()
        private val reportingWrong4Properties = Properties()
        private val reportingWrong5Properties = Properties()
        private val reportingWrong6Properties = Properties()
        private val reportingWrong7Properties = Properties()
        private val reportingWrong8Properties = Properties()
        private val reportingWrong9Properties = Properties()


        /** Simple helper method for resources */
        private fun resource(path: String) : String = this::class.java.classLoader.getResource(path)!!.path.replace(
            "%20", " "
        )


        /** 0) Configuration to read properties once before running multiple tests using them */
        @Throws(IOException::class)
        @BeforeClass fun configureTestsuite() {
            // read "project" properties into local properties object
            projectProperties.load(FileInputStream(projectPropertiesPath))

            // read "project" properties (with "isProductionSystem" set) into local properties object
            project2Properties.load(FileInputStream(project2PropertiesPath))

            // read "project" properties (with "isProductionSystem" set) into local properties object
            project3Properties.load(FileInputStream(project3PropertiesPath))

            // read "reporting" properties into local properties object
            reportingProperties.load(FileInputStream(reportingPropertiesPath))

            // read "reporting" properties (with "isProductionSystem" set) into local properties object
            reporting2Properties.load(FileInputStream(reporting2PropertiesPath))

            // read "reporting" properties (with "isProductionSystem" set) into local properties object
            reporting3Properties.load(FileInputStream(reporting3PropertiesPath))

            // read wrong "reporting" properties into local properties object (wrong 'product.filter')
            reportingWrong1Properties.load(FileInputStream(reportingWrong1PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'product.filter')
            reportingWrong2Properties.load(FileInputStream(reportingWrong2PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'product.version')
            reportingWrong3Properties.load(FileInputStream(reportingWrong3PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'product.rc')
            reportingWrong4Properties.load(FileInputStream(reportingWrong4PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'endpoint.rest')
            reportingWrong5Properties.load(FileInputStream(reportingWrong5PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'endpoint.rc.default.template')
            reportingWrong6Properties.load(FileInputStream(reportingWrong6PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'product.patch')
            reportingWrong7Properties.load(FileInputStream(reportingWrong7PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'endpoint.rc.version.template')
            reportingWrong8Properties.load(FileInputStream(reportingWrong8PropertiesPath))

            // read wrong "reporting" properties into local properties object (missing 'endpoint.rc.patch.template')
            reportingWrong9Properties.load(FileInputStream(reportingWrong9PropertiesPath))
        }
    }


    /** 1) Tests only applying the plugin (without project properties used for configuration) */
    @Test fun testApplyPluginWithoutJavaPlugin() {
        val project = ProjectBuilder.builder().build()

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because of no Java plugin applied
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(JavaPluginMissingException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 2) Tests only applying the plugin - not to root project */
    @Test fun testApplyPluginToNonRootProject() {
        val rootProject = ProjectBuilder.builder().build()
        val project = ProjectBuilder.builder().withParent(rootProject).build()

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because of no Java plugin applied
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(PluginWrongAppliedException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 3) Tests only applying the plugin (with single environment variable used for configuration) */
    @Test fun testApplyPluginWithEnvironmentVariablesToProject() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        withEnvironmentVariable(
            jUnitReportsPlugin.KEY_PATH, reportingPropertiesPath
        ).execute {
            // assert that environment variable is set correctly
            Assert.assertEquals(reportingPropertiesPath, System.getenv(jUnitReportsPlugin.KEY_PATH))

            try {
                // try applying plugin (should fail)
                project.pluginManager.apply(jUnitReportsPlugin::class.java)
            } catch (e: Exception) {
                // assert applying did not work because project extra properties missing
                // INFO: equal to check on InvalidUserDataException as it is based on it
                Assert.assertEquals(FilteringFunctionNotFoundException::class, e.cause!!::class)
            }

            Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
        }
    }


    /** 4) Tests applying the plugin from project.properties (with project extra properties set) */
    @Test fun testApplyPluginExtraPropertiesSet() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        // apply plugin
        project.pluginManager.apply(jUnitReportsPlugin::class.java)

        // assert that plugin is loaded
        Assert.assertTrue(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 5) Tests applying the plugin from project.properties with missing filtering function */
    @Test fun testApplyPluginMissingFilteringFunctionName() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong2PropertiesPath

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because filtering function name is wrong
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(FilteringFunctionNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 6) Tests applying the plugin from project.properties with product version not found */
    @Test fun testApplyPluginProductVersionNotFound() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version_wrongVersionName" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product version is not found
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductVersionNotFoundException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 7) Tests applying the plugin from project.properties with missing product version */
    @Test fun testApplyPluginMissingProductVersion() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong3PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product version not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductVersionNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 8) Tests applying the plugin from project.properties with product rc not found */
    @Test fun testApplyPluginProductRCNotFound() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc_wrongRCName" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product rc is not found
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductRCNotFoundException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 9) Tests applying the plugin from project.properties with missing product rc */
    @Test fun testApplyPluginMissingProductRC() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong4PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product rc not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductRCNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 10) Tests applying the plugin from project.properties with missing product patch info */
    @Test fun testApplyPluginMissingProductPatchInfo() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong7PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product patch information not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductVersionIsPatchNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 11) Tests applying the plugin from project.properties with missing product patch info (extension) */
    @Test fun testApplyPluginMissingProductPatchInfo2() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong8PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because product patch information not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(ProductVersionIsPatchNotFoundException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 12) Tests applying the plugin from project.properties with missing REST endpoint */
    @Test fun testApplyPluginMissingRESTEndpoint() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong5PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because REST endpoint not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(EndpointRESTNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 13) Tests applying the plugin from project.properties with missing default endpoint template */
    @Test fun testApplyPluginMissingDefaultEndpointTemplate() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong6PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because default endpoint template not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(EndpointDefaultTemplateNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 14) Tests applying the plugin from project.properties with missing version endpoint template */
    @Test fun testApplyPluginMissingVersionEndpointTemplate() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong8PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because version endpoint template not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(EndpointVersionTemplateNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 15) Tests applying the plugin from project.properties with missing patch endpoint template */
    @Test fun testApplyPluginMissingPatchEndpointTemplate() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingWrong9PropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        try {
            // try applying plugin (should fail)
            project.pluginManager.apply(jUnitReportsPlugin::class.java)
        } catch (e: Exception) {
            // assert applying did not work because patch endpoint template not given
            // INFO: equal to check on InvalidUserDataException as it is based on it
            Assert.assertEquals(EndpointPatchTemplateNotGivenException::class, e.cause!!::class)
        }

        Assert.assertFalse(project.plugins.hasPlugin(jUnitReportsPlugin::class.java))
    }


    /** 16) Evaluates correctly created subproject tasks */
    @Test fun testEvaluateSubProjectTasks() {
        val project = ProjectBuilder.builder().build()
        val subProject = ProjectBuilder.builder().withParent(project).build()
        project.pluginManager.apply(JavaPlugin::class.java)

        Assert.assertTrue(project.subprojects.size == 1)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        // apply plugin
        project.pluginManager.apply(jUnitReportsPlugin::class.java)

        // check if subproject tasks were created
        Assert.assertNotNull(subProject.tasks.findByName(JUNIT_HTML_RESULTS_TASK_NAME))
        Assert.assertNotNull(subProject.tasks.findByName(JUNIT_XML_RESULTS_TASK_NAME))
    }


    /** 17) Evaluates correctly created root project tasks (no BUILDSERVER environment variable) */
    @Test fun testEvaluateRootProjectTasksNoBuildServer() {
        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()

        project.pluginManager.apply(JavaPlugin::class.java)

        Assert.assertTrue(project.subprojects.size == 1)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        // apply plugin
        project.pluginManager.apply(jUnitReportsPlugin::class.java)

        // check if root project tasks were created
        Assert.assertNotNull(project.tasks.findByName(JUNIT_HTML_REPORTS_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(JUNIT_XML_REPORTS_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(RESULTS_ARCHIVE_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(FAILED_JUNIT_TESTS_TASK_NAME))
    }


    /** 18) Evaluates correctly created root project tasks (using BUILDSERVER environment variable) */
    @Test fun testEvaluateRootProjectTasksBuildServer() {
        System.setProperty("BUILDSERVER", "YESWECAN")
        System.setProperty("BUILD_NUMBER", 1337.toString())
        System.setProperty("BRANCH_NAME", "feature/inf/INFRA-1337")
        System.setProperty("COMMIT_HASH", "abcdef0123456789")

        val project = ProjectBuilder.builder().build()
        @Suppress("UNUSED_VARIABLE")
        val subProject = ProjectBuilder.builder().withParent(project).build()

        project.pluginManager.apply(JavaPlugin::class.java)

        Assert.assertTrue(project.subprojects.size == 1)

        // project properties reference (project.properties.set can not be used directly!)
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)

        // secretly append test properties to project
        projectProperties.forEach {
            propertiesExtension.set(it.key as String, it.value)
        }

        // Must be done this way as the value would change based on the person running this test!
        // INFO: normally the path inside the properties file is static / the same for everybody using it!
        propertiesExtension[jUnitReportsPlugin.KEY_PATH] = reportingPropertiesPath

        /** INFO: False-positive in Kotlin -> lambda arrow is necessary not redundant! */
        propertiesExtension.set(
            "VISUS", mapOf(
                "filterJUnitProjects" to { _: String -> true },
                "version" to "5.3.1",
                "rc" to "RC01",
                "patch" to "true"
            )
        )

        // apply plugin
        project.pluginManager.apply(jUnitReportsPlugin::class.java)

        // check if root project tasks were created
        Assert.assertNotNull(project.tasks.findByName(METADATA_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(JUNIT_REST_SEND_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(JUNIT_RC_SAVE_TASK_NAME))
        Assert.assertNotNull(project.tasks.findByName(JUNIT_FINAL_TASK_NAME))
    }
}
