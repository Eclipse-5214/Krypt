package xyz.meowing.krypt.features.map.render.layers

import net.minecraft.client.gui.GuiGraphics
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.map.Checkmark
import xyz.meowing.krypt.api.dungeons.enums.map.PuzzleType
import xyz.meowing.krypt.api.dungeons.enums.map.Room
import xyz.meowing.krypt.api.dungeons.enums.map.RoomType
import xyz.meowing.krypt.features.map.render.MapRenderConfig
import xyz.meowing.krypt.features.map.render.MapRenderer.translateAndScale
import xyz.meowing.krypt.utils.rendering.Render2D
import xyz.meowing.krypt.utils.rendering.Render2D.pushPop

object CheckmarkLayer {
    fun render(context: GuiGraphics) {
        renderDiscoveredRoomMarkers(context)
        renderRoomCheckmarks(context)
    }

    private fun renderDiscoveredRoomMarkers(context: GuiGraphics) {
        DungeonAPI.discoveredRooms.values.forEach { room ->
            val x = room.x * RoomLayer.ROOM_SPACING + RoomLayer.ROOM_RENDER_SIZE / 2 - 5
            val y = room.z * RoomLayer.ROOM_SPACING + RoomLayer.ROOM_RENDER_SIZE / 2 - 6

            context.pushPop {
                translateAndScale(context, x.toFloat(), y.toFloat(), MapRenderConfig.checkmarkScale.toFloat())
                Render2D.drawImage(context, Checkmark.questionMark, 0, 0, 10, 12)
            }
        }
    }

    private fun renderRoomCheckmarks(context: GuiGraphics) {
        DungeonAPI.uniqueRooms.forEach { room ->
            if (!room.explored) return@forEach

            val (centerX, centerZ) = RoomLayer.getRoomCenter(room)
            val x = (centerX * RoomLayer.ROOM_SPACING).toInt() + RoomLayer.ROOM_RENDER_SIZE / 2 - 6
            val y = (centerZ * RoomLayer.ROOM_SPACING).toInt() + RoomLayer.ROOM_RENDER_SIZE / 2 - 6

            val showCleared = MapRenderConfig.showClearedRoomCheckmarks && room.checkmark != Checkmark.UNEXPLORED
            val isPuzzle = room.type == RoomType.PUZZLE

            if (
                MapRenderConfig.renderPuzzleIcons &&
                isPuzzle &&
                room.checkmark == Checkmark.NONE
                ) {
                renderPuzzleIcon(context, room.name, x, y)
                return@forEach
            }

            val checkmark = getCheckmarkImage(room, showCleared, isPuzzle) ?: return@forEach
            val scale = if (showCleared) MapRenderConfig.clearedRoomCheckmarkScale.toFloat() else MapRenderConfig.checkmarkScale.toFloat()

            context.pushPop {
                translateAndScale(context, x.toFloat(), y.toFloat(), scale)
                Render2D.drawImage(context, checkmark, 0, 0, 12, 12)
            }
        }
    }

    private fun renderPuzzleIcon(context: GuiGraphics, puzzleName: String?, x: Int, y: Int) {
        context.pushPop {
            val scale = (MapRenderConfig.puzzleIconScale * MapRenderConfig.checkmarkScale).toFloat()
            translateAndScale(context, x.toFloat(), y.toFloat(), scale)
            val offset = if (puzzleName == "Teleport Maze") -2 else 0
            Render2D.drawImage(context, PuzzleType.getPuzzleIcon(puzzleName), 0, offset, 12, 12)
        }
    }

    private fun getCheckmarkImage(room: Room, showCleared: Boolean, isPuzzle: Boolean) = when {
        showCleared && isPuzzle -> {
            room.checkmark.image
        }

        showCleared && room.checkmark in listOf(Checkmark.GREEN, Checkmark.WHITE) -> {
            if (room.checkmark == Checkmark.GREEN) Checkmark.greenCheck else Checkmark.whiteCheck
        }

        room.type in listOf(RoomType.ENTRANCE, RoomType.PUZZLE) -> {
            null
        }

        room.type == RoomType.NORMAL && room.secrets != 0 -> {
            null
        }

        else -> room.checkmark.image
    }
}