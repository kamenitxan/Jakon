[![Java CI](https://github.com/kamenitxan/Jakon/actions/workflows/sbt.yml/badge.svg)](https://github.com/kamenitxan/Jakon/actions/workflows/sbt.yml)
[![codecov](https://codecov.io/gh/kamenitxan/Jakon/branch/master/graph/badge.svg)](https://codecov.io/gh/kamenitxan/Jakon)
[![Quality Gate Status](https://sonarqube.kamenitxan.eu/api/project_badges/measure?project=kamenitxan_Jakon_AYX5P6qaok6eoBlpoZHT&metric=alert_status&token=088f6735c2704892b49dfa0d7a47c5d05fb943ec)](https://sonarqube.kamenitxan.eu/dashboard?id=kamenitxan_Jakon_AYX5P6qaok6eoBlpoZHT)
[![Bugs](https://sonarqube.kamenitxan.eu/api/project_badges/measure?project=kamenitxan_Jakon_AYX5P6qaok6eoBlpoZHT&metric=bugs&token=088f6735c2704892b49dfa0d7a47c5d05fb943ec)](https://sonarqube.kamenitxan.eu/dashboard?id=kamenitxan_Jakon_AYX5P6qaok6eoBlpoZHT)
![](https://raw.githubusercontent.com/kamenitxan/Jakon/master/src/main/resources/static/jakon/css/images/logo2.png)

Scala static web generator 

```
ThisBuild / resolvers += "Artifactory" at "https://nexus.kamenitxan.eu/repository/jakon/"
libraryDependencies += "cz.kamenitxan" %% "jakon" % "0.5.1"
```

```xml
<repositories>
    <repository>
        <id>kamenitxan-maven-repository</id>
        <url>https://nexus.kamenitxan.eu/repository/jakon/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>cz.kamenitxan</groupId>
        <artifactId>jakon_3</artifactId>
        <version>0.5.1</version>
    </dependency>
<dependencies>
```

# Build
- Scala is build by Maven
- Administration frontend is build by [Brunch](https://brunch.io/)
    - build run by ```brunch build```

# Features
- Static site generator
    - dynamic forms are supported too
- Administration is automatically generated
- wysiwyg editor
- file manager
- multiple language support
