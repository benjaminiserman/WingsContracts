repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    maven("https://squiddev.cc/maven/")
    maven("https://maven.createmod.net")
}

architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/wingscontracts.accesswidener"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")
    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-common:${rootProject.property("forgeconfigapiport_version")}")
    val minecraftVersion = rootProject.property("minecraft_version")
    if (rootProject.property("cc_tweaked_enable") == "true") {
        val ccTweakedVersion = rootProject.property("cc_tweaked_version")
        compileOnly("cc.tweaked:cc-tweaked-$minecraftVersion-core-api:$ccTweakedVersion")
        compileOnly("cc.tweaked:cc-tweaked-$minecraftVersion-common-api:$ccTweakedVersion")
    }
    if (rootProject.property("create_enable") == "true") {
        val createVersion = rootProject.property("create_version")
        val ponderVersion = rootProject.property("ponder_version")
        compileOnly("com.simibubi.create:create-$minecraftVersion:$createVersion:slim") { isTransitive = false }
        compileOnly("net.createmod.ponder:Ponder-Forge-$minecraftVersion:$ponderVersion") { isTransitive = false }
    }

    implementation(kotlin("reflect"))
}