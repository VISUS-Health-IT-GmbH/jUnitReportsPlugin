/*  PropertiesExtension.kt
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

import java.util.Properties

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

import com.visus.infrastructure.exception.jUnitReportsPluginException


/**
 *  Function to retrieve the property value behind the provided property name
 *
 *  @param propertyName the name of the property in configuration properties file
 *  @param notGivenException exception which should be thrown when no property provided with given name
 *  @return property value string
 *  @throws jUnitReportsPluginException and its subclasses
 */
@Throws(jUnitReportsPluginException::class)
internal fun <T: jUnitReportsPluginException> Properties.getPropertyElement(propertyName: String,
                                                                            notGivenException: KClass<T>) : String {
    val notGivenMessage = "[Properties.getPropertyElement] No value for property '${propertyName}' given!"

    return this[propertyName]?.let {
        return with (it as String) {
            if (this.isBlank() || this == propertyName) {
                throw notGivenException.primaryConstructor!!.call(notGivenMessage)
            }

            this
        }
    } ?: run {
        throw notGivenException.primaryConstructor!!.call(notGivenMessage)
    }
}
