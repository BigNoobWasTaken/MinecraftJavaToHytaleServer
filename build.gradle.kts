plugins {
// Uncomment if you are using IntelliJ.
    idea
    java
    id("com.azuredoom.hytale-tools") version "1.+"
    id("io.freefair.lombok") version "9.+"
}


tasks.withType<Javadoc>().configureEach {
    (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

group = project.property("group").toString()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(property("java_version").toString().toInt()))
}

hytaleTools {
    javaVersion = property("java_version").toString().toInt()
    hytaleVersion = property("hytale_version").toString()
    manifestServerVersion = property("manifestServerVersion").toString()
    manifestGroup = property("manifest_group").toString()
    modId = property("mod_id").toString()
    modDescription = property("mod_description").toString()
    modUrl = property("mod_url").toString()
    mainClass = property("main_class").toString()
    modCredits = property("mod_author").toString()
    manifestDependencies = property("manifest_dependencies").toString()
    manifestOptionalDependencies = property("manifest_opt_dependencies").toString()
    curseforgeId = property("curseforgeID").toString()
    disabledByDefault = property("disabled_by_default").toString().toBoolean()
    includesPack = property("includes_pack").toString().toBoolean()
    patchline = property("patchline").toString()
    injectServerJavadocsIntoSources = property("injectServerJavadocsIntoSources").toString().toBoolean()
    generateAssetsBinary = property("generateAssetsBinary").toString().toBoolean()
    //hytaleHomeOverride = property("hytaleHomeOverride").toString()
}

repositories {
    mavenCentral()
    maven {
        name = "open collab Repository Maven Releases"
        url = uri("https://repo.opencollab.dev/maven-releases")
    }
    maven {
        name = "open collab Repository Maven Snapshots"
        url = uri("https://repo.opencollab.dev/maven-snapshots")
    }
    maven {
        name = "ViaVersion"
        url = uri("https://repo.viaversion.com")
    }
}

dependencies {
    implementation("org.geysermc.mcprotocollib:protocol:1.21.11-1") {
        exclude("io.netty", "*")
    }
    implementation("com.viaversion:viaversion-api:5.12.2")
    implementation("com.viaversion:viaversion-common:5.12.2")
    implementation("com.viaversion:viabackwards-common:5.12.2")
    implementation("com.google.guava:guava:33.8.0-jre")
}

tasks.named<Jar>("jar") {
    archiveBaseName.set(project.property("mod_name").toString())
    archiveVersion.set(project.property("version").toString())
}

// Uncomment if you are using IntelliJ.
 idea {
     module {
         isDownloadSources = true
         isDownloadJavadoc = true
     }
 }
