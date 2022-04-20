/*  StringExtensionTest.kt
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
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

import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.exception.PropertyValueIncorrectException
import com.visus.infrastructure.exception.StringExtensionException


/**
 *  StringExtensionTest:
 *  ===================
 *
 *  jUnit test cases on StringExtension
 */
open class StringExtensionTest {
    companion object {
        // test class location in Git repository ($buildDir/classes/kotlin/test)
        private val location = this::class.java.protectionDomain.codeSource.location.path
    }


    /** 1) Tests on encodeBranchName method */
    @Test fun testEncodeBranchName() {
        Assert.assertEquals("feature--12345--jUnitBug", "feature/12345/jUnitBug".encodeBranchName())
        Assert.assertEquals("mainBranch", "mainBranch")
    }


    /** 2) Tests on parsePropertyFunctionName method */
    @Test fun testParsePropertyFunctionName() {
        var failed = false
        try {
            "Project.filtering.function".parsePropertyFunctionName()
        } catch (err: StringExtensionException) {
            Assert.assertEquals(PropertyValueIncorrectException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)

        val (project: String, filteringFunction: String) = "Project.filteringFunction".parsePropertyFunctionName()
        Assert.assertEquals("Project", project)
        Assert.assertEquals("filteringFunction", filteringFunction)
    }


    /** 3) Tests on tryResolveAbsolutePath method */
    @Test fun testTryResolveAbsolutePath() {
        val project = ProjectBuilder.builder().withProjectDir(File(location)).build()

        // nullable null string
        val possibleString: String? = null
        Assert.assertNull(possibleString.tryResolveAbsolutePath(project))

        // directory
        Assert.assertNull(location.tryResolveAbsolutePath(project))

        // absolute & relative path
        Assert.assertEquals(
            File("${location}com/visus/infrastructure/extension/StringExtensionTest.class").absolutePath,
            "${location}com/visus/infrastructure/extension/StringExtensionTest.class".tryResolveAbsolutePath(project)
        )
        Assert.assertEquals(
            File("${location}com/visus/infrastructure/extension/StringExtensionTest.class").absolutePath,
            "com/visus/infrastructure/extension/StringExtensionTest.class".tryResolveAbsolutePath(project)
        )
    }


    /** 4) Tests on fixVersionScheme method */
    @Test fun test_fixVersionScheme() {
        Assert.assertEquals("5.0.0.0", "5".fixVersionScheme())
        Assert.assertEquals("5.1.0.0", "5.1".fixVersionScheme())
        Assert.assertEquals("5.1.2.0", "5.1.2".fixVersionScheme())
        Assert.assertEquals("5.1.2.3", "5.1.2.3".fixVersionScheme())
        Assert.assertEquals("5.1.2.3.4", "5.1.2.3.4".fixVersionScheme())
    }


    /** 5) Tests on versionABCx method */
    @Test fun test_versionABCx() {
        Assert.assertEquals("5.1.0.x", "5.1".versionABCx())
    }


    /** 6) Tests on versionABx method */
    @Test fun test_versionABx() {
        Assert.assertEquals("5.2.x", "5.2.3.4".versionABx())
    }


    /** 7) Tests on versionAx method */
    @Test fun test_versionAx() {
        Assert.assertEquals("5.x", "5.1.2.3".versionAx())
    }
}
