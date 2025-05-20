package dev.biserman.wingscontracts.scoreboard

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.scores.criteria.ObjectiveCriteria

object ScoreboardHandler {
    const val SCOREBOARD_NAME = "${WingsContractsMod.MOD_ID}.contract_score"

    fun init(level: ServerLevel) {
        if (!level.scoreboard.objectives.any { it.name == SCOREBOARD_NAME }) {
            level.scoreboard.addObjective(
                SCOREBOARD_NAME, ObjectiveCriteria.DUMMY, Component.translatable("scoreboard.$SCOREBOARD_NAME"),
                ObjectiveCriteria.RenderType.INTEGER
            )
        }
    }

    fun add(level: ServerLevel, player: Player, amount: Int) {
        val score = level.scoreboard.getOrCreatePlayerScore(
            player.scoreboardName,
            level.scoreboard.getObjective(SCOREBOARD_NAME)!!
        )
        score.score = score.score + amount
    }
}