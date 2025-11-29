package xyz.meowing.krypt.features.map.render.registry

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.phys.Vec3
import xyz.meowing.knit.api.KnitClient
import xyz.meowing.krypt.Krypt
import xyz.meowing.krypt.features.map.render.layers.BossMapLayer
import java.io.InputStreamReader

object BossMapRegistry {
    private val gson = Gson()
    private val bossMaps = mutableMapOf<String, List<BossMapLayer.BossMapData>>()
    private var initialized = false

    init {
        val resourceManager = KnitClient.client.resourceManager
        load(resourceManager)
    }

    fun getBossMap(floor: Int, playerPos: Vec3): BossMapLayer.BossMapData? {
        val maps = bossMaps[floor.toString()] ?: return null
        return maps.firstOrNull { map ->
            (0..2).all { axis ->
                val min = map.bounds[0][axis]
                val max = map.bounds[1][axis]
                val pos = listOf(playerPos.x, playerPos.y, playerPos.z)[axis]
                pos in min..max
            }
        }
    }

    private fun load(resourceManager: ResourceManager) {
        val id = ResourceLocation.fromNamespaceAndPath(Krypt.NAMESPACE, "dungeons/imagedata.json")
        val resource = resourceManager.getResource(id).orElse(null) ?: return

        val reader = InputStreamReader(resource.open())
        val type = object : TypeToken<Map<String, List<BossMapLayer.BossMapData>>>() {}.type
        val parsed = gson.fromJson<Map<String, List<BossMapLayer.BossMapData>>>(reader, type)

        bossMaps.putAll(parsed)
        initialized = true
    }
}