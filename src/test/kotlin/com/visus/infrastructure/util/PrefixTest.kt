/*  PrefixTest.kt
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
package com.visus.infrastructure.util

import java.util.Properties

import org.junit.Assert
import org.junit.Test


/**
 *  PrefixTest:
 *  ==========
 *
 *  jUnit test cases on Prefix
 */
open class PrefixTest {
    companion object {
        // Key used in tests
        private const val key = "t-key"
    }

    /** Tests on resolvePrefix */
    @Test fun testResolvePrefix() {
        Assert.assertEquals("", resolvePrefix(Properties(), key))

        val properties1 = Properties()
        properties1[key] = ""
        Assert.assertEquals("", resolvePrefix(properties1, key))

        val properties2 = Properties()
        properties2[key] = key
        Assert.assertEquals("", resolvePrefix(properties2, key))

        val properties3 = Properties()
        properties3[key] = "tRuE"
        Assert.assertEquals("PRODUCTIVE.", resolvePrefix(properties3, key))

        val properties4 = Properties()
        properties4[key] = "fAlSe"
        Assert.assertEquals("TESTING.", resolvePrefix(properties4, key))
    }
}
