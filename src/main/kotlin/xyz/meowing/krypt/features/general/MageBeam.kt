package xyz.meowing.krypt.features.general

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.krypt.annotations.Module
import xyz.meowing.krypt.config.ConfigDelegate
import xyz.meowing.krypt.config.ui.elements.base.ElementType
import xyz.meowing.krypt.events.core.PacketEvent
import xyz.meowing.krypt.events.core.RenderEvent
import xyz.meowing.krypt.events.core.TickEvent
import xyz.meowing.krypt.features.Feature
import xyz.meowing.krypt.managers.config.ConfigElement
import xyz.meowing.krypt.managers.config.ConfigManager
import xyz.meowing.krypt.utils.rendering.Render3D
import xyz.meowing.krypt.api.location.SkyBlockIsland
import xyz.meowing.krypt.events.core.LocationEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Inspired by Odin's old MageBeam feature which got removed, has some modified code from that feature.
 */
@Module
object MageBeam : Feature(
    "mageBeam",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private data class BeamData(
        val id: Long = System.nanoTime(),
        val points: CopyOnWriteArrayList<Vec3> = CopyOnWriteArrayList(),
        var lastUpdateTick: Int = 0
    )

    private val activeBeams = CopyOnWriteArrayList<BeamData>()
    private var currentTick = 0

    private val customBeamEnabled by ConfigDelegate<Boolean>("mageBeam.customBeamEnabled")
    private val duration by ConfigDelegate<Double>("mageBeam.duration")
    private val color by ConfigDelegate<Color>("mageBeam.color")
    private val hideParticles by ConfigDelegate<Boolean>("mageBeam.hideParticles")
    private val minPoints by ConfigDelegate<Double>("mageBeam.minPoints")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Mage beam",
                "Customizes the rendering of the mage beam ability",
                "General",
                ConfigElement(
                    "mageBeam",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Custom beam",
                ConfigElement(
                    "mageBeam.customBeamEnabled",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Duration (ticks)",
                ConfigElement(
                    "mageBeam.duration",
                    ElementType.Slider(10.0, 100.0, 40.0, false)
                )
            )
            .addFeatureOption(
                "Color",
                ConfigElement(
                    "mageBeam.color",
                    ElementType.ColorPicker(Color(255, 100, 100, 255))
                )
            )
            .addFeatureOption(
                "Hide vanilla particles",
                ConfigElement(
                    "mageBeam.hideParticles",
                    ElementType.Switch(true)
                )
            )
            .addFeatureOption(
                "Minimum points",
                ConfigElement(
                    "mageBeam.minPoints",
                    ElementType.Slider(1.0, 20.0, 8.0, false)
                )
            )
    }

    override fun initialize() {
        register<LocationEvent.WorldChange> {
            activeBeams.clear()
            currentTick = 0
        }

        register<TickEvent.Server> {
            currentTick++
        }

        register<PacketEvent.Received> { event ->
            val packet = event.packet as? ClientboundLevelParticlesPacket ?: return@register
            if (packet.particle != ParticleTypes.FIREWORK) return@register

            val newPoint = Vec3(packet.x, packet.y, packet.z)
            val recentBeam = activeBeams.lastOrNull()

            if (
                recentBeam != null &&
                (currentTick - recentBeam.lastUpdateTick) < 1 && 
                isPointInBeamDirection(recentBeam.points, newPoint)
            ) {
                recentBeam.points.add(newPoint)
                recentBeam.lastUpdateTick = currentTick
            } else {
                val newBeam = BeamData(points = CopyOnWriteArrayList<Vec3>().apply { add(newPoint) }, lastUpdateTick = currentTick)
                activeBeams.add(newBeam)

                TickScheduler.Server.schedule(duration.toLong()) {
                    activeBeams.removeAll { it.id == newBeam.id }
                }
            }

            if (hideParticles) event.cancel()
        }

        register<RenderEvent.World.Last> { event ->
            if (!customBeamEnabled) return@register

            activeBeams.forEach { beam ->
                if (beam.points.size < minPoints.toInt() || beam.points.isEmpty()) return@forEach

                Render3D.drawLine(
                    beam.points.first(),
                    beam.points.last(),
                    1f,
                    color,
                    event.context.consumers(),
                    event.context.matrixStack()
                )
            }
        }
    }

    private fun isPointInBeamDirection(points: List<Vec3>, newPoint: Vec3): Boolean {
        if (points.isEmpty() || points.size <= 1) return true
        val first = points.first()
        val last = points.last()
        val direction = last.subtract(first).normalize()
        val toNew = newPoint.subtract(last).normalize()
        return direction.dot(toNew) > 0.99
    }
}