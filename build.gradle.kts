plugins {
    id("java")
}

group = "me.hysong"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

//    implementation(project(":Libraries:KSFoundation"))
//    implementation(project(":Libraries:liblks"))

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set(project.name)
    archiveVersion.set("")
    archiveClassifier.set("")

    destinationDirectory.set(file("./"))

    // 3) Optional: manifest if needed by consumers
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

tasks.named("build") {
    dependsOn(tasks.named("jar"))
}