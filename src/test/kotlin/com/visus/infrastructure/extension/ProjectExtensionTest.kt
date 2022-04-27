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

import java.io.File
import java.io.FileInputStream
import java.util.Properties

import org.junit.Assert
import org.junit.Test

import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.testfixtures.ProjectBuilder

import com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties
import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

import com.visus.infrastructure.jUnitReportsPlugin
import com.visus.infrastructure.exception.NoPropertiesFileProvidedException
import com.visus.infrastructure.exception.NoPropertiesProvidedException
import com.visus.infrastructure.exception.ProjectExtensionException
import com.visus.infrastructure.exception.jUnitReportsPluginException
import com.visus.infrastructure.util.FilteringFunction


/**
 *  ProjectExtensionTest:
 *  ====================
 *
 *  jUnit test cases on ProjectExtension
 */
open class ProjectExtensionTest {
    companion object {
        // current Gradle project buildDir ($buildDir/classes/kotlin/test) -> 3x parent
        private val buildDir = File(
            this::class.java.protectionDomain.codeSource.location.path
        ).parentFile.parentFile.parentFile

        // The src/test/kotlin folder of this plugin!
        private val pluginSrcTestJava = File("${buildDir.parentFile.absolutePath}/src/test/kotlin")


        // paths / resources used throughout this test
        private val projectDir = resource("project")
        private const val project1Path = "1.properties"
        private val project1 = resource("project/1.properties")


        /** Simple helper method for resources */
        private fun resource(path: String) : String = this::class.java.classLoader.getResource(path)!!.path.replace(
            "%20", " "
        )
    }


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
            Assert.assertEquals(sysProp.second, project.resolvePropertyKey(sysProp.first))
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


    /** 3) Tests on readPropertiesFromFile (empty, empty) */
    @Test(expected = NoPropertiesFileProvidedException::class)
    fun testReadPropertiesFromFileEmptyEmpty() {
        ProjectBuilder.builder().withProjectDir(File(projectDir)).build()
            .readPropertiesFromFile(Properties(), listOf())
    }


    /** 4) Tests on readPropertiesFromFile (correct, empty) */
    @Test(expected = NoPropertiesFileProvidedException::class)
    fun testReadPropertiesFromFileCorrectEmpty() {
        val properties = Properties()
        properties["t-key"] = project1

        ProjectBuilder.builder().withProjectDir(File(projectDir)).build()
            .readPropertiesFromFile(properties, listOf())
    }


    /** 5) Tests on readPropertiesFromFile (correct, wrong) */
    @Test(expected = NoPropertiesFileProvidedException::class)
    fun testReadPropertiesFromFileCorrectWrong() {
        val properties = Properties()
        properties["t-key"] = project1

        ProjectBuilder.builder().withProjectDir(File(projectDir)).build()
            .readPropertiesFromFile(properties, listOf("t-key2", "t-key3"))
    }


    /** 6) Tests on readPropertiesFromFile (correct, correct) */
    @Test fun testReadPropertiesFromFileCorrectCorrect() {
        val correctProperties = Properties()
        correctProperties.load(FileInputStream(project1))

        val project = ProjectBuilder.builder().withProjectDir(File(projectDir)).build()

        val properties1 = Properties()
        properties1["t-key"] = project1
        val nProperties1 = project.readPropertiesFromFile(properties1, listOf("t-key"))
        Assert.assertEquals(correctProperties.size, nProperties1.size)
        correctProperties.forEach { key, value ->
            Assert.assertEquals(value, nProperties1[key])
        }

        val properties2 = Properties()
        properties2["t-key"] = project1Path
        val nProperties2 = project.readPropertiesFromFile(properties2, listOf("t-key"))
        Assert.assertEquals(correctProperties.size, nProperties2.size)
        correctProperties.forEach { key, value ->
            Assert.assertEquals(value, nProperties2[key])
        }
    }


    /** 7) Tests on getProjectExtraPropertyElement (empty) */
    @Test(expected = Test1Exception::class)
    fun testGetProjectExtraPropertyElementEmpty() {
        ProjectBuilder.builder().build()
            .getProjectExtraPropertyElement(Properties(), "", Test1Exception::class, Test2Exception::class)
    }


    /** 8) Tests on getProjectExtraPropertyElement (missing) */
    @Test(expected = Test2Exception::class)
    fun testGetProjectExtraPropertyElementMissing() {
        val properties = Properties()
        properties["t-key"] = project1
        ProjectBuilder.builder().build()
            .getProjectExtraPropertyElement(properties, "t-key", Test1Exception::class, Test2Exception::class)
    }


    /** 9) Tests on getProjectExtraPropertyElement (found) */
    @Test fun testGetProjectExtraPropertyElementFound() {
        val properties = Properties()
        properties["t-key"] = "Filter.function"

        // project properties reference (project.properties.set can not be used directly!)
        val project = ProjectBuilder.builder().build()
        val propertiesExtension = project.extensions.getByType(ExtraPropertiesExtension::class.java)
        propertiesExtension.set(
            "Filter", mapOf(
                "function" to { _: String -> true }
            )
        )

        // INFO: Implicit assertion as cast would fail if types do not match!
        @Suppress("UNCHECKED_CAST")(project.getProjectExtraPropertyElement(
            properties, "t-key", Test1Exception::class, Test2Exception::class
        ) as FilteringFunction)
    }


    /** 10) Test on "hasActualJUnitTestcases" without Java plugin (which is necessary) */
    @Test(expected = IllegalStateException::class)
    fun testHasActualJUnitTestcasesWithoutJavaPlugin() {
        Assert.assertFalse(ProjectBuilder.builder().build().hasActualJUnitTestcases())
    }


    /** 11) Test on "hasActualJUnitTestcases" with Java plugin but no test cases */
    @Test fun testHasActualJUnitTestcasesNoTestFile() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        // set listener to evaluate output logged by plugin
        project.logging.addStandardOutputListener { message ->
            Assert.assertTrue(
                message.contains(
                    "[${jUnitReportsPlugin::class.simpleName} -> Project.hasActualTests] No test cases were found in " +
                    "project '$${project.name}'! Maybe ignore it in the filtering function provided using the " +
                    "property 'product.filter' or add some actual jUnit tests."
                )
            )
        }

        Assert.assertFalse(project.hasActualJUnitTestcases())
    }


    /** 12) Test on "hasActualJUnitTestcases" with Java plugin and test cases (from this very plugin) */
    @Test fun testHasActualJUnitTestcasesWithTestFile() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(JavaPlugin::class.java)

        project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.filter {
            it.name.contains("test", ignoreCase = true)
        }.forEach {
            it.allSource.srcDir(pluginSrcTestJava.absolutePath)
        }

        Assert.assertTrue(project.hasActualJUnitTestcases())
    }
}


/** Necessary test exceptions for testing getProjectExtraPropertyElement */
internal class Test1Exception(message: String) : jUnitReportsPluginException(message)
internal class Test2Exception(message: String) : jUnitReportsPluginException(message)
