/*  ProjectExtensionTest.kt
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

import org.junit.Assert
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties
import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

import com.visus.infrastructure.exception.NoPropertiesProvidedException
import com.visus.infrastructure.exception.ProjectExtensionException


/**
 *  ProjectExtensionTest:
 *  ====================
 *
 *  jUnit test cases on ProjectExtension
 */
open class ProjectExtensionTest {
    /** 1) Tests on resolvePropertyKey method */
    @Test fun testResolvePropertyKey() {
        val project = ProjectBuilder.builder().build()

        Assert.assertNull(project.resolvePropertyKey("notFound"))

        val env = Pair("e-key", "e-value")
        withEnvironmentVariable(
            env.first, env.second
        ).execute {
            Assert.assertEquals(env.second, System.getenv(env.first))
            Assert.assertEquals(env.second, project.resolvePropertyKey(env.first))
        }

        val sysProp = Pair("s-key", "s-value")
        restoreSystemProperties {
            System.setProperty(sysProp.first, sysProp.second)
            Assert.assertEquals(sysProp.second, System.getProperty(sysProp.first))
        }

        val prjProp = Pair("p-key", "p-val")
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(prjProp.first, prjProp.second)
        Assert.assertEquals(prjProp.second, project.resolvePropertyKey(prjProp.first))
    }


    /** 2) Tests on readProperties method */
    @Test fun testReadProperties() {
        val project = ProjectBuilder.builder().build()

        var failed = false
        try {
            project.readProperties(listOf())
        } catch (err: ProjectExtensionException) {
            Assert.assertEquals(NoPropertiesProvidedException::class, err::class)
            failed = true
        }
        Assert.assertTrue(failed)

        val env = Pair("t-key", "t-value")
        withEnvironmentVariable(
            env.first, env.second
        ).execute {
            Assert.assertEquals(env.second, System.getenv(env.first))

            val properties = project.readProperties(listOf(env.first))
            Assert.assertEquals(1, properties.size)
            Assert.assertTrue(properties.containsKey(env.first))
            Assert.assertTrue(properties.containsValue(env.second))
            Assert.assertEquals(env.second, properties.getProperty(env.first))
        }
    }
}
