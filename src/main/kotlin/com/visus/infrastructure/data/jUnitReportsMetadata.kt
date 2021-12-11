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
@Suppress("kotlin:S117")
data class jUnitReportsMetadata(
    val id: Int,                // Build id
    val start: String,          // Build started timestamp
    val branch: String,         // Branch in repository
    val git_commit: String,     // Branch in repository last commit hash
    val version: String,        // Version of repository
    val rc: String,             // Release candidate of repository
    val projects: List<String>  // List of subprojects tested using jUnit
)
