/*  Prefix.kt
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
package com.visus.infrastructure.util

import java.util.Properties

import com.visus.infrastructure.extension.t


/**
 *  Tries to resolve other properties prefix from a specific property value
 *
 *  @param properties object containing all key-value pairs
 *  @param key to be used in evaluation
 *  @return either "PRODUCTIVE." or "TESTING." if found, nothing otherwise
 */
internal fun resolvePrefix(properties: Properties, key: String) = properties[key]?.let {
    val value : String = it as String
    when {
        value.isBlank() || value == key -> ""
        else                            -> value.toBoolean() t "PRODUCTIVE." ?: "TESTING."
    }
} ?: ""
