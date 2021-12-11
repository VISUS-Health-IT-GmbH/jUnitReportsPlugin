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

import org.gradle.api.Project

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.NoPropertiesFileProvidedException
import com.visus.infrastructure.exception.NoPropertiesProvidedException


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
    return System.getenv(key) ?: run {
        when {
            System.getProperties().containsKey(key)         -> System.getProperty(key)
            this.properties.containsKey(key)                -> this.properties[key]
            else                                            -> null
        }
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
