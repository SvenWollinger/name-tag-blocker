package io.wollinger.mc.nametagblocker

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object NameTagBlocker : ModInitializer {
    val logger: Logger = LoggerFactory.getLogger("name-tag-blocker")

	override fun onInitialize() {
		BlockedDatabase //Referencing to init

		UseEntityCallback.EVENT.register { player, level, hand, entity, result ->
			val item = player.getItemInHand(hand)
			if(!item.`is`(Items.NAME_TAG)) return@register InteractionResult.PASS
			val itemName = item.get(DataComponents.CUSTOM_NAME)?.string ?: return@register InteractionResult.PASS
			if(BlockedDatabase.isBlocked(itemName)) {
				if(result != null)
					player.displayClientMessage(Component.literal("That name is blocked!"), false)
				return@register InteractionResult.FAIL
			}
			return@register InteractionResult.PASS
		}

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
								all.forEach { option -> builder.suggest(option) }
								builder.buildFuture()
							}
							.executes { ctx ->
								val name = StringArgumentType.getString(ctx, "name")
								BlockedDatabase.remove(name)
								ctx.source.sendSuccess({ Component.literal("Removed!") }, false)
								1
							}
					)
			)
		)}
	}
}