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
package com.visus.infrastructure.extension

import java.io.File

import org.gradle.api.Project

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.PropertyValueIncorrectException


/**
 *  Encodes the branch name when storing RC results at file path where slashes are not allowed in folder name
 *  or indicate subfolder
 *
 *  @return encoded branch name
 */
internal fun String.encodeBranchName(): String = this.replace("/", "--")


/**
 *  Parses a string containing the path of property and function name into separate entities
 *
 *  @return pair of property and function name
 *  @throws PropertyValueIncorrectException when string is formatted incorrectly
 *
 *  TODO: Allow functions path other than eg "Product.filteringFunction" like "Product.filtering.function"!
 */
@Throws(PropertyValueIncorrectException::class)
internal fun String.parsePropertyFunctionName(): Pair<String, String> {
    val elements = this.split(".")
    if (elements.size != 2) {
        throw PropertyValueIncorrectException(
            "[${jUnitReportsPlugin::class.simpleName} -> String.parsePropertyFunctionName] Only functions allowed " +
            "yet of form 'Project.filteringFunction'!"
        )
    }

    return Pair(elements[0], elements[1])
}


/**
 *  Tries to resolve a string as a path in the following order
 *  - path itself
 *  - in target.projectDir
 *
 *  @param target the project which the plugin is applied to
 *  @return absolute path or null
 */
internal fun String?.tryResolveAbsolutePath(target: Project) : String? = this?.let {
    listOf(it, "${target.projectDir}$it").forEach { path ->
        with (File(path)) {
            when {
                this.exists() && this.isFile -> this.absolutePath
            }
        }
    }
    null
}
