package xyz.meowing.krypt.api.dungeons.enums.map

import xyz.meowing.krypt.features.map.render.MapRenderConfig
import java.awt.Color

enum class RoomType {
    NORMAL,
    PUZZLE,
    TRAP,
    YELLOW,
    BLOOD,
    FAIRY,
    ENTRANCE,
    UNKNOWN
    ;

    val color: Color
        get() = when (this) {
            NORMAL -> MapRenderConfig.normalRoomColor
            PUZZLE -> MapRenderConfig.puzzleRoomColor
            TRAP -> MapRenderConfig.trapRoomColor
            YELLOW -> MapRenderConfig.yellowRoomColor
            BLOOD -> MapRenderConfig.bloodRoomColor
            FAIRY -> MapRenderConfig.fairyRoomColor
            ENTRANCE -> MapRenderConfig.entranceRoomColor
            UNKNOWN -> Color.GRAY
        }

    val colorCode: String
        get() = when (this) {
            NORMAL -> "7"
            PUZZLE -> "d"
            TRAP -> "6"
            YELLOW -> "e"
            BLOOD -> "c"
            FAIRY -> "d"
            ENTRANCE -> "a"
            UNKNOWN -> "f"
        }

    val nickname: String
        get() = this.name.lowercase().replaceFirstChar { it.uppercase() }

    companion object {
        fun fromRoomData(data: RoomMetadata): RoomType? = when(data.type.lowercase()) {
            "normal", "rare" -> NORMAL
            "puzzle" -> PUZZLE
            "trap" -> TRAP
            "champion" -> YELLOW
            "blood" -> BLOOD
            "fairy" -> FAIRY
            "entrance" -> ENTRANCE
            else -> null
        }

        fun fromMapColor(color: Int): RoomType? = when (color) {
            18 -> BLOOD
            30 -> ENTRANCE
            63, 85 -> NORMAL
            82 -> FAIRY
            62 -> TRAP
            74 -> YELLOW
            66 -> PUZZLE
            else -> null
        }
    }
}