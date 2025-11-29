package xyz.meowing.krypt.features.map.render.layers

import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.map.Checkmark
import xyz.meowing.krypt.api.dungeons.enums.map.Room
import xyz.meowing.krypt.api.dungeons.enums.map.RoomType
import xyz.meowing.krypt.features.map.render.MapRenderConfig
import xyz.meowing.krypt.utils.rendering.Render2D
import xyz.meowing.krypt.utils.rendering.Render2D.pushPop
import xyz.meowing.krypt.utils.rendering.Render2D.width

object LabelLayer {
    fun render(context: GuiGraphics) {
        DungeonAPI.uniqueRooms.forEach { room ->
            if (!room.explored) return@forEach

            val checkmarkMode = when (room.type) {
                RoomType.PUZZLE -> MapRenderConfig.puzzleCheckmarkMode
                RoomType.NORMAL, RoomType.YELLOW, RoomType.TRAP, RoomType.UNKNOWN -> MapRenderConfig.normalCheckmarkMode
                else -> return@forEach
            }

            if (checkmarkMode < 1) return@forEach

            renderRoomLabel(context, room, checkmarkMode)
        }
    }

    private fun renderRoomLabel(context: GuiGraphics, room: Room, checkmarkMode: Int) {
        val secrets = if (room.checkmark == Checkmark.GREEN) room.secrets else room.secretsFound

        val roomNameColor = when (room.checkmark) {
            Checkmark.FAILED -> MapRenderConfig.roomTextFailedColor.code
            Checkmark.GREEN -> MapRenderConfig.roomTextSecretsColor.code
            Checkmark.WHITE -> MapRenderConfig.roomTextClearedColor.code
            else -> MapRenderConfig.roomTextNotClearedColor.code
        }

        val secretsColor = when (room.checkmark) {
            Checkmark.GREEN -> MapRenderConfig.secretsTextSecretsColor.code
            Checkmark.WHITE -> MapRenderConfig.secretsTextClearedColor.code
            else -> MapRenderConfig.secretsTextNotClearedColor.code
        }

        val roomText = room.name ?: "???"
        val secretText = "$secrets/${room.secrets}"

        val lines = buildList {
            if (checkmarkMode in listOf(1, 3)) {
                addAll(roomText.split(" ").map { roomNameColor + it })
            }

            if (checkmarkMode in listOf(2, 3) && room.secrets != 0) {
                add(secretsColor + secretText)
            }
        }

        val (centerX, centerZ) = RoomLayer.getRoomCenter(room)
        val scale = (0.75f * MapRenderConfig.roomLabelScale).toFloat()

        context.pushPop {
            val matrix = context.pose()
            //#if MC >= 1.21.8
            //$$ matrix.translate((centerX * RoomLayer.ROOM_SPACING).toFloat() + RoomLayer.ROOM_RENDER_SIZE / 2, (centerZ * RoomLayer.ROOM_SPACING).toFloat() + RoomLayer.ROOM_RENDER_SIZE / 2)
            //$$ matrix.scale(scale, scale, 1f)
            //#else
            matrix.translate((centerX * RoomLayer.ROOM_SPACING).toFloat() + RoomLayer.ROOM_RENDER_SIZE / 2, (centerZ * RoomLayer.ROOM_SPACING).toFloat() + RoomLayer.ROOM_RENDER_SIZE / 2, 0f)
            matrix.scale(scale, scale, 1f)
            //#endif

            lines.forEachIndexed { i, line ->
                val drawX = (-line.width() / 2).toFloat()
                val drawY = (9 * i - (lines.size * 9) / 2).toFloat()

                if (MapRenderConfig.textShadow) {
                    renderTextShadow(context, line.stripColor(), drawX.toInt(), drawY.toInt(), scale)
                }

                Render2D.renderString(context, line, drawX, drawY, 1f)
            }
        }
    }

    private fun renderTextShadow(context: GuiGraphics, text: String, x: Int, y: Int, scale: Float) {
        val offsets = listOf(scale to 0f, -scale to 0f, 0f to scale, 0f to -scale)

        offsets.forEach { (dx, dy) ->
            context.pushPop {
                val matrix = context.pose()
                matrix.translate(dx, dy, 0f)
                Render2D.renderString(context, "§0$text", x.toFloat(), y.toFloat(), 1f)
            }
        }
    }
}