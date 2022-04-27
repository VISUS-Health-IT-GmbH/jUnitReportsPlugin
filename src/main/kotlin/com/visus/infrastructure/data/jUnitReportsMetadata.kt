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

import java.util.stream.Collectors

import com.visus.infrastructure.extension.t


/**
 *  jUnitReportsMetadata:
 *  ====================
 *
 *  Data class containing all necessary information written to the metadata file "jUnit.json"
 */
@Suppress("kotlin:S101", "ClassNaming")
data class jUnitReportsMetadata(
    val id: Int,                // build id
    val branch: String,         // branch in repository
    val commit: String,         // commit hash
    val version: String?,       // (optional) version of repository
    val rc: String?,            // (optional) release candidate of repository
    val type: String?,          // (optional) build type
    val projects: List<String>  // list of subprojects tested using jUnit
)


/**
 *  Converts a jUnitReportsMetadata object to JSON (replacing "jacksonObjectMapper().writeValueAsString(...)")
 *
 *  @param metadata the object to turn into JSON
 *  @return JSON string
 */
internal fun toJSON(metadata: jUnitReportsMetadata) : String {
    var output = "{"
    output += "\n\t\"id\": ${metadata.id},"
    output += "\n\t\"branch\": \"${metadata.branch}\","
    output += "\n\t\"commit\": \"${metadata.commit}\","
    output += "\n\t\"version\": ${metadata.version?.let { "\"${metadata.version}\"" } ?: "null"},"
    output += "\n\t\"rc\": ${metadata.rc?.let { "\"${metadata.rc}\"" } ?: "null"},"
    output += "\n\t\"type\": ${metadata.type?.let { "\"${metadata.type}\"" } ?: "null"},"
    output += "\n\t\"projects\": ["
    output += "\n\t\t${(metadata.projects.isNotEmpty() t metadata.projects.stream().collect(Collectors.joining("\",\n\t\t\"", "\"", "\""))) ?: ""}"
    output += "\n\t]"
    output += "\n}"
    return output
}
