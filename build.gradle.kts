import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

val pluginId: String by project
val pluginName: String by project
val pluginGroup: String by project
val pluginLicense: String by project
val pluginUrl: String by project
val pluginVersion: String by project
val pluginAuthors: String by project
val pluginDescription: String by project

plugins {
    java
    eclipse
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

group = pluginGroup
version = pluginVersion

repositories {
    maven() {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    maven() {
        url = uri("https://maven.aliyun.com/repository/spring/")
    }
    maven("papermc-repo") {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("sonatype") {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven("jitpack") {
        url = uri("https://jitpack.io")
    }
    mavenCentral()
}

dependencies {
    implementation("com.github.dsx137:jable:1.0.4")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to pluginName,
            "Implementation-Version" to pluginVersion
        )
    }
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates by tasks.registering(Copy::class) {
    val props = mapOf(
        "plugin_id" to pluginId,
        "plugin_name" to pluginName,
        "plugin_group" to pluginGroup,
        "plugin_version" to pluginVersion,
        "plugin_authors" to pluginAuthors,
        "plugin_url" to pluginUrl,
        "plugin_description" to pluginDescription
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

// 将生成的模板文件夹添加到源文件夹中
sourceSets["main"].java.srcDirs(generateTemplates.map { it.destinationDir })

// 当Gradle项目的构建文件有更改并重新导入到IDE中时自动运行generateTemplates任务
rootProject.idea.project.settings.taskTriggers.afterSync(generateTemplates)
project.eclipse.synchronizationTasks(generateTemplates)

tasks {
    shadowJar {
        minimize()
    }
}