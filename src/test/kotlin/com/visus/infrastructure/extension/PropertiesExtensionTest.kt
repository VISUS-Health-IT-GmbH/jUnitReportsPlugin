/*  PropertiesExtensionTest.kt
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

import org.junit.Assert
import org.junit.Test

import com.visus.infrastructure.exception.jUnitReportsPluginException


/**
 *  PropertiesExtensionTest:
 *  =================
 *
 *  jUnit test cases on PropertiesExtension
 */
open class PropertiesExtensionTest {
    companion object {
        // locally used variables in test cases
        private const val propertyKey   = "testKey"
        private const val propertyValue = "testValue"
        private val exception = jUnitReportsPluginException::class
    }


    /** 1) Test if property missing and therefore throws exception */
    @Test(expected = jUnitReportsPluginException::class)
    fun testPropertyMissing() {
        val properties = Properties()
        properties.getPropertyElement(propertyKey, exception)
    }


    /** 2) Test if property blank and therefore throws exception */
    @Test(expected = jUnitReportsPluginException::class)
    fun testPropertyBlank() {
        val properties = Properties()
        properties[propertyKey] = ""
        properties.getPropertyElement(propertyKey, exception)
    }


    /** 3) Test if property key equals value and therefore throws exception */
    @Test(expected = jUnitReportsPluginException::class)
    fun testPropertyKeyValueSame() {
        val properties = Properties()
        properties[propertyKey] = propertyKey
        properties.getPropertyElement(propertyKey, exception)
    }


    /** 4) Evaluates method for its correctness when key and value differs */
    @Test fun testPropertyKeyCorrect() {
        val properties = Properties()
        properties[propertyKey] = propertyValue
        Assert.assertEquals(propertyValue, properties.getPropertyElement(propertyKey, exception))
    }
}
