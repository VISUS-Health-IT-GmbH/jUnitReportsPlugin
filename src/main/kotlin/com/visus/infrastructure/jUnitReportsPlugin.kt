/*  jUnitReportsPlugin.kt
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

import groovy.lang.Closure

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
// SonarLint false-positive: kotlin:S1128
import org.gradle.kotlin.dsl.register

import com.visus.infrastructure.exception.PluginWrongAppliedException
import com.visus.infrastructure.exception.JavaPluginMissingException
import com.visus.infrastructure.exception.FilteringFunctionNotGivenException
import com.visus.infrastructure.exception.FilteringFunctionNotFoundException
import com.visus.infrastructure.exception.ProductVersionNotGivenException
import com.visus.infrastructure.exception.ProductVersionNotFoundException
import com.visus.infrastructure.exception.ProductRCNotGivenException
import com.visus.infrastructure.exception.ProductRCNotFoundException
import com.visus.infrastructure.exception.ProductVersionIsPatchNotGivenException
import com.visus.infrastructure.exception.ProductVersionIsPatchNotFoundException
import com.visus.infrastructure.exception.EndpointRESTNotGivenException
import com.visus.infrastructure.exception.EndpointDefaultTemplateNotGivenException
import com.visus.infrastructure.exception.EndpointVersionTemplateNotGivenException
import com.visus.infrastructure.exception.EndpointPatchTemplateNotGivenException

import com.visus.infrastructure.extension.getProjectExtraPropertyElement
import com.visus.infrastructure.extension.getPropertyElement
import com.visus.infrastructure.extension.hasActualJUnitTestcases
import com.visus.infrastructure.extension.readProperties
import com.visus.infrastructure.extension.readPropertiesFromFile

import com.visus.infrastructure.tasks.CLEAN_ARTIFACT_TASK_NAME
import com.visus.infrastructure.tasks.CleanArtifactsTask

import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.FAILED_JUNIT_TESTS_TASK_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.METADATA_TASK_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_FILE_NAME
import com.visus.infrastructure.tasks.artifacts.RESULTS_ARCHIVE_TASK_NAME
import com.visus.infrastructure.tasks.artifacts.FailedJUnitTestsTask
import com.visus.infrastructure.tasks.artifacts.MetadataTask
import com.visus.infrastructure.tasks.artifacts.ResultsArchiveTask

import com.visus.infrastructure.tasks.combining.JUNIT_HTML_RESULTS_TASK_NAME
import com.visus.infrastructure.tasks.combining.JUNIT_XML_RESULTS_TASK_NAME
import com.visus.infrastructure.tasks.combining.JUnitHTMLResultsTask
import com.visus.infrastructure.tasks.combining.JUnitXMLResultsTask

import com.visus.infrastructure.tasks.gathering.JUNIT_HTML_REPORTS_TASK_NAME
import com.visus.infrastructure.tasks.gathering.JUNIT_XML_REPORTS_TASK_NAME
import com.visus.infrastructure.tasks.gathering.JUnitHTMLReportsTask
import com.visus.infrastructure.tasks.gathering.JUnitXMLReportsTask

import com.visus.infrastructure.tasks.publishing.JUNIT_REST_SEND_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUNIT_RC_SAVE_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUNIT_FINAL_TASK_NAME
import com.visus.infrastructure.tasks.publishing.JUnitRESTSendTask
import com.visus.infrastructure.tasks.publishing.JUnitRCSaveTask
import com.visus.infrastructure.tasks.publishing.JUnitFinalTask

import com.visus.infrastructure.util.FilteringFunction
import com.visus.infrastructure.util.resolvePrefix


/**
 *  jUnitReportsPlugin:
 *  ==================
 *
 *  @author Tobias Hahnen
 *
 *  Plugin adding reporting & validation logic to a Gradle project. Can only be applied to root project!
 */
@Suppress("kotlin:S101", "ClassNaming")
open class jUnitReportsPlugin : Plugin<Project> {
    companion object {
        // identifiers of the properties needed by this plugin
        internal const val KEY_PATH                             = "plugins.junitreporting.properties.path"
        internal const val KEY_ALTERNATEPATH                    = "plugins.junitreporting.properties.alternatePath"
        internal const val KEY_ISPRODUCTIONSYSTEM               = "plugins.junitreporting.properties.isProductionSystem"

        // property names
        internal const val filteringFunctionPropertyName        = "product.filter"
        internal const val productVersionPropertyName           = "product.version"
        internal const val productRCPropertyName                = "product.rc"
        internal const val productVersionIsPatch                = "product.patch"
        internal const val endpointRESTPropertyName             = "endpoint.rest"
        internal const val endpointDefaultTemplatePropertyName  = "endpoint.rc.default.template"
        internal const val endpointVersionTemplatePropertyName  = "endpoint.rc.version.template"
        internal const val endpointPatchTemplatePropertyName    = "endpoint.rc.patch.template"
    }


    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        // 1) check if project is root project
        when {
            target.rootProject != target -> throw PluginWrongAppliedException(
                "[${this::class.simpleName}] This plugin was applied to a subproject but can only applied to a root " +
                "project to work correctly!"
            )
        }

        // 2) check if Java plugin is applied
        when {
            !target.plugins.hasPlugin(JavaPlugin::class.java) -> throw JavaPluginMissingException(
                "[${this::class.simpleName}] This plugin was applied to a root project missing the Java plugin which " +
                "is necessary to work correctly!"
            )
        }

        // 3) read properties (including key / values) from project's "gradle.properties" file + resolve prefix
        var properties = target.readProperties(listOf(KEY_PATH, KEY_ALTERNATEPATH, KEY_ISPRODUCTIONSYSTEM))
        val prefix : String = resolvePrefix(properties, KEY_ISPRODUCTIONSYSTEM)

        // 4) read values from given file(s)
        properties = target.readPropertiesFromFile(
            properties, listOf(KEY_ALTERNATEPATH, KEY_PATH)
        )

        // 5) get filtering function for later use
        var filteringFunctionGroovy = false
        val filteringFunction: Any = target.getProjectExtraPropertyElement(
            properties, prefix + filteringFunctionPropertyName,
            FilteringFunctionNotGivenException::class, FilteringFunctionNotFoundException::class
        )
        try {
            @Suppress("UNUSED_VARIABLE")
            val testFunction = filteringFunction as Closure<*>
            filteringFunctionGroovy = true
        } catch (ignored: Exception) { /* NOSONAR */ }

        // 6) get product version for later use
        val productVersion = target.getProjectExtraPropertyElement(
            properties, prefix + productVersionPropertyName,
            ProductVersionNotGivenException::class, ProductVersionNotFoundException::class
        ) as String

        // 7) get product release candidate for later use
        val productRC = target.getProjectExtraPropertyElement(
            properties, prefix + productRCPropertyName,
            ProductRCNotGivenException::class, ProductRCNotFoundException::class
        ) as String

        // 8) get product version is patch for saving to network share
        val productVersionIsPatch = target.getProjectExtraPropertyElement(
            properties, prefix + productVersionIsPatch,
            ProductVersionIsPatchNotGivenException::class, ProductVersionIsPatchNotFoundException::class
        ).toString().toBoolean()

        // 9) get REST API endpoint of jUnit backend
        val endpointREST = properties.getPropertyElement(
            prefix + endpointRESTPropertyName, EndpointRESTNotGivenException::class
        )

        // 10) get default endpoint template for saving RC results to network share
        val endpointDefaultTemplate = properties.getPropertyElement(
            prefix + endpointDefaultTemplatePropertyName, EndpointDefaultTemplateNotGivenException::class
        )

        // 11) get version endpoint template for saving RC results to network share
        val endpointVersionTemplate = properties.getPropertyElement(
            prefix + endpointVersionTemplatePropertyName, EndpointVersionTemplateNotGivenException::class
        )

        // 12) get patch endpoint template for saving RC results to network share
        val endpointPatchTemplate = properties.getPropertyElement(
            prefix + endpointPatchTemplatePropertyName, EndpointPatchTemplateNotGivenException::class
        )

        // 13) extend "clean" Task of root project
        target.tasks.getByName("clean").dependsOn(
            target.tasks.register<CleanArtifactsTask>(CLEAN_ARTIFACT_TASK_NAME)
        )

        // 14) configure filtered subproject
        val filteredSubprojects = target.subprojects.filter {
            when(filteringFunctionGroovy) {
                true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
                false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
            }
        }.filter {
            // INFO: On projects not yet filtered out: Try to predict if actual jUnit test cases were found in the files
            //       and if none found, exclude the project as well!
            //       -> When no source sets are available, skip the check
            it.hasActualJUnitTestcases()
        }.toSet()

        filteredSubprojects.forEach { prj ->
            // combineJUnitHTMLResults & combineJUnitXMLResults
            prj.tasks.register<JUnitHTMLResultsTask>(JUNIT_HTML_RESULTS_TASK_NAME)
            prj.tasks.register<JUnitXMLResultsTask>(JUNIT_XML_RESULTS_TASK_NAME) {
                dependsOn(JUNIT_HTML_RESULTS_TASK_NAME)
            }
        }

        // 15) configure root project (available everywhere)
        // gatherJUnitHTMLReports & gatherJUnitXMLReports & createJUnitResultsArchive
        target.tasks.register<JUnitHTMLReportsTask>(
            JUNIT_HTML_REPORTS_TASK_NAME, filteredSubprojects
        )

        target.tasks.register<JUnitXMLReportsTask>(
            JUNIT_XML_REPORTS_TASK_NAME, filteredSubprojects
        ).configure {
            // Gathering XML reports from subprojects depends on gathering HTML reports and combining in subprojects
            dependsOn(JUNIT_HTML_REPORTS_TASK_NAME)
            dependsOn(
                filteredSubprojects.map {
                    it.tasks.getByName(JUNIT_XML_RESULTS_TASK_NAME)
                }
            )
        }

        target.tasks.register<ResultsArchiveTask>(RESULTS_ARCHIVE_TASK_NAME) {
            // Creating ZIP archive depends on gathering XML files
            dependsOn(JUNIT_XML_REPORTS_TASK_NAME)
        }

        target.tasks.register<FailedJUnitTestsTask>(FAILED_JUNIT_TESTS_TASK_NAME) {
            // Creating "failed_junit_tests.txt" depends on creating ZIP archive
            dependsOn(RESULTS_ARCHIVE_TASK_NAME)
        }

        // 16) configure root project (only on build server)
        if (target.providers.systemProperty("BUILDSERVER").forUseAtConfigurationTime().isPresent) {
            // createJUnitMetadataFile & publishJUnitNormal & publishJUnitRC & publishJUnitResults
            target.tasks.register<MetadataTask>(
                METADATA_TASK_NAME, filteredSubprojects
            ).configure {
                // Creating "jUnit.json" depends on creating "failed_junit_tests.txt"
                dependsOn(FAILED_JUNIT_TESTS_TASK_NAME)

                // Necessary inputs
                version = productVersion
                rc = productRC
            }

            target.tasks.register<JUnitRESTSendTask>(
                JUNIT_REST_SEND_TASK_NAME, endpointREST,
                FAILED_JUNIT_TESTS_FILE_NAME, METADATA_FILE_NAME, RESULTS_ARCHIVE_FILE_NAME
            ).configure {
                // Sending jUnit results to REST API depends on creating metadata file
                dependsOn(METADATA_TASK_NAME)
            }

            target.tasks.register<JUnitRCSaveTask>(JUNIT_RC_SAVE_TASK_NAME) {
                // Saving RC results to file endpoint depends on creating metadata file
                dependsOn(METADATA_TASK_NAME)

                // Only run task if RC is actual release candidate
                onlyIf { productRC.startsWith("RC") && !productRC.endsWith("_build") }

                // Necessary inputs
                version = productVersion
                rc = productRC
                this.productVersionIsPatch = productVersionIsPatch
                this.endpointDefaultTemplate = endpointDefaultTemplate
                this.endpointVersionTemplate = endpointVersionTemplate
                this.endpointPatchTemplate = endpointPatchTemplate
            }

            target.tasks.register<JUnitFinalTask>(JUNIT_FINAL_TASK_NAME)
        }
    }
}
