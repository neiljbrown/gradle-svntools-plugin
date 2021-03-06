# gradle-svntools-plugin

A [Gradle](https://www.gradle.org) plugin that provides various Subversion-related tasks.

Here is a very short build script that prints out the SVN revision:

    apply plugin: "at.bxm.svntools"
    task info << {
      println "Current revision is $svntools.info.revisionNumber"
    }

The implementation is based on [SVNKit](http://svnkit.com/), which means that no SVN commandline client is required
and any working copy format (up to version 1.8) is supported.

Binaries are hosted at [Bintray](https://bintray.com/martoe/gradle-plugins/gradle-svntools-plugin/)
and are available at the [jcenter Maven repo](https://bintray.com/bintray/jcenter/).
Please report bugs and feature requests at the [Github issue page](https://github.com/martoe/gradle-svntools-plugin/issues).

## Use-case examples

* Add the SVN revision to the version number when publishing artifacts
* Create a branch and/or tag as part of an automated release process
* Commit files that have been changed during the build process (e.g. bumped version numbers)

## Applying the plugin

(see also the [Gradle plugin portal](https://plugins.gradle.org/plugin/at.bxm.svntools/) page)

### Using the [Gradle plugins DSL](https://www.gradle.org/docs/current/userguide/plugins.html#sec:plugins_block) (Gradle 2.1 and above)

    plugins {
      id "at.bxm.svntools" version "1.0"
    }

### Using an [external dependency](https://www.gradle.org/docs/current/userguide/organizing_build_logic.html#sec:external_dependencies)

    buildscript {
      repositories {
        jcenter()
      }
      dependencies {
        classpath "at.bxm.gradleplugins:gradle-svntools-plugin:1.0"
      }
    }
    apply plugin: "at.bxm.svntools"


# Usage

## General Configuration

The `svntools` block (implemented by `at.bxm.gradleplugins.svntools.SvnToolsPluginExtension`) can be used to

* specify default values for some configuration properties:
    * **username**: The SVN username - leave empty if no authentication is required (default: `$project.svntools.username`)
    * **password**: The SVN password - leave empty if no authentication is required (default: `$project.svntools.password`)
* access information about the *current SVN workspace* (an `at.bxm.gradleplugins.svntools.SvnData` object):
    * **info.revisionNumber** The SVN revision number
    * **info.url** The complete SVN URL of the checked-out project
    * **info.repositoryRootUrl** The root URL of the SVN repository
    * **info.name** Either "trunk", the name of the current branch, or the name of the current tag
    * **info.trunk** "true" if the SVN URL refers to a trunk
    * **info.branch** "true" if the SVN URL refers to a branch
    * **info.tag** "true" if the SVN URL refers to a tag

Note: The `svntools.info` object assumes that the current Gradle project has been checked out from SVN. To retrieve information about other SVN files or workspaces, use the "SvnInfo" task below.

### Example

    apply plugin: "at.bxm.svntools"

    svntools {
      username = "john"
      password = "secret"
    }

    task info << {
      println "Current revision is $svntools.info.revisionNumber"
    }


## SvnInfo task (at.bxm.gradleplugins.svntools.SvnInfo)

Creates a `at.bxm.gradleplugins.svntools.SvnData` object (see above) that contains information about a file or directory
within an SVN workspace.
The object is added as an "extra property" to the Gradle project and may be accessed with `$project.svnData`.

### Configuration

* **sourcePath**: Source path for reading the SVN metadata (default: `$project.projectDir`)
* **targetPropertyName**: The name of the project extra property that will receive the resulting SvnData object (default: `svnData`)
* **ignoreErrors**: Continue the build if the specified path doesn't contain SVN data (default: `false`)
* **username**: The SVN username - leave empty if no authentication is required (default: `$project.svntools.username`)
* **password**: The SVN password - leave empty if no authentication is required (default: `$project.svntools.password`)

### Example

This Gradle script creates a `svn.properties` file that contains the SVN URL and revision of the buildfile, and adds it to the JAR artifact:

    apply plugin: "java"
    apply plugin: "at.bxm.svntools"

    task svnStatus(type: at.bxm.gradleplugins.svntools.SvnInfo) {
      sourcePath = project.buildFile
      doLast {
        def props = new Properties()
        props.setProperty("url", project.svnData.url)
        props.setProperty("revision", project.svnData.revisionNumber as String)
        file("$project.buildDir/svn.properties").withWriter { props.store(it, null) }
      }
    }

    jar {
      dependsOn svnStatus
      from(project.buildDir, { include "svn.properties" })
    }

## SvnCommit task (at.bxm.gradleplugins.svntools.SvnCommit)

Commits a list of files (and directories) within the current SVN workspace.

### Configuration

* **source**: A list of files and directories that should be committed.
              If these are not under version control already, they will be added first.
              If this list is empty of the files contain no modifications, no commit will be executed.
* **commitMessage**: An optional commit message.
* **username**: The SVN username - leave empty if no authentication is required (default: `$project.svntools.username`)
* **password**: The SVN password - leave empty if no authentication is required (default: `$project.svntools.password`)

### Example

This Gradle script commits a changelog file to SVN:

    apply plugin: "at.bxm.svntools"

    version = "1.0"

    task createChangelog() {
      project.ext.changelog = file("changelog_${project.version}.txt")
      project.ext.changelog.text = "list of changes for this release..."
    }

    task commitChangelog(type: at.bxm.gradleplugins.svntools.SvnCommit, dependsOn: createChangelog) {
      source << project.ext.changelog
      commitMessage = "Changelog added"
    }

## SvnTag task (at.bxm.gradleplugins.svntools.SvnTag)

Creates an SVN tag based on a local SVN workspace.

### Configuration

* **workspaceDir**: Local workspace that should be tagged (default: `$project.projectDir`)
* **tagName**: Name of the SVN tag (required, no default)
* **commitMessage**: An optional commit message.
* **username**: The SVN username - leave empty if no authentication is required (default: `$project.svntools.username`)
* **password**: The SVN password - leave empty if no authentication is required (default: `$project.svntools.password`)

### Example

The `release` task creates an SVN tag using the current version number:

    apply plugin: "at.bxm.svntools"

    version = "1.0"

    task svnTag(type: at.bxm.gradleplugins.svntools.SvnTag) {
      tagName = "v$project.version"
      commitMessage = "Release version $project.version"
    }

    task release(dependsOn: svnTag)

[![Build Status](https://travis-ci.org/martoe/gradle-svntools-plugin.png)](https://travis-ci.org/martoe/gradle-svntools-plugin)
