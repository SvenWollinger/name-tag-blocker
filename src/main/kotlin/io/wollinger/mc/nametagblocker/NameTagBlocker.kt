package io.wollinger.mc.nametagblocker

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import org.slf4j.LoggerFactory

object NameTagBlocker : ModInitializer {
    private val logger = LoggerFactory.getLogger("name-tag-blocker")

	override fun onInitialize() {
		CommandRegistrationCallback.EVENT.register { dispatcher, context, selection ->
			dispatcher.register(literal("nametagblocker").then(
				literal("list").executes { ctx ->
					val all = BlockedDatabase.getAll().joinToString(separator = ", ")
					ctx.source.sendSuccess({Component.literal("All: $all")}, false)
					1
				}
			)
				.then(
					literal("add").then(
						argument("name", StringArgumentType.word()).executes { ctx ->
							val name = StringArgumentType.getString(ctx, "name")
							BlockedDatabase.add(name)
							ctx.source.sendSuccess({Component.literal("Added!")}, false)
							1
						}
					)
				)
				.then(
					literal("remove").then(
						argument("name", StringArgumentType.word())
							.suggests { context, builder ->
								val all = BlockedDatabase.getAll().filter { name ->
									val typed = builder.remaining
									typed.isEmpty() || name.startsWith(typed)
								}

								for (option in all) {
									builder.suggest(option)
								}

								builder.buildFuture()
							}
							.executes { ctx ->
								val name = StringArgumentType.getString(ctx, "name")
								BlockedDatabase.remove(name)
								ctx.source.sendSuccess({Component.literal("Removed!")}, false)
								1
							}
					)
			)
		)}
	}
}