/*  jUnitReportsPluginException.kt
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
package com.visus.infrastructure.exception

import org.gradle.api.InvalidUserDataException


/**
 *  Base extension for every extension thrown by this plugin
 *
 *  @author Tobias Hahnen
 */
@Suppress("kotlin:S101")
internal open class jUnitReportsPluginException(message: String) : InvalidUserDataException(message)


/**
 *  Exception thrown when plugin is applied to subproject not root project which is wrong!
 */
internal class PluginWrongAppliedException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when no Java plugin applied to root project
 */
internal class JavaPluginMissingException(message: String) : jUnitReportsPluginException(message)


internal class GetPropertyElementException(message: String) : jUnitReportsPluginException(message)


internal class GetProjectExtraPropertyElementException(message: String) : jUnitReportsPluginException(message)






/**
 *  Exception thrown when filtering function not given in properties file provided by the user
 */
internal class FilteringFunctionNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when filtering function given in properties file provided by the user was not found in root
 *  projects extra properties
 */
internal class FilteringFunctionNotFoundException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product version not given in properties file provided by the user
 */
internal class ProductVersionNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product version given in properties file provided by the user was not found in root projects
 *  extra properties
 */
internal class ProductVersionNotFoundException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product release candidate not given in properties file provided by the user
 */
internal class ProductRCNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product release candidate given in properties file provided by the user was not found in root
 *  projects extra properties
 */
internal class ProductRCNotFoundException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product version is patch not given in properties file provided by the user
 */
internal class ProductVersionIsPatchNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when product version is patch given in properties file provided by the user was not found in root
 *  projects extra properties
 */
internal class ProductVersionIsPatchNotFoundException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when REST endpoint not given in properties file provided by the user
 */
internal class EndpointRESTNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when default release candidate endpoint template not given in properties file provided by the user
 */
internal class EndpointDefaultTemplateNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when version release candidate endpoint template not given in properties file provided by the user
 */
internal class EndpointVersionTemplateNotGivenException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when patch release candidate endpoint template not given in properties file provided by the user
 */
internal class EndpointPatchTemplateNotGivenException(message: String) : jUnitReportsPluginException(message)
