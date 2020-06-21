import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4-M1"
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.genanik"
version = "2.1.7"

repositories {
    maven( url = "https://dl.bintray.com/kotlin/kotlin-eap" )
    maven( url = "https://maven.aliyun.com/nexus/content/repositories/jcenter" )
    maven( url = "https://dl.bintray.com/him188moe/mirai" )
    maven( url = "https://mirrors.huaweicloud.com/repository/maven" )
    jcenter()
    mavenCentral()
}

val miraiCoreVersion = "1.0.2"
val miraiConsoleVersion = "0.5.2"

dependencies {
    compileOnly("com.github.houbb", "opencc4j", "1.4.0")
    compileOnly("net.java.dev.jna", "jna", "5.5.0")

    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")

    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("net.mamoe:mirai-core:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-core-qqandroid:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-console:$miraiConsoleVersion")
    testImplementation("com.madgag", "animated-gif-lib","1.4")
    testImplementation("com.github.houbb", "opencc4j", "1.4.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val runMiraiConsole by creating(JavaExec::class.java) {
        group = "mirai"
        dependsOn(shadowJar)
        dependsOn(testClasses)

        val testConsoleDir = "test"

        doFirst {
            fun removeOldVersions() {
                File("$testConsoleDir/plugins/").walk()
                    .filter { it.name.matches(Regex("""${project.name}-.*-all.jar""")) }
                    .forEach {
                        it.delete()
                        println("deleting old files: ${it.name}")
                    }
            }

            fun copyBuildOutput() {
                File("build/libs/").walk()
                    .filter { it.name.contains("-all") }
                    .maxBy { it.lastModified() }
                    ?.let {
                        println("Coping ${it.name}")
                        it.inputStream()
                            .transferTo(File("$testConsoleDir/plugins/${it.name}").apply { createNewFile() }
                                .outputStream())
                        println("Copied ${it.name}")
                    }
            }

            workingDir = File(testConsoleDir)
            workingDir.mkdir()
            File(workingDir, "plugins").mkdir()
            removeOldVersions()
            copyBuildOutput()

            classpath = sourceSets["test"].runtimeClasspath
            main = "mirai.RunMirai"
            standardInput = System.`in`
            args(miraiCoreVersion, miraiConsoleVersion)
        }
    }
}