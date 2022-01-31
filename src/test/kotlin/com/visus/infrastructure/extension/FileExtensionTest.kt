/*  FileExtensionTest.kt
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

import java.io.File

import org.junit.Assert
import org.junit.Test

import com.visus.infrastructure.exception.*


/**
 *  FileExtensionTest:
 *  =================
 *
 *  jUnit test cases on FileExtension
 */
open class FileExtensionTest {
    companion object {
        // path to example html files in "resources" folder
        private val htmlCorrect = resource("html/correct.html")
        private val htmlWrong   = resource("html/wrong.html")
        private val htmlWrong2  = resource("html/wrong2.html")

        /** Simple helper method for resources */
        private fun resource(path: String) : String = this::class.java.classLoader.getResource(path)!!.path.replace(
            "%20", " "
        )
    }


    /** 1) Tests on parseHTMLFailures method with wrong input HTML */
    @Test fun testParseHTMLFailuresWrong() {
        var failed = false
        try {
            File(htmlWrong).parseHTMLFailures()
        } catch (err: FileExtensionException) {
            Assert.assertEquals(HTMLFailedNumberParserException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 2) Tests on parseHTMLFailures method with correct input HTML */
    @Test fun testParseHTMLFailuresCorrect() {
        Assert.assertEquals(23, File(htmlCorrect).parseHTMLFailures())
    }


    /** 3) Tests on parseHTMLIgnored method with wrong input HTML */
    @Test fun testParseHTMLIgnoredWrong() {
        var failed = false
        try {
            File(htmlWrong).parseHTMLIgnored()
        } catch (err: FileExtensionException) {
            Assert.assertEquals(HTMLIgnoredNumberParserException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 4) Tests on parseHTMLIgnored method with correct input HTML */
    @Test fun testParseHTMLIgnoredCorrect() {
        Assert.assertEquals(1, File(htmlCorrect).parseHTMLIgnored())
    }


    /** 5) Tests on parseHTMLFailedTests method with wrong input HTML */
    @Test fun testParseHTMLFailedTestsWrong() {
        var failed = false
        try {
            File(htmlWrong).parseHTMLFailedTests()
        } catch (err: FileExtensionException) {
            Assert.assertEquals(HTMLFailedListParserException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 6) Tests on parseHTMLFailedTests method with wrong input HTML (assertion failed) */
    @Test fun testParseHTMLFailedTestsWrong2() {
        var failed = false
        try {
            File(htmlWrong2).parseHTMLFailedTests()
        } catch (err: AssertionError) {
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 7) Tests on parseHTMLFailedTests method with correct input HTML */
    @Test fun testParseHTMLFailedTestsCorrect() {
        val tests = File(htmlCorrect).parseHTMLFailedTests()
        Assert.assertEquals(23, tests.size)

        setOf(
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginExtraPropertiesSet",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginExtraWithProductionSystemPropertiesSet",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginExtraWithTestingSystemPropertiesSet",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginFilteringFunctionNotFound",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingDefaultEndpointTemplate",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingFilteringFunctionName",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingPatchEndpointTemplate",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingProductPatchInfo",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingProductRC",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingProductVersion",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingRESTEndpoint",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginMissingVersionEndpointTemplate",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginProductRCNotFound",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginProductVersionNotFound",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginToNonRootProject",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginWithAllEnvironmentVariablesToProject",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginWithEnvironmentVariablesToProject",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginWithoutJavaPlugin",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginWithoutPropertiesToProject",
            "com.visus.infrastructure.jUnitReportsPluginTest.testApplyPluginWrongFilteringFunctionName",
            "com.visus.infrastructure.jUnitReportsPluginTest.testEvaluateRootProjectTasksBuildServer",
            "com.visus.infrastructure.jUnitReportsPluginTest.testEvaluateRootProjectTasksNoBuildServer",
            "com.visus.infrastructure.jUnitReportsPluginTest.testEvaluateSubProjectTasks"
        ).forEach {
            Assert.assertTrue(tests.contains(it))
        }
    }


    /** 8) Tests on parseHTMLIgnoredTests method with wrong input HTML */
    @Test fun testParseHTMLIgnoredTestsWrong() {
        var failed = false
        try {
            File(htmlWrong).parseHTMLIgnoredTests()
        } catch (err: FileExtensionException) {
            Assert.assertEquals(HTMLIgnoredListParserException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 9) Tests on parseHTMLIgnoredTests method with wrong input HTML (assertion failed) */
    @Test fun testParseHTMLIgnoredTestsWrong2() {
        var failed = false
        try {
            File(htmlWrong2).parseHTMLIgnoredTests()
        } catch (err: AssertionError) {
            failed = true
        }
        Assert.assertTrue(failed)
    }


    /** 10) Tests on parseHTMLIgnoredTests method with correct input HTML */
    @Test fun testParseHTMLIgnoredTestsCorrect() {
        val tests = File(htmlCorrect).parseHTMLIgnoredTests()
        Assert.assertEquals(1, tests.size)
        Assert.assertTrue(
            tests.contains("com.visus.infrastructure.extension.StringExtensionTest.testTryResolveAbsolutePath")
        )
    }
}
