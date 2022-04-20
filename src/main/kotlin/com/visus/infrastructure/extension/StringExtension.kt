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
    listOf(it, "${target.projectDir}/$it").forEach { path ->
        val fileObj = File(path)
        when {
            fileObj.exists() && fileObj.isFile -> return@let fileObj.absolutePath
        }
    }
    null
}


/**
 *  Fix a specific version to match the scheme <Major>.<Minor>.<Micro>.<Patch>
 *  Accepted versions look like this: 5.3.0.0
 *  -> 5 will be adjusted to 5.0.0.0
 *  -> 5.1 will be adjusted to 5.1.0.0
 *  -> 5.1.2 will be adjusted to 5.1.2.0
 *  -> 5.1.2.3 will not be adjusted
 *  -> 5.1.2.3.4 will not be adjusted
 *
 *  @return the adjusted version
 */
internal fun String.fixVersionScheme() : String = with (this.split("\\.".toRegex())) {
    if (this.size >= 4) {
        return@with this@fixVersionScheme
    }

    var newVersion = this@fixVersionScheme
    for (i in 0..(3-this.size)) {
        newVersion = "$newVersion.0"
    }
    newVersion
}


/**
 *  Get version without the patch level
 *  Accepted versions look like this: 5.3.0.x
 *
 *  @return the adjusted version
 */
internal fun String.versionABCx() : String  = with (this.fixVersionScheme().split("\\.".toRegex())) {
    "${this[0]}.${this[1]}.${this[2]}.x"
}


/**
 *  Get version without the patch / micro level
 *  Accepted versions look like this: 5.3.x
 *
 *  @return the adjusted version
 */
internal fun String.versionABx() : String = with (this.fixVersionScheme().split("\\.".toRegex())) {
    "${this[0]}.${this[1]}.x"
}


/**
 *  Get version without the patch / micro / minor level
 *  Accepted versions look like this: 5.x
 *
 *  @return the adjusted version
 */
internal fun String.versionAx() : String = "${this.fixVersionScheme().split("\\.".toRegex())[0]}.x"
