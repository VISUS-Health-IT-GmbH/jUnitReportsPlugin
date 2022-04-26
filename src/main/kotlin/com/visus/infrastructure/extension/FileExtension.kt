/*  FileExtension.kt
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
@file:Suppress("TooGenericExceptionCaught", "SwallowedException")

package com.visus.infrastructure.extension

import java.io.File

import org.jsoup.Jsoup

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.HTMLFailedNumberParserException
import com.visus.infrastructure.exception.HTMLIgnoredNumberParserException
import com.visus.infrastructure.exception.HTMLFailedListParserException
import com.visus.infrastructure.exception.HTMLIgnoredListParserException


/**
 *  Parse reporting HTML file to get number of failed jUnit tests
 *
 *  @return number of failed jUnit tests
 *  @throws HTMLFailedNumberParserException when parsing HTML file did not work!
 */
@Throws(HTMLFailedNumberParserException::class)
internal fun File.parseHTMLFailures() : Int {
    return try {
        Jsoup.parse(this, null).select("#failures div")[0].ownText().toInt()
    } catch (err: Exception) {
        throw HTMLFailedNumberParserException(
            "[${jUnitReportsPlugin::class.simpleName} -> File.parseHTMLFailures] Cannot parse number of failed jUnit " +
            "tests from file '${this.absolutePath}'! See error: ${err.message}"
        )
    }
}


/**
 *  Parse reporting HTML file to get number of ignored jUnit tests
 *
 *  @return number of ignored jUnit tests
 *  @throws HTMLIgnoredNumberParserException when parsing HTML file did not work!
 */
@Throws(HTMLIgnoredNumberParserException::class)
internal fun File.parseHTMLIgnored() : Int {
    return try {
        Jsoup.parse(this, null).select("#ignored div")[0].ownText().toInt()
    } catch (err: Exception) {
        throw HTMLIgnoredNumberParserException(
            "[${jUnitReportsPlugin::class.simpleName} -> File.parseHTMLIgnored] Cannot parse number of ignored jUnit " +
            "tests from file '${this.absolutePath}'! See error: ${err.message}"
        )
    }
}


/**
 *  Parse reporting HTML file to get list of failed jUnit tests
 *
 *  @return list of failed jUnit tests
 *  @throws HTMLFailedListParserException when parsing HTML file did not work!
 */
@Throws(HTMLFailedListParserException::class)
internal fun File.parseHTMLFailedTests() : Set<String> {
    return try {
        assert(this.parseHTMLFailures() > 0)

        val failedTests = mutableSetOf<String>()

        Jsoup.parse(this, null).select("#tab0 ul li").forEach {
            failedTests.add(it.select("a")[1].attributes()["href"].replace("classes/", "").replace("html#", ""))
        }

        when {
            failedTests.size <= 0 -> throw Exception(
                "[${jUnitReportsPlugin::class.simpleName} -> File.parseHTMLFailedTests] No failed tests found in list!"
            )
        }

        failedTests
    } catch (err: Exception) {
        throw HTMLFailedListParserException(
            "[${jUnitReportsPlugin::class.simpleName} -> File.parseHTMLFailedTests] Cannot parse list of failed " +
            "jUnit tests from file '${this.absolutePath}'! See error: ${err.message}"
        )
    }
}


/**
 *  Parse reporting HTML file to get list of ignored jUnit tests
 *
 *  @return list of ignored jUnit tests
 *  @throws HTMLIgnoredListParserException when parsing HTML file did not work!
 */
@Throws(HTMLIgnoredListParserException::class)
internal fun File.parseHTMLIgnoredTests() : Set<String> {
    return try {
        assert(this.parseHTMLIgnored() > 0)

        val failedTests = mutableSetOf<String>()

        Jsoup.parse(this, null).select("#tab1 ul li").forEach {
            failedTests.add(it.select("a")[1].attributes()["href"].replace("classes/", "").replace("html#", ""))
        }

        failedTests
    } catch (err: Exception) {
        throw HTMLIgnoredListParserException(
            "[${jUnitReportsPlugin::class.simpleName} -> File.parseHTMLIgnoredTests] Cannot parse list of ignored " +
            "jUnit tests from file '${this.absolutePath}'! See error: ${err.message}"
        )
    }
}
