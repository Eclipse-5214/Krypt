package xyz.meowing.krypt.features.floor7.solvers

import net.minecraft.core.BlockPos
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import xyz.meowing.knit.api.KnitClient.world
import xyz.meowing.krypt.annotations.Module
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.DungeonFloor
import xyz.meowing.krypt.api.dungeons.enums.DungeonPhase
import xyz.meowing.krypt.config.ConfigDelegate
import xyz.meowing.krypt.config.ui.elements.base.ElementType
import xyz.meowing.krypt.events.core.RenderEvent
import xyz.meowing.krypt.features.Feature
import xyz.meowing.krypt.managers.config.ConfigElement
import xyz.meowing.krypt.managers.config.ConfigManager
import xyz.meowing.krypt.utils.rendering.Render3D
import java.awt.Color

@Module
object LightsSolver : Feature(
    "lightsSolver",
    dungeonFloor = listOf(DungeonFloor.F7, DungeonFloor.M7)
) {
    private val blocks = listOf(
        BlockPos(62, 136, 142),
        BlockPos(58, 136, 142),
        BlockPos(60, 135, 142),
        BlockPos(60, 134, 142),
        BlockPos(62, 133, 142),
        BlockPos(58, 133, 142)
    )

    private val color by ConfigDelegate<Color>("lightsSolver.color")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Lights solver",
                "Displays the fastest solution for lights device",
                "Floor 7",
                ConfigElement(
                    "lightsSolver",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Highlight color",
                ConfigElement(
                    "lightsSolver.color",
                    ElementType.ColorPicker(Color(0, 255, 255, 127))
                )
            )
    }

    override fun initialize() {
        register<RenderEvent.World.Last> { event ->
            if (DungeonAPI.F7Phase != DungeonPhase.F7.P3) return@register
            val world = world ?: return@register

            blocks.forEach { pos ->
                val block = world.getBlockState(pos) ?: return@forEach
                if (block.block != Blocks.LEVER) return@forEach
                if (block.getValue(BlockStateProperties.POWERED)) return@forEach

                val shape = world.getBlockState(pos).getShape(EmptyBlockGetter.INSTANCE, pos).move(pos)

                Render3D.drawFilledShapeVoxel(
                    shape,
                    color,
                    event.context.consumers(),
                    event.context.matrixStack()
                )
            }
        }
    }
}