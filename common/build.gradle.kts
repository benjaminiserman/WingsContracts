repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    maven("https://squiddev.cc/maven/")
}

architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/wingscontracts.accesswidener"))
    mixin.useLegacyMixinAp = false
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")
    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-common:${rootProject.property("forgeconfigapiport_version")}")
    //if (rootProject.property("cc_tweaked_enable") == "true") {
        val ccTweakedMinecraftVersion = rootProject.property("cc_tweaked_minecraft_version")
        val ccTweakedVersion = rootProject.property("cc_tweaked_version")
        compileOnly("cc.tweaked:cc-tweaked-$ccTweakedMinecraftVersion-core-api:$ccTweakedVersion")
        compileOnly("cc.tweaked:cc-tweaked-$ccTweakedMinecraftVersion-common-api:$ccTweakedVersion")
    //}
    implementation(kotlin("reflect"))
}