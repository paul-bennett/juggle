plugins {
    application
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

java {
    toolchain {
        // Java 12 required for java.lang.Class.arrayType(), supporting passing arrays as -p and -r options
        languageVersion.set(JavaLanguageVersion.of(12))
    }
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
}
