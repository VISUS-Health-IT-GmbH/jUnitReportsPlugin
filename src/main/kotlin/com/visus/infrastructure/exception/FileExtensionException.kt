/*  FileExtensionException.kt
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
 *  Base extension for every extension thrown by the extensions provided to File class
 *
 *  @author Tobias Hahnen
 */
internal sealed class FileExtensionException(message: String) : jUnitReportsPluginException(message)


/**
 *  Exception thrown when parsing HTML file (jUnit report) to find number of failed tests did not work
 */
internal class HTMLFailedNumberParserException(message: String) : FileExtensionException(message)


/**
 *  Exception thrown when parsing HTML file (jUnit report) to find number of ignored tests did not work
 */
internal class HTMLIgnoredNumberParserException(message: String) : FileExtensionException(message)


/**
 *  Exception thrown when parsing HTML file (jUnit report) to find list of failed tests did not work
 */
internal class HTMLFailedListParserException(message: String) : FileExtensionException(message)


/**
 *  Exception thrown when parsing HTML file (jUnit report) to find list of ignored tests did not work
 */
internal class HTMLIgnoredListParserException(message: String) : FileExtensionException(message)
