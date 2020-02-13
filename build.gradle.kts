//import edu.sc.seis.tasks.XSLT

plugins {
  "project-report"
  kotlin("jvm") version "1.3.61"
  id("edu.sc.seis.version-class") version "1.1.1"
  "java"
  eclipse
  "project-report"
  "maven-publish"
  application
}

application {
  mainClassName = "edu.sc.seis.sod.Start"
  applicationName = "sod"
}

group = "edu.sc.seis"
version = "4.0.0-SNAPSHOT-WEBSTATUS"
// also remember to change in sod.Version class


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

sourceSets {
  create("relax") {
        resources {
            srcDir(File(project.buildDir, "generated-src/relax/resources"))
        }
    }
}

dependencies {
    implementation("edu.sc.seis:seedCodec:1.0.11")
    implementation("edu.sc.seis:seisFile:1.8.0") {
      // we need seisFile for sac output, but not all the other functionality
      exclude("com.martiansoftware:jsap")
      exclude("org.rxtx:rxtx")
      exclude("org.codehaus.woodstox:woodstox-core-lgpl")
      exclude("net.java.dev.msv:msv-core")
      exclude("org.apache.httpcomponents:httpclient")
      exclude("mysql:mysql-connector-java")
    }
    implementation("edu.sc.seis:sod-mock:4.0.0-SNAPSHOT")
    implementation("edu.sc.seis:sod-model:4.0.0-SNAPSHOT")
    implementation("edu.sc.seis:sod-util:4.0.0-SNAPSHOT")
    implementation("edu.sc.seis:sod-bag:4.0.0-SNAPSHOT")
    implementation("edu.sc.seis:TauP:2.4.4")
    implementation("com.oregondsp.signalprocessing:oregondsp:2011")


    implementation("org.hsqldb:hsqldb:2.4.0")
    implementation("jline:jline:0.9.94")
    implementation("com.martiansoftware:jsap:2.1")
    implementation("thaiopensource:jing:20091111")
    implementation("rngconvUSC:rngconv:20030225")
    implementation( "com.fasterxml.woodstox:woodstox-core:6.0.3")
    implementation("org.eclipse.jetty:jetty-servlet:9.4.5+")
    implementation("org.msgpack:msgpack-core:0.8.13+")
    //implementation("javax.xml:jaxp-api:1.4.2")

    implementation("org.json:json:20170516")

    implementation("org.hibernate:hibernate-ehcache:5.4.11.Final")
    implementation( "org.hibernate:hibernate-core:5.4.11.Final")
    implementation( "org.hibernate:hibernate-c3p0:5.4.11.Final")

    implementation("org.apache.velocity:velocity-tools:2.0") {
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
//    runtime "org.codehaus.groovy:groovy-all:2.2.2"
//    runtime "org.jruby:jruby:1.7.3"
//
    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("edu.sc.seis:sod-mock")).with(project(":sod-mock"))
        substitute(module("edu.sc.seis:sod-model")).with(project(":sod-model"))
        substitute(module("edu.sc.seis:sod-bag")).with(project(":sod-bag"))
        substitute(module("edu.sc.seis:sod-util")).with(project(":sod-util"))
        substitute(module("edu.sc.seis:seisFile")).with(project(":seisFile"))
        substitute(module("edu.sc.seis:seedCodec")).with(project(":seedCodec"))
        substitute(module("edu.sc.seis:TauP")).with(project(":TauP"))
    }
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven(url = "http://www.seis.sc.edu/software/maven2")
}

val dirName = project.name+"-"+version

val binDistFiles = copySpec {

    from("build/output") {
        fileMode=755
        include("bin/**")
    }
    from("build/output") {
        include("lib/**")
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
        fileMode=755
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
    dependsOn("makeScript")
    dependsOn("buildSchema")
    dependsOn("buildGrouperSchema")
    group = "dist"
    with(binDistFiles)
    into(file("$buildDir/explode"))
}
//explodeBin.doLast({ ant.chmod(dir: "$buildDir/explode/bin", perm: "755", includes: "*") })

tasks.register<Sync>("explodeDist") {
  dependsOn("explodeBin")
  dependsOn("doc")
    group = "dist"
    with(distFiles)
    into(file("$buildDir/explode"))
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
    "find_responses" to "edu.sc.seis.sod.tools.find_responses"
    //"sod" to  "edu.sc.seis.sod.Start"
)
for (key in scriptNames.keys) {
  tasks.register<CreateStartScripts>(key) {
    outputDir = file("build/scripts")
    mainClassName = scriptNames[key]
    applicationName = key
    classpath = sourceSets["main"].runtimeClasspath + project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files
  }
  tasks.named("createRunScripts") {
      dependsOn(key)
  }
}




tasks.register<Sync>("copySodSite") {
    dependsOn("buildSchemaDocs")
    dependsOn(":seiswww:makeSodSite")
    group = "dist"
    from(project.file("../seiswww/build/sod/"))
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
    val inRNGFile = File(project.buildDir.path, "generated-src/relaxInclude/sod.rng")
//    inRNGFile = new File(project.buildDir, "generated-src/relax/resources/edu/sc/seis/sod/data/sod.rng")
//    jvmArgs = ["-agentlib:yjpagent", "-XX:+HeapDumpOnOutOfMemoryError", "-Xmx512m"]
    jvmArgs = listOf("-Xmx512m")
    group = "dist"
//   inputs.files "site/elementPage.vm", inRNGFile.getParentFile()
    inputs.dir(inRNGFile.getParentFile())
//    outDir = project.file("build/velocity/sod/ingredients")
    val outDir = File(project.buildDir, "generated-src/velocity/sod/ingredients")
    outputs.dir(outDir)
    workingDir = outDir
    args = listOf(inRNGFile.path, project.projectDir.path, outDir.path)
    classpath(configurations.runtime)
    classpath(project.file("build/classes/main"))
    main="edu.sc.seis.sod.validator.documenter.SchemaDocumenter"
    doFirst {
        if (!outDir.exists() ) {
            outDir.mkdirs()
        }
    }
}

tasks.register<JavaExec>("buildSchema") {
    dependsOn("transform")
    dependsOn("compileJava")
    jvmArgs = listOf("-Xmx1512m")
    group = "build"
    val inFile = File(project.buildDir.path, "generated-src/relaxInclude/sod.rng")
    inputs.dir(inFile.getParentFile())
    val resourcesDir = File(project.buildDir, "generated-src/relax/resources")
    val outFile = File(resourcesDir, "edu/sc/seis/sod/data/sod.rng")
    outputs.files(outFile)
    doFirst {outFile.getParentFile().mkdirs()}
    args = listOf("-s ", inFile.path)
    main = "com.thaiopensource.relaxng.util.Driver"
    workingDir = outFile.getParentFile()
}

tasks.register<JavaExec>("buildGrouperSchema") {
    dependsOn("transformGrouper")
    dependsOn("compileJava")
    group = "build"
    val inFile = File(project.buildDir.path, "generated-src/relaxInclude/grouper.rng")
    inputs.dir(inFile.getParentFile())
    val resourcesDir = File(project.buildDir, "generated-src/relax/resources")
    val outFile = File(resourcesDir, "edu/sc/seis/sod/data/grouper.rng")
    outputs.files(outFile)
    doFirst {outFile.getParentFile().mkdirs()}
    args = listOf("-s ", inFile.path)
    jvmArgs = listOf("-Xmx1512m")
    main = "com.thaiopensource.relaxng.util.Driver"
    workingDir = outFile.getParentFile()
}
/*
tasks.register<XSLT>("transform") {
    destDir= File(project.buildDir, "generated-src/relaxInclude")
    stylesheetFile = File(project.projectDir, "src/main/xslt/simpleXInclude.xslt")
    val rngFile = File(project.projectDir, "src/main/relax/sod.rng")
    source = rngFile
    inputs.dir(rngFile.getParentFile())
    outputs.dir(destDir)
}

tasks.register<XSLT>("transformGrouper") {
    destDir= File(project.buildDir, "generated-src/relaxInclude")
    stylesheetFile = File(project.projectDir, "src/main/xslt/simpleXInclude.xslt")
    val rngFile = File(project.projectDir, "src/main/relax/grouper.rng")
    source = rngFile
    inputs.dir(rngFile.getParentFile())
    outputs.dir(destDir)
}
 */


//doc.dependsOn(copySodSite)
//assemble.dependsOn(tarDist)
//assemble.dependsOn(zipDist)
//processResources.dependsOn(buildSchema)
//processResources.dependsOn(buildGrouperSchema)
//processResources{ from(project.sourceSets.main.resources)
//                  from(buildSchema.resourcesDir)}
