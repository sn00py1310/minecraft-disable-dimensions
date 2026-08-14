package xyz.snoopys.mods.disabledimensions;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;

import static net.minecraft.commands.Commands.literal;

public class DisableDimensions implements ModInitializer {

    public static final String MOD_ID = "disabledimensions";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Identifier COMMAND_PERMISSION = Identifier.fromNamespaceAndPath(MOD_ID, "command");
    private static boolean enderEyesEnabled = false;

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("disabledimensions").requires(source -> source.checkPermission(COMMAND_PERMISSION, PermissionLevel.GAMEMASTERS)).then(literal("ender_eye").executes(context -> {
                String status = enderEyesEnabled ? "OFF" : "ON";
                context.getSource().sendSuccess(() -> Component.literal("Eye of Ender restrictions are currently " + status + "."), false);
                return 1;
            }).then(literal("disable").executes(context -> {
                enderEyesEnabled = false;
                context.getSource().sendSuccess(() -> Component.literal("Eye of Ender restrictions are now ON."), false);
                LOGGER.info("[DisableDimensions] Eye of Ender restrictions enabled by {}.", context.getSource().getTextName());
                return 1;
            }))
            .then(literal("enable").executes(context -> {
                enderEyesEnabled = true;
                context.getSource().sendSuccess(() -> Component.literal("Eye of Ender restrictions are now OFF."), true);
                LOGGER.info("[DisableDimensions] Eye of Ender restrictions disabled by {}.", context.getSource().getTextName());
                return 1;
            }))));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[DisableDimensions] Initializing...");

        // Register commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));

        /*
         * Prevent throwing an Eye of Ender.
         */
        ItemEvents.USE.register((level, player, hand) -> {
            if (!enderEyesEnabled && player.getItemInHand(hand).is(Items.ENDER_EYE)) {
                LOGGER.info("[DisableDimensions] Blocked {} from throwing an Eye of Ender.", player.getName().getString());
                player.sendSystemMessage(Component.literal("Notice: Throwing Eye of Ender is disabled."));
                return InteractionResult.FAIL;
            }

            // Let vanilla handle every other item.
            return null;
        });

        /*
         * Prevent placing an Eye of Ender into an End Portal Frame.
         */
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {

            if (!enderEyesEnabled && player.getItemInHand(hand).is(Items.ENDER_EYE)) {
                var blockState = level.getBlockState(hitResult.getBlockPos());
                if (blockState.is(Blocks.END_PORTAL_FRAME)) {
                    LOGGER.info("[DisableDimensions] Blocked {} from placing an Eye of Ender into an End Portal Frame.", player.getName().getString());
                    player.sendSystemMessage(Component.literal("Notice: Placing Eye of Ender into End Portal Frames is disabled."));
                    return InteractionResult.FAIL;
                }
            }

            // Let vanilla handle every other block interaction.
            return InteractionResult.PASS;
        });

        LOGGER.info("[DisableDimensions] Initialization complete.");
    }
}