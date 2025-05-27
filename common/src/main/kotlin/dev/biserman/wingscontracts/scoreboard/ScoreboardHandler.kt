package dev.biserman.wingscontracts.scoreboard

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.scores.Score
import net.minecraft.world.scores.criteria.ObjectiveCriteria

object ScoreboardHandler {
    const val CONTRACT_SCORE = "${WingsContractsMod.MOD_ID}.contract_score"
    const val CONTRACT_SCORE_PERIODIC = "${WingsContractsMod.MOD_ID}.contract_score_periodic"

    fun init(level: ServerLevel) {
        if (!level.scoreboard.objectives.any { it.name == CONTRACT_SCORE }) {
            level.scoreboard.addObjective(
                CONTRACT_SCORE,
                ObjectiveCriteria.DUMMY,
                Component.translatable("scoreboard.$CONTRACT_SCORE"),
                ObjectiveCriteria.RenderType.INTEGER
            )
        }

        if (!level.scoreboard.objectives.any { it.name == CONTRACT_SCORE_PERIODIC }) {
            level.scoreboard.addObjective(
                CONTRACT_SCORE_PERIODIC,
                ObjectiveCriteria.DUMMY,
                Component.translatable("scoreboard.$CONTRACT_SCORE_PERIODIC"),
                ObjectiveCriteria.RenderType.INTEGER
            )
        }
    }

    fun add(level: ServerLevel, player: Player, amount: Int) {
        val score = level.scoreboard.getOrCreatePlayerScore(
            player.scoreboardName,
            level.scoreboard.getObjective(CONTRACT_SCORE) ?: return
        )
        score.score = score.score + amount

        val periodicScore = level.scoreboard.getOrCreatePlayerScore(
            player.scoreboardName,
            level.scoreboard.getObjective(CONTRACT_SCORE_PERIODIC) ?: return
        )
        periodicScore.score = periodicScore.score + amount
    }

    fun resetPeriodic(level: ServerLevel) {
        val objective = level.scoreboard.getObjective(CONTRACT_SCORE_PERIODIC)
        level.scoreboard.playerScores.forEach { kvp ->
            kvp.value.remove(objective)
        }
    }
}