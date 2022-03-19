/*  ProjectExtensionTestGroovy.groovy
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

import kotlin.jvm.JvmClassMappingKt
import kotlin.reflect.KClass

import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.testfixtures.ProjectBuilder

import com.visus.infrastructure.exception.GroovyCompatibleException
import com.visus.infrastructure.exception.jUnitReportsPluginException
import com.visus.infrastructure.exception.PropertyValueIncorrectException


/**
 *  jUnit test cases on ProjectExtension but with Groovy
 */
class ProjectExtensionTestGroovy {
    /** 1) Tests on getProjectExtraPropertyElement (empty) for compatibility with Groovy */
    @Test(expected = GroovyCompatibleException)
    void testGetProjectExtraPropertyElementEmpty() {
        def project = ProjectBuilder.builder().build()
        use (ProjectExtensionKt) {
            project.getProjectExtraPropertyElement(
                new Properties(), "",
                JvmClassMappingKt.getKotlinClass(Test1ExceptionGroovy::getClass() as Class) as KClass,
                JvmClassMappingKt.getKotlinClass(Test2ExceptionGroovy::getClass() as Class) as KClass
            )
        }
    }


    /** 2) Tests on getProjectExtraPropertyElement (missing) for compatibility with Groovy */
    @Test(expected = PropertyValueIncorrectException)
    void testGetProjectExtraPropertyElementMissing() {
        def properties = new Properties()
        properties["t-key"] = "project1"

        def project = ProjectBuilder.builder().build()
        use (ProjectExtensionKt) {
            project.getProjectExtraPropertyElement(
                properties, "t-key",
                JvmClassMappingKt.getKotlinClass(Test1ExceptionGroovy::getClass() as Class) as KClass,
                JvmClassMappingKt.getKotlinClass(Test2ExceptionGroovy::getClass() as Class) as KClass
            )
        }
    }


    /** 3) Tests on getProjectExtraPropertyElement (found) for compatibility with Groovy */
    @Test void testGetProjectExtraPropertyElementFound() {
        def properties = new Properties()
        properties["t-key"] = "Filter.function"

        // project properties reference (project.properties.set can not be used directly!)
        def project = ProjectBuilder.builder().build()
        def propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension)
        propertiesExtension.Filter = [
            "function" : { String _ -> true }
        ]

        use (ProjectExtensionKt) {
            project.getProjectExtraPropertyElement(
                properties, "t-key",
                JvmClassMappingKt.getKotlinClass(Test1ExceptionGroovy::getClass() as Class) as KClass,
                JvmClassMappingKt.getKotlinClass(Test2ExceptionGroovy::getClass() as Class) as KClass
            ) as Closure
        }
    }
}


/** Necessary test exceptions for testing getProjectExtraPropertyElement */
class Test1ExceptionGroovy extends jUnitReportsPluginException {
    Test1ExceptionGroovy(String message) { super(message) }
}
class Test2ExceptionGroovy extends jUnitReportsPluginException {
    Test2ExceptionGroovy(String message) { super(message) }
}
