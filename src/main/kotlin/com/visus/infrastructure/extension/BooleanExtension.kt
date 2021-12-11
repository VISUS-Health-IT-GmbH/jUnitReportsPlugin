/*  BooleanExtension.kt
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


/**
 *  Simple ternary operator to use on boolean values as known in other languages
 *  Usage:
 *      val res = <Condition> t <Value if true> ?: <Value if false>
 *
 *  INFO: https://discuss.kotlinlang.org/t/ternary-operator/2116/81
 *
 *  "The future is now, old man"
 *  - Dewey (MitM)
 */
internal infix fun <T: Any> Boolean.t(value: T) : T? = if (this) value else null
