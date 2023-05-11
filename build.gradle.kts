import org.gradle.kotlin.dsl.support.uppercaseFirstChar

plugins {
    application
    jacoco
    antlr
}

val friendlyName = "Juggle"
group = "com.angellane"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr", "antlr4", "4.12.0")
    implementation("org.antlr", "antlr4-runtime", "4.12.0")

    implementation("info.picocli", "picocli", "4.7.2")

    testImplementation("org.junit.jupiter", "junit-jupiter-api",    "5.9.2")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.9.2")
}

java.toolchain {
    // We use a lot of Java 17 features: records, pattern matching, etc.
    languageVersion.set(JavaLanguageVersion.of(17))
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

tasks.generateGrammarSource {
    arguments = arguments + listOf("-package", "com.angellane.juggle.parser")
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
    dependsOn(tasks.test)   // tests are required to run before generating the report
}
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            limit {
                minimum = "0.9".toBigDecimal()
            }
        }
    }
}
// These next two blocks exclude our ANTLR-generated code from the JaCoCo
// report and verification steps.  It's unclear to me why I can't put this
// code in the previous named tasks, but it works here.
tasks.withType<JacocoReport> {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it).apply {
                exclude("com/angellane/juggle/parser/**")
            }
        }))
    }
}
tasks.withType<JacocoCoverageVerification> {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it).apply {
                exclude("com/angellane/juggle/parser/**")
            }
        }))
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


// This next section configures a couple of tasks to generate JAR files that are used as input to some
// of the tests.  There's no need to invoke the tasks explicitly -- we hook them into test task too.
//
// This still feels a bit more complex than I'd have expected. Is there a better way?

var testLib = "testLib"
var testApp = "testApp"

var testLibSrc: SourceSet = sourceSets.create(testLib)
var testAppSrc: SourceSet = sourceSets.create(testApp)

var jarTestLibTaskName = "jar${testLib.uppercaseFirstChar()}"
var jarTestAppTaskName = "jar${testApp.uppercaseFirstChar()}"

tasks.named<JavaCompile>(testAppSrc.compileJavaTaskName) {
    dependsOn(tasks[testLibSrc.compileJavaTaskName])
    classpath = testLibSrc.runtimeClasspath
}

tasks.create<Jar>(jarTestLibTaskName) {
    group = "build"
    description = "Assembles a jar archive containing the $testLib classes"
    dependsOn(tasks[testLibSrc.compileJavaTaskName])

    from(tasks[testLibSrc.compileJavaTaskName].outputs)
    archiveBaseName.set(testLib)
    archiveVersion.set("")
}

tasks.create<Jar>(jarTestAppTaskName) {
    group = "build"
    description = "Assembles a jar archive containing the $testApp classes"

    dependsOn(tasks[testAppSrc.compileJavaTaskName])

    from(tasks[testAppSrc.compileJavaTaskName].outputs)
    archiveBaseName.set(testApp)
    archiveVersion.set("")
}

tasks.named("test") {
    dependsOn(jarTestLibTaskName, jarTestAppTaskName)
}
