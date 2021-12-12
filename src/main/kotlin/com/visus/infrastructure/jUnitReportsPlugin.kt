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

import java.util.Properties

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

import groovy.lang.Closure

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.extra

import com.visus.infrastructure.exception.*
import com.visus.infrastructure.extension.*
import com.visus.infrastructure.util.FilteringFunction
import com.visus.infrastructure.tasks.*


/**
 *  jUnitReportsPlugin:
 *  ==================
 *
 *  @author Tobias Hahnen
 *
 *  Plugin adding reporting & validation logic to a Gradle project. Can only be applied to root project!
 */
@Suppress("kotlin:S101")
sealed class jUnitReportsPlugin : Plugin<Project> {

    companion object {
        // identifiers of the properties needed by this plugin
        internal const val KEY_PATH                             = "plugins.resultreporting.properties.path"
        internal const val KEY_ALTERNATEPATH                    = "plugins.resultreporting.properties.alternatePath"
        internal const val KEY_ISPRODUCTIONSYSTEM               = "plugins.resultreporting.properties.isProductionSystem"

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

        // 3) read properties (including key / values) from project's "gradle.properties" file
        var properties = target.readProperties(
            listOf(KEY_PATH, KEY_ALTERNATEPATH, KEY_ISPRODUCTIONSYSTEM)
        )

        // 4) determine if different separate test / production system
        val prefix : String = properties[KEY_ISPRODUCTIONSYSTEM]?.let {
            val value : String = it as String
            when {
                value.isBlank() || value == KEY_ISPRODUCTIONSYSTEM  -> ""
                else                                                -> value.toBoolean() t "PRODUCTIVE." ?: "TESTING."
            }
        } ?: ""

        // 5) read values from given file(s)
        properties = target.readPropertiesFromFile(
            properties, listOf(KEY_ALTERNATEPATH, KEY_PATH)
        )

        // 6) get filtering function for later use
        var filteringFunctionGroovy = false
        val filteringFunction: Any = getProjectExtraPropertyElement(
            target, properties, prefix + filteringFunctionPropertyName,
            FilteringFunctionNotGivenException::class, FilteringFunctionNotFoundException::class
        )
        try {
            @Suppress("UNUSED_VARIABLE")
            val testFunction = filteringFunction as Closure<*>
            filteringFunctionGroovy = true
        } catch (ignored: Exception) {}

        // 7) get product version for later use
        val productVersion = getProjectExtraPropertyElement(
            target, properties, prefix + productVersionPropertyName,
            ProductVersionNotGivenException::class, ProductVersionNotFoundException::class
        ) as String

        // 8) get product release candidate for later use
        val productRC = getProjectExtraPropertyElement(
            target, properties, prefix + productRCPropertyName,
            ProductRCNotGivenException::class, ProductRCNotFoundException::class
        ) as String

        // 9) get product version is patch for saving to network share
        var productVersionIsPatch = getProjectExtraPropertyElement(
            target, properties, prefix + productVersionIsPatch,
            ProductVersionIsPatchNotGivenException::class, ProductVersionIsPatchNotFoundException::class
        )
        productVersionIsPatch = when (productVersionIsPatch) {
            is String   -> productVersionIsPatch.toBoolean()
            else        -> productVersionIsPatch.toString().toBoolean()
        }

        // 10) get REST API endpoint of jUnit backend
        val endpointREST = getPropertyElement(
            properties, prefix + endpointRESTPropertyName, EndpointRESTNotGivenException::class
        )

        // 11) get default endpoint template for saving RC results to network share
        val endpointDefaultTemplate = getPropertyElement(
            properties, prefix + endpointDefaultTemplatePropertyName, EndpointDefaultTemplateNotGivenException::class
        )

        // 12) get version endpoint template for saving RC results to network share
        val endpointVersionTemplate = getPropertyElement(
            properties, prefix + endpointVersionTemplatePropertyName, EndpointVersionTemplateNotGivenException::class
        )

        // 13) get patch endpoint template for saving RC results to network share
        val endpointPatchTemplate = getPropertyElement(
            properties, prefix + endpointPatchTemplatePropertyName, EndpointPatchTemplateNotGivenException::class
        )

        // 14) extend "clean" Task of root project
        target.tasks.getByName("clean") {
            doFirst {
                target.delete(
                    "${target.projectDir}/jUnit.json",
                    "${target.projectDir}/jUnit.zip",
                    "${target.projectDir}/failed_junit_tests.txt"
                )
            }
        }

        // 15) configure filtered subproject
        target.subprojects.filter {
            when(filteringFunctionGroovy) {
                true    -> (filteringFunction as Closure<*>).call(it.name) as Boolean
                false   -> @Suppress("UNCHECKED_CAST")(filteringFunction as FilteringFunction)(it.name)
            }
        }.forEach { prj ->
            // combineJUnitHTMLResults & combineJUnitXMLResults
            prj.createCombineJUnitHTMLReportsTask("/jUnit")
            prj.createCombineJUnitXMLReportsTask("/jUnit/xmlresults")
        }

        // 16) configure root project (available everywhere)
        // gatherJUnitHTMLReports & gatherJUnitXMLReports & createJUnitResultsArchive
        target.createGatherJUnitHTMLTask("/jUnit", filteringFunction, filteringFunctionGroovy)
        target.createGatherJUnitXMLTask("/jUnit", "/jUnit/projects", filteringFunction, filteringFunctionGroovy)
        target.createCreateJUnitResultsArchiveTask("/jUnit", "jUnit.zip")

        // 17) configure root project (only on build server)
        if (System.getProperties().containsKey("BUILDSERVER")) {
            // createJUnitMetadataFile & publishJUnitNormal & publishJUnitRC & publishJUnitResults
            target.createCreateJUnitMetadataFileTask(
                "/jUnit.json", productVersion, productRC, filteringFunction, filteringFunctionGroovy
            )
            target.createPublishJUnitNormalTask("jUnit.zip", "jUnit.json", endpointREST)
            target.createPublishJUnitRCTask(
                productRC, endpointDefaultTemplate, productVersionIsPatch, endpointPatchTemplate,
                endpointVersionTemplate, productVersion
            )
            target.createPublishJunitResultsTask()
        }
    }


    /**
     *  Function to retrieve the property value behind the provided property name
     *
     *  @param properties the properties containing configuration for this plugin
     *  @param propertyName the name of the property in configuration properties file
     *  @param notGivenException exception which should be thrown when no property provided with given name
     *  @return property value string
     *  @throws jUnitReportsPluginException and its subclasses
     */
    @Throws(jUnitReportsPluginException::class)
    private fun <T: jUnitReportsPluginException> getPropertyElement(properties: Properties, propertyName: String,
                                                                    notGivenException: KClass<T>) : String {
        val notGivenMessage = "[${this::class.simpleName}.getPropertyElement] No value for property " +
                                "'${propertyName}' given!"

        return properties[propertyName]?.let {
            return with (it as String) {
                if (this.isBlank() || this == propertyName) {
                    throw notGivenException.primaryConstructor?.call(notGivenMessage)
                            ?: GetPropertyElementException(notGivenMessage)
                }

                this
            }
        } ?: run {
            throw notGivenException.primaryConstructor?.call(notGivenMessage)
                    ?: GetPropertyElementException(notGivenMessage)
        }
    }


    /**
     *  Function to retrieve the object behind the project extra property given by the provided property name
     *  Fails with specific exceptions provided as well if not found
     *
     *  @param target the project which the plugin is applied to
     *  @param properties the properties containing configuration for this plugin
     *  @param propertyName the name of the property in projects external properties
     *  @param notGivenException exception which should be thrown when no property provided with given name
     *  @param notFoundException exception which should be thrown when no property found with given name
     *  @return object (or function) behind the identifier in projects own extra properties
     *  @throws jUnitReportsPluginException and its subclasses
     */
    @Throws(jUnitReportsPluginException::class)
    private fun <T: jUnitReportsPluginException, U: jUnitReportsPluginException> getProjectExtraPropertyElement(
        target: Project, properties: Properties, propertyName: String,
        notGivenException: KClass<T>, notFoundException: KClass<U>
    ) : Any {
        val property = getPropertyElement(properties, propertyName, notGivenException)

        val (part1: String, part2: String) = property.parsePropertyFunctionName()
        try {
            return (target.extra[part1] as Map<*, *>)[part2]!!
        } catch (err: Exception) {
            val message = "[${this::class.simpleName}.getProjectExtraPropertyElement] No value for property " +
                            "'${propertyName}' found in root projects extra properties OR another exception " +
                            "occurred: ${err.message}"
            throw notFoundException.primaryConstructor?.call(message)
                    ?: GetProjectExtraPropertyElementException(message)
        }
    }
}
