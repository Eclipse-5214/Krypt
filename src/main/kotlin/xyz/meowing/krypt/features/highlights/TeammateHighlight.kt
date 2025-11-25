package xyz.meowing.krypt.features.highlights

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import xyz.meowing.krypt.annotations.Module
import xyz.meowing.krypt.api.dungeons.DungeonAPI
import xyz.meowing.krypt.api.dungeons.enums.DungeonClass
import xyz.meowing.krypt.api.location.SkyBlockIsland
import xyz.meowing.krypt.config.ConfigDelegate
import xyz.meowing.krypt.config.ui.elements.base.ElementType
import xyz.meowing.krypt.features.Feature
import xyz.meowing.krypt.managers.config.ConfigElement
import xyz.meowing.krypt.managers.config.ConfigManager
import java.awt.Color

@Module
object TeammateHighlight : Feature(
    "teammateHighlight",
    island = SkyBlockIsland.THE_CATACOMBS
) {
    private val mageColor by ConfigDelegate<Color>("teammateHighlight.mageColor")
    private val archerColor by ConfigDelegate<Color>("teammateHighlight.archerColor")
    private val healerColor by ConfigDelegate<Color>("teammateHighlight.healerColor")
    private val tankColor by ConfigDelegate<Color>("teammateHighlight.tankColor")
    private val bersColor by ConfigDelegate<Color>("teammateHighlight.bersColor")

    override fun addConfig() {
        ConfigManager
            .addFeature(
                "Teammate highlight",
                "Highlights teammates in dungeons",
                "Highlights",
                ConfigElement(
                    "teammateHighlight",
                    ElementType.Switch(false)
                )
            )
            .addFeatureOption(
                "Mage color",
                ConfigElement(
                    "teammateHighlight.mageColor",
                    ElementType.ColorPicker(Color(85, 255, 255))
                )
            )
            .addFeatureOption(
                "Archer color",
                ConfigElement(
                    "teammateHighlight.archerColor",
                    ElementType.ColorPicker(Color(255, 170, 0))
                )
            )
            .addFeatureOption(
                "Healer color",
                ConfigElement(
                    "teammateHighlight.healerColor",
                    ElementType.ColorPicker( Color(255, 85, 255))
                )
            )
            .addFeatureOption(
                "Tank color",
                ConfigElement(
                    "teammateHighlight.tankColor",
                    ElementType.ColorPicker(Color(0, 170, 0))
                )
            )
            .addFeatureOption(
                "Berserk color",
                ConfigElement(
                    "teammateHighlight.bersColor",
                    ElementType.ColorPicker( Color(170, 0, 0))
                )
            )
    }

    @JvmStatic
    fun getTeammateColor(entity: Entity): Int? {
        if (!isEnabled()) return null
        if (entity !is Player) return null
        return DungeonAPI.players.find { it?.name == entity.name.stripped }?.dungeonClass?.getColor()?.rgb
    }

    private fun DungeonClass?.getColor(): Color? {
        return when (this) {
            DungeonClass.MAGE -> mageColor
            DungeonClass.ARCHER -> archerColor
            DungeonClass.HEALER -> healerColor
            DungeonClass.TANK -> tankColor
            DungeonClass.BERSERK -> bersColor
            else -> null
        }
    }
}