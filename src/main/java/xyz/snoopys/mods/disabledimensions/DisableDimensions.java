package xyz.snoopys.mods.disabledimensions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisableDimensions implements ModInitializer {

	public static final String MOD_ID = "disabledimensions";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[DisableDimensions] Initializing...");

		// Prevent throwing an Eye of Ender.
		ItemEvents.USE.register((level, player, hand) -> {
			if (player.getItemInHand(hand).is(Items.ENDER_EYE)) {
				LOGGER.info(
						"[DisableDimensions] Blocked {} from throwing an Eye of Ender.",
						player.getName().getString()
				);

				player.sendSystemMessage(
						Component.literal("Notice: Throwing Eye of Ender is disabled.")
				);

				return InteractionResult.FAIL;
			}

			// Let vanilla handle every other item.
			return null;
		});

		// Prevent ONLY placing an Eye of Ender into an End Portal Frame.
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {

			if (player.getItemInHand(hand).is(Items.ENDER_EYE)) {
				var blockState = level.getBlockState(hitResult.getBlockPos());

				if (blockState.is(Blocks.END_PORTAL_FRAME)) {
					LOGGER.info(
							"[DisableDimensions] Blocked {} from placing an Eye of Ender into an End Portal Frame.",
							player.getName().getString()
					);

					player.sendSystemMessage(
							Component.literal("Notice: Placing Eye of Ender into End Portal Frames is disabled.")
					);

					return InteractionResult.FAIL;
				}
			}

			// Let vanilla handle every other block interaction.
			return InteractionResult.PASS;
		});

		LOGGER.info("[DisableDimensions] Initialization complete.");
	}
}