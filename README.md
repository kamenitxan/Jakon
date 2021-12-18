[![Java CI](https://github.com/kamenitxan/Jakon/actions/workflows/sbt.yml/badge.svg)](https://github.com/kamenitxan/Jakon/actions/workflows/sbt.yml)
[![codecov](https://codecov.io/gh/kamenitxan/Jakon/branch/master/graph/badge.svg)](https://codecov.io/gh/kamenitxan/Jakon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kamenitxan_Jakon&metric=alert_status)](https://sonarcloud.io/dashboard?id=kamenitxan_Jakon)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=kamenitxan_Jakon&metric=bugs)](https://sonarcloud.io/dashboard?id=kamenitxan_Jakon)

![](https://raw.githubusercontent.com/kamenitxan/Jakon/master/src/main/resources/static/jakon/css/images/logo2.png)

Scala static web generator 

```xml
<repositories>
    <repository>
        <id>kamenitxan-maven-repository</id>
        <url>https://kamenitxans-maven-repository.appspot.com/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>cz.kamenitxan</groupId>
        <artifactId>jakon</artifactId>
        <version>0.2-SNAPSHOT</version>
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