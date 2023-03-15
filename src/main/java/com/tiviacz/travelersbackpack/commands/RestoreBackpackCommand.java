package com.tiviacz.travelersbackpack.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tiviacz.travelersbackpack.common.BackpackManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class RestoreBackpackCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated)
    {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = CommandManager.literal("tb").requires(player -> player.hasPermissionLevel(2));

        Command<ServerCommandSource> restore = (commandSource) -> {
            UUID backpackID = UuidArgumentType.getUuid(commandSource, "backpack_id");
            ServerPlayerEntity player = EntityArgumentType.getPlayer(commandSource, "target");
            ItemStack backpack = BackpackManager.getBackpack(player.getServerWorld(), backpackID);
            if(backpack == null)
            {
                commandSource.getSource().sendError(new LiteralText("Backpack with ID " + backpackID.toString() + " not found"));
                return 0;
            }

            if(!player.inventory.insertStack(backpack))
            {
                player.dropItem(backpack, false);
            }

            commandSource.getSource().sendFeedback(new LiteralText("Successfully restored " + player.getDisplayName().getString() + "'s backpack"), true);
            return 1;
        };

        literalargumentbuilder
                .then(CommandManager.argument("target", EntityArgumentType.player())
                        .then(CommandManager.argument("backpack_id", UuidArgumentType.uuid())
                                .then(CommandManager.literal("restore").executes(restore))
                        ));


        dispatcher.register(literalargumentbuilder);
    }
}