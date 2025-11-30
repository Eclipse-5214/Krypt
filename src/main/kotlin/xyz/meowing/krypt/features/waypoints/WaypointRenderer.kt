package xyz.meowing.krypt.features.waypoints

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.map.Checkmark
import xyz.meowing.krypt.events.core.RenderEvent
import xyz.meowing.krypt.features.waypoints.DungeonWaypoints.onlyRenderAfterClear
import xyz.meowing.krypt.features.waypoints.DungeonWaypoints.stopRenderAfterGreen
import xyz.meowing.krypt.features.waypoints.DungeonWaypoints.textRenderDistance
import xyz.meowing.krypt.features.waypoints.DungeonWaypoints.textScale
import xyz.meowing.krypt.utils.Utils.equalsOneOf
import xyz.meowing.krypt.utils.rendering.Render3D

object WaypointRenderer {
    fun render(event: RenderEvent.World.Last) {
        val room = DungeonAPI.currentRoom ?: return

        if (stopRenderAfterGreen && room.checkmark == Checkmark.GREEN) return
        if (onlyRenderAfterClear && !room.checkmark.equalsOneOf(Checkmark.WHITE, Checkmark.GREEN)) return

        val waypoints = RoomWaypointHandler.getWaypoints(room) ?: return

        val matrices = event.context.matrixStack()
        val consumers = event.context.consumers()

        waypoints.forEach { waypoint ->
            if (waypoint.clicked) return@forEach
            val color = if (DungeonWaypoints.overrideColors) (waypoint.type?.color ?: WaypointType.MINE.color) else waypoint.color
            val block = waypoint.aabb.move(waypoint.blockPos)

            val style = when {
                waypoint.filled && waypoint.depth -> 2
                waypoint.filled -> 1
                else -> 0
            }

            when (style) {
                0 -> {
                    Render3D.drawOutlinedBB(
                        block,
                        color,
                        consumers,
                        matrices,
                        true
                    )
                }

                1 -> {
                    Render3D.drawFilledBB(
                        block,
                        color,
                        consumers,
                        matrices,
                        true
                    )
                }

                2 -> {
                    Render3D.drawSpecialBB(
                        block,
                        color,
                        consumers,
                        matrices,
                        true
                    )
                }
            }

            if (DungeonWaypoints.renderText) {
                val title = waypoint.title ?: if (waypoint.type == WaypointType.START) "Start" else return@forEach
                val center = block.center.add(0.0, 0.1 * textScale, 0.0)
                val player = KnitClient.player ?: return@forEach

                if (player.position().distanceTo(center) >= textRenderDistance) return@forEach

                Render3D.drawString(
                    title,
                    center,
                    matrices,
                    scale = textScale.toFloat(),
                    depth = false
                )
            }
        }
    }
}