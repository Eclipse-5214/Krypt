package xyz.meowing.krypt.api.dungeons.enums.map

import net.minecraft.resources.ResourceLocation
import xyz.meowing.krypt.Krypt

enum class PuzzleType(
    val nameString: String,
    val icon: ResourceLocation,
    var checkmark: Checkmark? = null
) {
    UNKNOWN("???", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/clear/question_mark")),
    HIGHER_BLAZE("Higher Blaze", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/blaze")),
    LOWER_BLAZE("Lower Blaze", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/blaze")),
    BEAMS("Creeper Beams", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/beam")),
    WEIRDOS("Three Weirdos", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/weirdos")),
    TIC_TAC_TOE("Tic Tac Toe", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/ttt")),
    WATER_BOARD("Water Board", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/water")),
    TELEPORT_MAZE("Teleport Maze", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/tp_maze")),
    BOULDER("Boulder", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/boulder")),
    ICE_FILL("Ice Fill", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/ice_fill")),
    ICE_PATH("Ice Path", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/ice_path")),
    QUIZ("Quiz", ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "krypt/puzzles/quiz"))
    ;

    companion object {
        fun getPuzzleIcon(puzzleName: String?): ResourceLocation {
            return when (puzzleName) {
                BEAMS.nameString -> BEAMS.icon
                WEIRDOS.nameString -> WEIRDOS.icon
                TIC_TAC_TOE.nameString -> TIC_TAC_TOE.icon
                WATER_BOARD.nameString -> WATER_BOARD.icon
                TELEPORT_MAZE.nameString -> TELEPORT_MAZE.icon
                HIGHER_BLAZE.nameString -> HIGHER_BLAZE.icon
                LOWER_BLAZE.nameString -> LOWER_BLAZE.icon
                BOULDER.nameString -> BOULDER.icon
                ICE_FILL.nameString -> ICE_FILL.icon
                ICE_PATH.nameString -> ICE_PATH.icon
                QUIZ.nameString -> QUIZ.icon
                else -> UNKNOWN.icon
            }
        }
    }
}