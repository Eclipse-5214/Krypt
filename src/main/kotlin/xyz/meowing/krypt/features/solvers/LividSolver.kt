package xyz.meowing.krypt.features.solvers

import xyz.meowing.krypt.config.ConfigDelegate
import xyz.meowing.krypt.config.ui.elements.base.ElementType
import xyz.meowing.krypt.features.Feature
import xyz.meowing.krypt.utils.rendering.Render3D
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.BlockPos
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.knit.api.KnitPlayer.player
import xyz.meowing.knit.api.scheduler.TickScheduler
import xyz.meowing.krypt.annotations.Module
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.DungeonFloor
import xyz.meowing.krypt.events.core.EntityEvent
import xyz.meowing.krypt.events.core.LocationEvent
import xyz.meowing.krypt.events.core.RenderEvent
import xyz.meowing.krypt.events.core.WorldEvent
import xyz.meowing.krypt.managers.config.ConfigElement
import xyz.meowing.krypt.managers.config.ConfigManager
import xyz.meowing.krypt.utils.Utils.toFloatArray
import xyz.meowing.krypt.utils.glowThisFrame
import xyz.meowing.krypt.utils.glowingColor
import java.awt.Color

@Module
object LividSolver : Feature(
    "lividSolver",
    dungeonFloor = listOf(DungeonFloor.F5, DungeonFloor.M5)
) {
    private var currentLivid = Livid.HOCKEY
    private val lividPos = BlockPos(5, 108, 42)

    private enum class Livid(
        val entityName: String,
        val block: Block
    ) {
        VENDETTA("Vendetta", Blocks.WHITE_STAINED_GLASS),
        CROSSED("Crossed", Blocks.MAGENTA_STAINED_GLASS),
        ARCADE("Arcade", Blocks.YELLOW_STAINED_GLASS),
        SMILE("Smile", Blocks.LIME_STAINED_GLASS),
        DOCTOR("Doctor", Blocks.GRAY_STAINED_GLASS),
        PURPLE("Purple", Blocks.PURPLE_STAINED_GLASS),
        SCREAM("Scream", Blocks.BLUE_STAINED_GLASS),
        FROG("Frog", Blocks.GREEN_STAINED_GLASS),
        HOCKEY("Hockey", Blocks.RED_STAINED_GLASS)
        ;

        var entity: Player? = null
    }

    private val highlightLividColor by ConfigDelegate<Color>("highlightLivid.color")
    private val highlightLividLine by ConfigDelegate<Boolean>("highlightLivid.line")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Livid solver",
                "Shows the correct Livid in F5/M5",
                "Solvers",
                ConfigElement(
                    "lividSolver",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight correct livid color",
                ConfigElement(
                    "lividSolver.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
            .addFeatureOption(
                "Tracer",
                ConfigElement(
                    "lividSolver.line",
                    ElementType.Switch(false)
                )
            )
    }

    override fun initialize() {
        createCustomEvent<RenderEvent.Entity.Pre>("renderLivid") { event ->
            val entity = event.entity

            if (currentLivid.entity == entity && player?.hasLineOfSight(entity) == true) {
                entity.glowThisFrame = true
                entity.glowingColor = highlightLividColor.rgb
            }
        }

        createCustomEvent<RenderEvent.World.Last>("renderLine") { event ->
            currentLivid.entity?.let { entity ->
                if (player?.hasLineOfSight(entity) == true) {
                    Render3D.drawLineToEntity(
                        entity,
                        event.context.consumers(),
                        event.context.matrixStack(),
                        highlightLividColor.toFloatArray(),
                        highlightLividColor.alpha.toFloat()
                    )
                }
            }
        }

        register<WorldEvent.BlockUpdate> { event ->
            if (event.pos != lividPos) return@register

            currentLivid = Livid.entries.find { it.block.defaultBlockState() == event.new.block.defaultBlockState() } ?: return@register
            registerRender()
        }

        register<EntityEvent.Packet.Metadata> { event ->
            if (!DungeonAPI.inBoss) return@register

            val blindnessDuration = client.player?.getEffect(MobEffects.BLINDNESS)?.duration ?: 0
            val delay = (blindnessDuration - 20).coerceAtLeast(1).toLong()

            TickScheduler.Client.schedule(delay) {
                val player = event.entity as? Player ?: return@schedule
                if (player.name.stripped == "${currentLivid.entityName} Livid") currentLivid.entity = player
            }
        }

        register<LocationEvent.WorldChange> {
            unregisterRender()
        }
    }

    private fun registerRender() {
        registerEvent("renderLivid")
        if (highlightLividLine) registerEvent("renderLine")
    }

    private fun unregisterRender() {
        unregisterEvent("renderLivid")
        unregisterEvent("renderLine")

        currentLivid = Livid.HOCKEY
        currentLivid.entity = null
    }
}