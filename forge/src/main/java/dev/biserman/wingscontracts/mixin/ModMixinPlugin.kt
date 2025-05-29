package dev.biserman.wingscontracts.mixin

import net.minecraftforge.fml.loading.LoadingModList
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class ModMixinPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String) {}
    override fun getRefMapperConfig() = null
    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        if (mixinClassName == "dev.biserman.wingscontracts.mixin.PortalGoggleInformationMixin") {
            return LoadingModList.get().mods.any { it.modId == "create" }
        }

        return true
    }

    override fun acceptTargets(
        targetClassName: Set<String>,
        mixinClassName: Set<String>
    ) {
    }

    override fun getMixins(): List<String> = listOf()
    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo
    ) {
    }
}