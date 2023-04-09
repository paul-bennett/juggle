plugins {
    application
    jacoco
}

val friendlyName = "Juggle"
group = "com.angellane"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("args4j", "args4j", "2.33")

    testImplementation("org.junit.jupiter", "junit-jupiter-api",    "5.9.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.9.2")
}

// I would ideally like to specify the Java version using Gradle's toolchain
// feature, but it's a bit tangled up with respect to my testInput projects
// -- they don't seem to pick up the toolchain settings.
//
// Using the old method of setting target & source compatibility instead.

//java {
//    toolchain {
//        // Java 12 required for java.lang.Class.arrayType(), supporting passing arrays as -p and -r options
//        languageVersion.set(JavaLanguageVersion.of(12))
//    }
//}

tasks.withType<JavaCompile> {
    targetCompatibility = "12"
    sourceCompatibility = "12"
}

application {
    mainClass.set("com.angellane.juggle.Main")
}

afterEvaluate {
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
        attributes["Implementation-Title"] = friendlyName
        attributes["Implementation-Version"] = version
    }

    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    dependsOn(tasks.jar)    // One of the tests in README.md uses the built JAR file
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}


// This next section configures a couple of tasks to generate JAR files that are used as input to some
// of the tests.  There's no need to invoke the tasks explicitly -- we hook them into test task too.
//
// I'm not a Gradle expert, so this looks/feels clumsier than strictly necessary.
// I'm sure there's a better way of doing this.

sourceSets {
    create("testLib").java.setSrcDirs(listOf("src/test-input/java"))
    create("testApp").java.setSrcDirs(listOf("src/test-input/java"))
}

tasks.register<JavaCompile>("testLibCompile") {
    val srcName = name.removeSuffix("Compile")
    classpath = sourceSets.named(srcName).get().runtimeClasspath
    destinationDirectory.set(File(project.buildDir.toString() + "/classes/" + srcName))
    include("**/" + srcName.removePrefix("test") + ".java")
    source = sourceSets.named(srcName).get().java
}

tasks.register<JavaCompile>("testAppCompile") {
    val srcName = name.removeSuffix("Compile")
    classpath = sourceSets.named(srcName.removeSuffix("App") + "Lib").get().runtimeClasspath
    destinationDirectory.set(File(project.buildDir.toString() + "/classes/" + srcName))
    include("**/" + srcName.removePrefix("test") + ".java")
    source = sourceSets.named(srcName).get().java
}

tasks.register<Jar>("testLibJar") {
    val srcName = name.removeSuffix("Jar")
    val compileTaskName = srcName + "Compile"

    dependsOn(compileTaskName)

    from(tasks.named(compileTaskName).get().outputs)

    archiveBaseName.set(srcName)
    archiveVersion.set("")
}

tasks.register<Jar>("testAppJar") {

    val srcName = name.removeSuffix("Jar")
    val compileTaskName = srcName + "Compile"

    dependsOn(compileTaskName)

    from(tasks.named(compileTaskName).get().outputs)

    archiveBaseName.set(srcName)
    archiveVersion.set("")
}

tasks.named("test") {
    dependsOn("testAppJar", "testLibJar")
}
