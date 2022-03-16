/*  ProjectExtension.kt
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
package com.visus.infrastructure.extension

import java.io.FileInputStream
import java.util.Properties

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.NoPropertiesProvidedException
import com.visus.infrastructure.exception.NoPropertiesFileProvidedException
import com.visus.infrastructure.exception.jUnitReportsPluginException


/**
 *  Reads the project properties which are used for configuration regarding the properties used by this plugin!
 *
 *  @param keys set of necessary properties
 *  @return the specific properties key-value pairs read from the project properties itself
 *  @throws NoPropertiesProvidedException when necessary keys were not found
 */
@Throws(NoPropertiesProvidedException::class)
internal fun Project.readProperties(keys: List<String>) : Properties {
    val properties = Properties()

    keys.forEach {
        this.resolvePropertyKey(it)?.let { value ->
            properties[it] = value
        }
    }

    when (properties.size) {
        0 -> throw NoPropertiesProvidedException(
            "[${jUnitReportsPlugin::class.simpleName} -> Project.readProperties] The necessary properties " +
            "[${keys.joinToString(",")}] were not provided to this project!"
        )
    }

    return properties
}


/**
 *  Tries to resolve a property key to find a value in the following order
 *  - system environment variable
 *  - system property
 *  - (root) project property
 *
 *  @param key property key
 *  @return value if found, else null
 */
internal fun Project.resolvePropertyKey(key: String) : Any? {
    return when {
        this.providers.environmentVariable(key).forUseAtConfigurationTime().isPresent
            -> this.providers.environmentVariable(key).forUseAtConfigurationTime().get()
        this.providers.systemProperty(key).forUseAtConfigurationTime().isPresent
            -> this.providers.systemProperty(key).forUseAtConfigurationTime().get()
        this.properties.containsKey(key)    -> this.properties[key]
        else                                -> null
    }
}


/**
 *  Reads a properties file provided key after key
 *
 *  @param properties
 *  @param keys order of property keys probably containing file path
 *  @return properties read from file
 *  @throws NoPropertiesFileProvidedException when file not found using all keys
 */
@Throws(NoPropertiesFileProvidedException::class)
internal fun Project.readPropertiesFromFile(properties: Properties, keys: List<String>) : Properties {
    val newProperties = Properties()

    keys.forEach {
        (properties[it] as String?).tryResolveAbsolutePath(this)?.let { path ->
            newProperties.load(FileInputStream(path))
            return newProperties
        }
    }

    throw NoPropertiesFileProvidedException(
        "[${jUnitReportsPlugin::class.simpleName} -> Project.readPropertiesFromFile] The necessary properties " +
        "[${keys.joinToString(",")}] were not provided to this project!"
    )
}


/**
 *  Function to retrieve the object behind the project extra property given by the provided property name
 *  Fails with specific exceptions provided as well if not found
 *
 *  @param properties the properties containing configuration for this plugin
 *  @param propertyName the name of the property in projects external properties
 *  @param notGivenException exception which should be thrown when no property provided with given name
 *  @param notFoundException exception which should be thrown when no property found with given name
 *  @return object (or function) behind the identifier in projects own extra properties
 *  @throws jUnitReportsPluginException and its subclasses
 */
@Throws(jUnitReportsPluginException::class)
internal fun <T: jUnitReportsPluginException, U: jUnitReportsPluginException> Project.getProjectExtraPropertyElement(
    properties: Properties, propertyName: String, notGivenException: KClass<T>, notFoundException: KClass<U>
) : Any {
    val (part1: String, part2: String) = properties.getPropertyElement(
        propertyName, notGivenException
    ).parsePropertyFunctionName()

    try {
        return (this.extra[part1] as Map<*, *>)[part2]!!
    } catch (@Suppress("TooGenericExceptionCaught") err: Exception) {
        throw notFoundException.primaryConstructor!!.call(
            "[Project.getProjectExtraPropertyElement] No value for property '${propertyName}' found in root projects " +
            "extra properties OR another exception occurred: ${err.message}"
        )
    }
}
