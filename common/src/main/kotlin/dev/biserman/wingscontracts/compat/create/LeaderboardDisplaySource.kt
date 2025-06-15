package dev.biserman.wingscontracts.compat.create

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext
import com.simibubi.create.content.redstone.displayLink.source.ValueListDisplaySource
import dev.biserman.wingscontracts.WingsContractsMod
import net.createmod.catnip.data.IntAttached
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.stream.Stream


class LeaderboardDisplaySource(val objectiveString: String, val langKey: String) : ValueListDisplaySource() {
    override fun provideEntries(context: DisplayLinkContext, maxRows: Int): Stream<IntAttached<MutableComponent>> {
        val scoreboard = context.level().scoreboard
        val objective = scoreboard.getObjective(objectiveString) ?: return Stream.empty()
        val scores = scoreboard.listPlayerScores(objective)

        return scores.mapIndexed { index, score ->
            IntAttached.with(
                score.value,
                Component.literal("${index + 1}. ${score.owner}")
            )
        }.stream()
    }

    override fun getName(): MutableComponent =
        Component.translatable("block.${WingsContractsMod.MOD_ID}.contract_portal.display_source.$langKey")

    override fun valueFirst(): Boolean {
        return false
    }
}