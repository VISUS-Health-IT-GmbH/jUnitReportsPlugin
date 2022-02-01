/*  jUnitReportsMetadata.kt
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
package com.visus.infrastructure.data


/**
 *  jUnitReportsMetadata:
 *  ====================
 *
 *  Data class containing all necessary information written to the metadata file "jUnit.json"
 */
@Suppress("kotlin:S117", "ClassNaming")
data class jUnitReportsMetadata(
    val id: Int,                // build id
    val branch: String,         // branch in repository
    val commit: String,         // commit hash
    val version: String?,       // (optional) version of repository
    val rc: String?,            // (optional) release candidate of repository
    val type: String?,          // (optional) build type
    val projects: List<String>  // list of subprojects tested using jUnit
)
