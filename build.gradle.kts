import java.nio.file.Paths

plugins {
 // kotlin("jvm") version "1.9.0"
  id("edu.sc.seis.version-class") version "1.4.1"
  "java"
  eclipse
  "project-report"
  `maven-publish`
  application
  id("com.github.ben-manes.versions") version "0.53.0"
}

// dumb to keep intellij from crashing...
tasks.register("prepareKotlinBuildScriptModel"){}

application {
    mainClass.set("edu.sc.seis.sod.Start")
  //applicationName = "sod"
}

group = "edu.sc.seis"
version = "4.0.0rc2"
// also remember to change in sod.Version class
// site/velocity/VM_library.vm
// site/velocity/previousReleases.vm


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

tasks.register("versionToVersionFile") {
  inputs.files("build.gradle.kts")
  outputs.files("VERSION")
  File("VERSION").writeText(""+version)
}

sourceSets {
  create("relax") {
        resources {
            srcDir(layout.buildDirectory.dir("generated-src/relax/resources"))
        }
    }
}

val rng by configurations.creating

dependencies {
    rng("org.relaxng:jing:20181222")
    implementation("edu.sc.seis:seedCodec:1.2.0")
    implementation("edu.sc.seis:seisFile:2.3.4")
    implementation("info.picocli:picocli:4.7.6")
    implementation("edu.sc.seis:TauP:3.1.0")
    implementation("com.isti:isti.util:20120201")
    implementation("com.oregondsp.signalprocessing:oregondsp:1.0.1-alpha")


    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-reload4j:1.7.36")
    implementation("org.hsqldb:hsqldb:2.7.4")
    implementation("jline:jline:0.9.94")
    implementation("com.martiansoftware:jsap:2.1")
    implementation("thaiopensource:jing:20091111")
    implementation("rngconvUSC:rngconv:20030225")
    implementation( "com.fasterxml.woodstox:woodstox-core:6.5.1")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.5+")
    implementation("org.msgpack:msgpack-core:0.9.11")
    //implementation("javax.xml:jaxp-api:1.4.2")

    implementation("org.json:json:20251224")

    implementation("org.hibernate:hibernate-ehcache:5.6.15.Final")
    implementation( "org.hibernate:hibernate-core:5.6.15.Final")
    implementation( "org.hibernate:hibernate-c3p0:5.6.15.Final")

    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1") {
        exclude("xml-apis:xml-apis")
        exclude("org.apache.struts:struts-taglib")
        exclude("org.apache.struts:struts-tiles")
        exclude("org.apache.struts:struts-core")
        exclude("javax.servlet:servlet-api")
    }


    //compile "xerces:xercesImpl:2.11.0"
    //implementation("xalan:xalan:2.7.1")

    implementation("edu.sc.seis.mapData:dcwpo_browse:1.0")
    implementation("net.sourceforge.javacsv:javacsv:2.0")

    // Use JUnit Jupiter API for testing.
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")}

configurations.all {
    resolutionStrategy.dependencySubstitution {
//        substitute(module("edu.sc.seis:seisFile")).using(project(":seisFile"))
//        substitute(module("edu.sc.seis:seedCodec")).using(project(":seedCodec"))
//        substitute(module("edu.sc.seis:TauP")).using(project(":TauP"))
    }
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    maven {
	    url = uri("https://www.seis.sc.edu/software/maven2/")
	    isAllowInsecureProtocol = true
    }
    mavenLocal()
}


tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}


val dirName = project.name+"-"+version


distributions {
  main {
    distributionBaseName = "SOD"
    contents {
      from("scripts") {
          include("cwg.prop")
      }
      from(".") {
          include("etc/**")
          include("gpl-3.0.txt")
          include("defaultProps")
          //include("externalExample/**")
          include("src/**")
          include("lib/**")
          include("gradle/**")
          include("gradlew")
          include("gradlew.bat")
          exclude("**/*.svn")
      }
      from("scripts") {
          include("tutorial/**")
          include("CMTReadySeismograms.xml")
          include("ammonChannels.xml")
          include("breqfast.xml")
          include("dmcWebService.xml")
          include("preferBroadband.xml")
          include("realtime.xml")
          include("sAndSKSInTanzania.xml")
          include("southAmericanSacFiles.xml")
          include("variableDataWindow.xml")
          include("vector.xml")
          include("weed.xml")
          include("winston.xml")
          into("recipes")
      }
      from("build") {
          //include("doc/**")
          include("build.gradle")
          include("settings.gradle")
      }
      //from("build/velocity/sod") {
      //  include("ingredients")
      //  into("doc")
      //}
      //from("build/docs") {
      //    include("javadoc/**")
      //    into("doc/documentation")
      //}
      from("site/doc") {
        into("doc")
      }
      from("build/generated-src/modVersion") {
        include("java/**")
        into("src/main")
      }
    }
  }
}

val binDistFiles = copySpec {
    from(configurations.runtimeClasspath) {
      include("*")
      into("lib")
    }
    from("build/scripts") {
        include("**")
        into("bin")
    }
    from("build/libs") {
        include("**")
        into("lib")
    }
    from("scripts") {
        include("cwg.prop")
    }
    from(".") {
        include("etc/**")
    }
}

val distFiles = copySpec {
    with(binDistFiles)
    from(".") {
        include("gradlew")
        include("gradlew.bat")
    }
    from(".") {
        include("gpl-3.0.txt")
        include("defaultProps")
        include("externalExample/**")
        include("src/**")
        include("lib/**")
        include("gradle/**")
        exclude("**/*.svn")
    }
    from("scripts") {
        include("tutorial/**")
        include("CMTReadySeismograms.xml")
        include("ammonChannels.xml")
        include("breqfast.xml")
        include("dmcWebService.xml")
        include("preferBroadband.xml")
        include("realtime.xml")
        include("sAndSKSInTanzania.xml")
        include("southAmericanSacFiles.xml")
        include("recfunc_ears.xml")
        include("variableDataWindow.xml")
        include("vector.xml")
        include("weed.xml")
        include("winston.xml")
        into("recipes")
    }
    from("build") {
        include("doc/**")
        include("build.gradle")
        include("settings.gradle")
    }
    from("build/docs") {
        include("javadoc/**")
        into("doc/documentation")
    }
}

tasks.register<Tar>("tarBin") {
    dependsOn("explodeBin")
    group = "dist"
    compression = Compression.GZIP
    into(dirName) {
        with(binDistFiles)
    }
}


tasks.register<Zip>("zipDist") {
    dependsOn("explodeDist")
    group = "dist"
    into(dirName) {
        with(distFiles)
    }
}

tasks.register<Tar>("tarDist") {
    dependsOn("explodeDist")
    group = "dist"
    compression = Compression.GZIP
    into(dirName) {
        with(distFiles)
    }
}

tasks.register<Sync>("explodeBin") {
    dependsOn("startScripts")
    dependsOn("createRunScripts")
    dependsOn("buildSchema")
    dependsOn("buildGrouperSchema")
    dependsOn("jar")
    group = "dist"
    with(binDistFiles)
    into(layout.buildDirectory.dir("explode"))
}
//explodeBin.doLast({ ant.chmod(dir: "$buildDir/explode/bin", perm: "755", includes: "*") })

tasks.register<Sync>("explodeDist") {
  dependsOn("explodeBin")
  dependsOn("buildSchemaDocs")
    group = "dist"
    with(distFiles)
    into(layout.buildDirectory.dir("explode"))
}
/*
// not needed any more???
fun getDExtras(): Map<String,String> {
    return mapOf( "seis.name" to "sod"+getVersion(),
                "python.path" to "${LIB}",
                "java.awt.headless" to "true",
                "swing.volatileImageBufferEnabled" to "false" )
} */

tasks.register("createRunScripts"){}
tasks.named("startScripts") {
    dependsOn("createRunScripts")
}

val scriptNames = mapOf(
    "find_events" to "edu.sc.seis.sod.tools.find_events",
    "find_stations" to "edu.sc.seis.sod.tools.find_stations",
    "find_channels" to "edu.sc.seis.sod.tools.find_channels",
    "find_seismograms" to "edu.sc.seis.sod.tools.find_seismograms",
    "find_responses" to "edu.sc.seis.sod.tools.find_responses",
    "schemadocs" to "edu.sc.seis.sod.validator.documenter.SchemaDocumenter",
    "sod" to  "edu.sc.seis.sod.Start"
)
for (key in scriptNames.keys) {
  tasks.register<CreateStartScripts>(key) {
    outputDir = file("build/scripts")
      getMainClass().set(scriptNames[key])
    applicationName = key
    classpath = sourceSets["main"].runtimeClasspath + project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files
  }
  tasks.named("createRunScripts") {
      dependsOn(key)
  }
}



tasks.register<JavaExec>("makeSodSite") {
    dependsOn("buildSchemaDocs")
    dependsOn("compileJava")
    val inVmFile = File(project.projectDir.path, "site/velocity/index.vm")
    jvmArgs = listOf("-Xmx512m")
    group = "dist"
//   inputs.files "site/elementPage.vm", inRNGFile.getParentFile()
    inputs.dir(inVmFile.getParentFile())
//    outDir = project.file("build/velocity/sod/ingredients")
    val outDir = layout.buildDirectory.dir("generated-src/velocity/sod/site")
    outputs.dir(outDir)
    workingDir = File(project.projectDir, "site")
    classpath(sourceSets.getByName("main").runtimeClasspath)
    classpath(project.file("build/classes/main"))
    getMainClass().set("edu.sc.seis.sod.validator.documenter.SchemaDocumenter")
    doFirst {
        val myoutDir = file(layout.buildDirectory.dir("generated-src/velocity/sod/site"))
        if (!myoutDir.exists() ) {
            myoutDir.mkdirs()
        }
        args = listOf(inVmFile.path, ".", myoutDir.path)
    }
}

tasks.register<Sync>("copySodSite") {
    dependsOn("buildSchemaDocs")
    //dependsOn("makeSodSite")
    group = "dist"
    from(project.file("site/www/"))
    from(tasks.get("buildSchemaDocs")) {
      into("ingredients")
    }
    into(project.file("build/doc/"))
}

tasks.register<Copy>("staticSchemaDocs") {
    group = "dist"
    val outDir = project.file("build/velocity/sod/ingredients/")
    from(project.file("site/schemaDocs/docs"))
    into(outDir)
    doFirst {
        outDir.mkdirs()
    }
}

tasks.register<JavaExec>("buildSchemaDocs") {
    dependsOn("staticSchemaDocs")
    dependsOn("buildSchema")
    dependsOn("compileJava")
    val inRNGFile = layout.buildDirectory.dir("generated-src/relaxInclude/sod.rng")
//    inRNGFile = new File(project.buildDir, "generated-src/relax/resources/edu/sc/seis/sod/data/sod.rng")
//    jvmArgs = ["-agentlib:yjpagent", "-XX:+HeapDumpOnOutOfMemoryError", "-Xmx512m"]
    jvmArgs = listOf("-Xmx512m")
    group = "dist"
//   inputs.files "site/elementPage.vm", inRNGFile.getParentFile()
    inputs.dir(file(inRNGFile).getParentFile())
//    outDir = project.file("build/velocity/sod/ingredients")
    val outDir = layout.buildDirectory.dir("generated-src/velocity/sod/ingredients")
    outputs.dir(outDir)
    workingDir = File(project.projectDir, "site")
    classpath(sourceSets.getByName("main").runtimeClasspath)
    classpath(project.file("build/classes/main"))
    getMainClass().set("edu.sc.seis.sod.validator.documenter.SchemaDocumenter")
    doFirst {
        if (!file(outDir).exists() ) {
            file(outDir).mkdirs()
        }
        args = listOf(file(inRNGFile).path, ".", file(outDir).path)
    }
}

tasks.register<JavaExec>("buildSchema") {
    dependsOn("transform")
    dependsOn("compileJava")
    classpath(configurations.getByName("rng"))
    jvmArgs = listOf("-Xmx1512m")
    group = "build"
    val inFile = file(layout.buildDirectory.dir("generated-src/relaxInclude/sod.rng"))
    inputs.dir(inFile.getParentFile())
    val resourcesDir = layout.buildDirectory.dir("generated-src/relax/resources")
    val outFile =  layout.buildDirectory.dir("generated-src/relax/resources/edu/sc/seis/sod/data/sod.rng")
    outputs.files(outFile)


    val rngOutput = StringBuilder()
    logging.addStandardOutputListener { rngOutput.append(it) }
    doLast {
	var rngOutText = rngOutput.toString()
	if (rngOutText.startsWith(":sod:buildSchema", ignoreCase=false)) {
		rngOutText= rngOutText.substring(17)
	}
        file(outFile).writeText(rngOutText.toString())
    }

    doFirst {
      file(outFile).getParentFile().mkdirs()
    }
    args = listOf("-s", inFile.path)
    getMainClass().set("com.thaiopensource.relaxng.util.Driver")
    workingDir = file(outFile).getParentFile()
}

tasks.register<JavaExec>("buildGrouperSchema") {
    dependsOn("transformGrouper")
    dependsOn("compileJava")
    classpath(configurations.getByName("rng"))
    group = "build"
    val inFile = file(layout.buildDirectory.dir("generated-src/grouperRelaxInclude/grouper.rng"))
    inputs.dir(inFile.getParentFile())
    val resourcesDir = layout.buildDirectory.dir("generated-src/relax/resources")
    val outFile = layout.buildDirectory.dir("generated-src/relax/resources/edu/sc/seis/sod/data/grouper.rng")
    outputs.files(outFile)
    doFirst {
      file(outFile).getParentFile().mkdirs()
    }

    val rngOutput = StringBuilder()
    logging.addStandardOutputListener { rngOutput.append(it) }
    doLast {
        file(outFile).writeText(rngOutput.toString())
	var rngOutText = rngOutput.toString()
	if (rngOutText.startsWith(":sod:buildGrouperSchema")) {
		rngOutText= rngOutText.substring(24)
	}
        file(outFile).writeText(rngOutText.toString())
    }

    args = listOf("-s", inFile.path)
    jvmArgs = listOf("-Xmx1512m")
    getMainClass().set("com.thaiopensource.relaxng.util.Driver")
    workingDir = file(outFile).getParentFile()
}


tasks.register<edu.sc.seis.tasks.XSLT>("transform") {
    destDir= file(layout.buildDirectory.dir("generated-src/relaxInclude"))
    stylesheetFile = File(project.projectDir, "src/main/xslt/simpleXInclude.xslt")
    val rngFile = File(project.projectDir, "src/main/relax/sod.rng")
    source(rngFile)
    inputs.dir(rngFile.getParentFile())
    outputs.dir(destDir)
}

tasks.register<edu.sc.seis.tasks.XSLT>("transformGrouper") {
    destDir= file(layout.buildDirectory.dir("generated-src/grouperRelaxInclude"))
    stylesheetFile = project.file("src/main/xslt/simpleXInclude.xslt")
    val rngFile = project.fileTree("src") {
      include("main/relax/grouper.rng")
    }
    source = rngFile
    inputs.file(stylesheetFile)
    outputs.dir(destDir)
}


tasks.named("sourcesJar") {
    dependsOn("makeVersionClass")
}

tasks.named("makeVersionClass") {
  inputs.files("src/main/")
  inputs.files("build.gradle.kts")
}



//doc.dependsOn(copySodSite)
//assemble.dependsOn(tarDist)
//assemble.dependsOn(zipDist)
tasks.processResources {
    dependsOn("buildSchema")
    dependsOn("buildGrouperSchema")
    from(tasks.named<JavaExec>("buildSchema").get().outputs) {
      into("edu/sc/seis/sod/data")
    }
    from(tasks.named<JavaExec>("buildGrouperSchema").get().outputs) {
      into("edu/sc/seis/sod/data")
    }
}
//processResources.dependsOn(buildGrouperSchema)
//tasks.processResources{ from(project.sourceSets.main.resources)
//                  from(tasks.get("buildSchema").resourcesDir)}
