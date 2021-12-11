/*  ProjectExtensionException.kt
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
 *  Base extension for every extension thrown by the extensions provided to Project class
 *
 *  @author Tobias Hahnen
 */
internal sealed class ProjectExtensionException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when no properties provided to project in any way
 */
internal class NoPropertiesProvidedException(message: String) : ProjectExtensionException(message)


/**
 *  Exception thrown when
 */
internal class NoPropertiesFileProvidedException(message: String) : ProjectExtensionException(message)
