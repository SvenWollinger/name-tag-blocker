package io.wollinger.mc.nametagblocker

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.io.File

@Serializable
data class ModConfig(
    val ignoreCase: Boolean = false,
    val global: ArrayList<String> = arrayListOf("Dinnerbone", "Grumm"),
)

object BlockedDatabase {
    private val json: Json = Json {
        encodeDefaults = true
        prettyPrint = true
    }
    private val configDir: File = FabricLoader.getInstance().configDir.toFile()
    private val configFile: File = File(configDir, "nametagblocker.json")
    private val config: ModConfig

    init {
        NameTagBlocker.logger.info("Trying to load config file ${configFile.absolutePath}")
        config = if(!configFile.exists()) {
            ModConfig()
        } else {
            json.decodeFromString(configFile.readText())
        }
        save()
    }

    fun add(name: String) {
        if(config.global.contains(name)) return
        config.global.add(name)
        save()
    }

    fun remove(name: String) {
        config.global.remove(name)
        save()
    }

    fun getAll() = config.global.toList()

    fun save() {
        configFile.writeText(json.encodeToString(config))
    }
}