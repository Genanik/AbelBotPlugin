plugins {
    kotlin("jvm") version "1.4-M1"
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.genanik"
version = "1.1.8"

repositories {
    maven(url =  "https://dl.bintray.com/him188moe/mirai")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://mirrors.huaweicloud.com/repository/maven")
    mavenCentral()
    jcenter()
}

val miraiCoreVersion = "0.37.0"
val miraiConsoleVersion = "0.4.9"

dependencies {
    compileOnly("com.madgag", "animated-gif-lib","1.4")
    compileOnly("com.github.houbb", "opencc4j", "1.4.0")
    compileOnly( "org.json", "json", "20190722")

    testImplementation("com.madgag", "animated-gif-lib","1.4")
    testImplementation("com.github.houbb", "opencc4j", "1.4.0")
    testImplementation( "org.json", "json", "20190722")


    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core-jvm:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("net.mamoe:mirai-core-jvm:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-core-qqandroid-jvm:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-console:$miraiConsoleVersion")
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

    val buildAndCopyToPluginsFolder by creating(JavaExec::class.java) {
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
        }
    }
}