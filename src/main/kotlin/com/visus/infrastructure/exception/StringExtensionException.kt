/*  StringExtensionException.kt
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


/**
 *  Base extension for every extension thrown by the extensions provided to String class
 *
 *  @author Tobias Hahnen
 */
internal sealed class StringExtensionException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when string containing property and function name is not build correctly
 *  (e.g. "Project.filtering.function" instead of "Project.filteringFunction")
 */
internal class PropertyValueIncorrectException(message: String) : StringExtensionException(message)

