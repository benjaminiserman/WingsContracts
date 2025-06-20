package dev.biserman.wingscontracts.scoreboard

import dev.biserman.wingscontracts.WingsContractsMod
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.FixedFormat
import net.minecraft.network.chat.numbers.NumberFormat
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
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
                ObjectiveCriteria.RenderType.INTEGER,
                true,
                null
            )
        }

        if (!level.scoreboard.objectives.any { it.name == CONTRACT_SCORE_PERIODIC }) {
            level.scoreboard.addObjective(
                CONTRACT_SCORE_PERIODIC,
                ObjectiveCriteria.DUMMY,
                Component.translatable("scoreboard.$CONTRACT_SCORE_PERIODIC"),
                ObjectiveCriteria.RenderType.INTEGER,
                true,
                null
            )
        }
    }

    fun add(level: ServerLevel, player: Player, amount: Int) {
        level.scoreboard.getOrCreatePlayerScore(
            player,
            level.scoreboard.getObjective(CONTRACT_SCORE) ?: return
        ).add(amount)

        level.scoreboard.getOrCreatePlayerScore(
            player,
            level.scoreboard.getObjective(CONTRACT_SCORE_PERIODIC) ?: return
        ).add(amount)
    }

    fun resetPeriodic(level: ServerLevel) {
        val objective = level.scoreboard.getObjective(CONTRACT_SCORE_PERIODIC) ?: return
        for (player in level.scoreboard.trackedPlayers) {
            level.scoreboard.resetSinglePlayerScore(player, objective)
        }
    }

    fun announceTopScores(level: ServerLevel, count: Int) {
        val objective = level.scoreboard.getObjective(CONTRACT_SCORE_PERIODIC) ?: return
        val topScores = level.scoreboard.playerScores
            .map { kvp ->
                object {
                    val playerName = kvp.key
                    val score = kvp.value[objective]?.value ?: 0
                }
            }.filter { it.score != 0 }
            .sortedByDescending { it.score }
            .take(count)

        if (topScores.isEmpty()) {
            return
        }

        level.server.playerList.broadcastSystemMessage(
            Component.translatable("scoreboard.${WingsContractsMod.MOD_ID}.contract_score_periodic.message")
                .append(CommonComponents.NEW_LINE)
                .append(
                    Component.literal(
                        topScores.withIndex()
                            .joinToString("\n") { "${it.index + 1}. ${it.value.playerName} - ${it.value.score}" }
                    )),
            false
        )
    }
}