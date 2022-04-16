# jUnitReportsPlugin

![example workflow](https://github.com/VISUS-Health-IT-GmbH/jUnitReportsPlugin/actions/workflows/gradle.yml/badge.svg)
![example workflow](https://github.com/VISUS-Health-IT-GmbH/jUnitReportsPlugin/actions/workflows/gradle_validation.yml/badge.svg)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/VISUS-Health-IT-GmbH/jUnitReportsPlugin/actions/workflows/gradle.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_jUnitReportsPlugin&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_jUnitReportsPlugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_jUnitReportsPlugin&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_jUnitReportsPlugin)

Gradle Plugin to allow projects to report their jUnit results to endpoints defined by the user.

## Usage

To find out how to apply this plugin to your Gradle project see the information over at the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.visus.infrastructure.junitreports)!

## Configuration

Optional parameters are available to configure this plugin differently. The parameters can be set as environment 
variables which must have the following structure:

```properties
# Path to file of type String
plugins.junitreporting.properties.path=Path (eg. /var/result/reporting.properties)

# Determine separate testing / production servers of type Boolean?
plugins.junitreporting.properties.isProductionSystem=Boolean or empty
```

Otherwise the parameters must be set in the projects own gradle.properties file.

```properties
# Path to normal configuration properties file
plugins.junitreporting.properties.path=Path (eg. /var/result/reporting.properties)

# Path to alternate configuration properties file (replaces normal path)
plugins.junitreporting.properties.alternatePath=Path (eg. /var/result/reporting_alternate.properties)

# Determine separate testing / production servers (or one single if empty)
plugins.junitreporting.properties.isProductionSystem=Boolean or empty
```

The *reporting.properties* file given by one of the configurations should look like this:

```properties
# which filtering function in project extra properties should be used (e.g. VISUS.filterJUnitProjects)
product.filter=String

# which version should be used in metadata (e.g. VISUS.version)
product.version=String

# which release candidate should be used in metadata (e.g. VISUS.rc)
product.rc=String

# whether release type is only a patch (e.g. VISUS.patch)
product.patch=String

# which REST endpoint should be used for publishing results (e.g. http://127.0.0.1:12345/VISUS)
endpoint.rest=URL

# template of the path where RC results should be stored by default (e.g. \\\\nw-share\\product\\build\\{BRANCH}\\{RC}\\{BUILDID})
# possible templated elements are: {VERSION}, {RC}, {BRANCH}, {BUILDID}
# -> will be replaced using environment variables!
endpoint.rc.default.template=Path

# template of the path were RC results should be stored for version (e.g. \\\\nw-share\\product\\release\\{BRANCH}\\{RC}\\{BUILDID})
# possible templated elements are: {VERSION}, {RC}, {BRANCH}, {BUILDID}
# -> will be replaced using environment variables!
endpoint.rc.version.template=Path

# template of the path were RC results should be stored for patch (e.g. \\\\nw-share\\product\\patch\\{BRANCH}\\{RC}\\{BUILDID})
# possible templated elements are: {VERSION}, {RC}, {BRANCH}, {BUILDID}
# -> will be replaced using environment variables!
endpoint.rc.patch.template=Path
```

### Information regarding RC builds

Only when RC starts with "RC" and does not end with "_build" (eg. "RC02") jUnit results will be written to the path
endpoint as well as the REST endpoint!
