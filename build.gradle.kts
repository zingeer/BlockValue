plugins {
    kotlin("jvm") version "1.5.0"
}

repositories {
    jcenter()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://mvnrepository.com/artifact/io.netty/netty-buffer")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.5.0")
    implementation("com.github.rqbik", "bukkt", "1.0.3")
    implementation("io.netty", "netty-buffer", "4.1.65.Final")
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileJava {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.encoding = "UTF-8"
    }
    jar {
        //val out = project.findProperty("out")?.toString() ?: "testServer/plugins"
        //destinationDirectory.set(File("$rootDir/$out"))
        doFirst {
            from({
                configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
            })
        }
        exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}