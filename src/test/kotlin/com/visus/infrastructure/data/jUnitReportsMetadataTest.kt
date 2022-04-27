/*  jUnitReportsMetadataTest.kt
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
package com.visus.infrastructure.data

import org.junit.Assert
import org.junit.Test


/**
 *  jUnitReportsMetadataTest:
 *  ========================
 *
 *  jUnit test cases on jUnitReportsMetadata
 */
open class jUnitReportsMetadataTest {
    /** 1) test toJSON method for converting jUnitReportsMetadata object to JSON string */
    @Test fun testToJSON() {
        Assert.assertEquals(
            "{\"id\":1,\"branch\":\"test\",\"commit\":\"abc\",\"version\":\"1.0\",\"rc\":\"RC01\",\"type\":\"DAILY\"," +
            "\"projects\":[\"a\",\"b\",\"c\"]}",
            toJSON(jUnitReportsMetadata(1, "test", "abc", "1.0", "RC01", "DAILY", listOf("a", "b", "c")))
                .replace("\t", "")
                .replace("\n", "")
                .replace(" ", "")
        )

        Assert.assertEquals(
            "{\"id\":1,\"branch\":\"test\",\"commit\":\"abc\",\"version\":null,\"rc\":null,\"type\":null," +
            "\"projects\":[]}",
            toJSON(jUnitReportsMetadata(1, "test", "abc", null, null, null, listOf()))
                .replace("\t", "")
                .replace("\n", "")
                .replace(" ", "")
        )
    }
}
