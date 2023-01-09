# GemischteTuete

![example workflow](https://github.com/VISUS-Health-IT-GmbH/GemischteTuete/actions/workflows/gradle.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_GemischteTuete&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_GemischteTuete)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_GemischteTuete&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_GemischteTuete)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=VISUS-Health-IT-GmbH_GemischteTuete&metric=coverage)](https://sonarcloud.io/summary/new_code?id=VISUS-Health-IT-GmbH_GemischteTuete)

Jenkins shared library to be used in a BitBucket + Jenkins + Gradle environment.

## BitBucket integration

Containing methods regarding Git operations as well as BitBucket specific operations / information. The Git operations
are targeted at Jenkins pipelines skipping the default checkout because of multiple repositories involved in the build.

## Gradle integration

Containing methods to invoke Gradle in a Windows environment. Including direct calls implemented regarding specific
Gradle plugins or core plugin tasks.

## Jenkins integration

Containing methods to invoke DSL methods and interact with the REST API.

## Workspace integration

Containing methods to interact with the agents' Jenkins workspace as well as with directories / files outside the normal
Jenkins scope via command line interaction.
