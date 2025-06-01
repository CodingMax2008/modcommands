package com.example.examplemod.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public class TopCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("top")
            .requires(source -> source.hasPermission(2))
            .executes(TopCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Level world = player.serverLevel();
        BlockPos pos = player.blockPosition();

        int topY = world.getMaxBuildHeight() - 1;
        for (int y = topY; y > world.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.isEmptyBlock(checkPos)) {
                topY = y + 1;
                break;
            }
        }

        player.teleportTo(pos.getX() + 0.5, topY, pos.getZ() + 0.5);

        player.level().playSound(
            null,
            player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            1.0F,
            1.0F
        );

        ((ServerLevel) player.level()).sendParticles(
            ParticleTypes.TOTEM_OF_UNDYING,
            player.getX(),
            player.getY() + 1,
            player.getZ(),
            30,
            0.5,
            1.0,
            0.5,
            0.1
        );

        return Command.SINGLE_SUCCESS;
    }
}
