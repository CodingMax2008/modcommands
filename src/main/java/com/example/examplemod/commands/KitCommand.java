package com.example.examplemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;

public class KitCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("kit")
            .then(Commands.argument("kitname", StringArgumentType.word())
                .suggests(KitCommand::suggestKits)  // Suggestions dynamiques ici
                .executes(context -> giveKit(
                    context.getSource(),
                    context.getSource().getPlayerOrException(),
                    StringArgumentType.getString(context, "kitname")))));
    }

    private static int giveKit(CommandSourceStack source, ServerPlayer player, String kitName) throws CommandSyntaxException {
        boolean isCreative = player.gameMode.getGameModeForPlayer() == GameType.CREATIVE;

        switch (kitName.toLowerCase()) {
            case "dev":
                if (!isCreative) {
                    source.sendFailure(Component.literal("Vous devez être en mode Créatif pour utiliser ce kit."));
                    return 0;
                }
                giveDevKit(player);
                break;

            case "start":
                giveStartKit(player);
                break;

            case "advanced":
                if (!isCreative && !hasAdvancement(player, new ResourceLocation("minecraft", "husbandry/suit_up"))) {
                    source.sendFailure(Component.literal("Vous devez avoir le succès 'Suit Up' pour utiliser ce kit."));
                    return 0;
                }
                giveAdvancedKit(player);
                break;

            case "explorer":
                if (!isCreative && !hasAdvancement(player, new ResourceLocation("minecraft", "nether/return_to_sender"))) {
                    source.sendFailure(Component.literal("Vous devez avoir mis un pied dans le Nether pour utiliser ce kit."));
                    return 0;
                }
                giveExplorerKit(player);
                break;

            case "alchemist":
                if (!isCreative && !hasAdvancement(player, new ResourceLocation("minecraft", "nether/brew_potion"))) {
                    source.sendFailure(Component.literal("Vous devez avoir l'avancement 'Brew Potion' pour utiliser ce kit."));
                    return 0;
                }
                giveAlchemistKit(player);
                break;

            case "nature":
                if (!isCreative && !hasAdvancement(player, new ResourceLocation("minecraft", "husbandry/plant_seed"))) {
                    source.sendFailure(Component.literal("Vous devez avoir l'avancement 'A Seedy Place' pour utiliser ce kit."));
                    return 0;
                }
                giveNatureKit(player);
                break;

            default:
                source.sendFailure(Component.literal("Kit inconnu : " + kitName));
                return 0;
        }

        source.sendSuccess(() -> Component.literal("Kit '" + kitName + "' donné !"), true);
        return 1;
    }

    // Suggestions dynamiques selon gamemode et avancements
    private static CompletableFuture<Suggestions> suggestKits(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }

        boolean isCreative = player.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
        List<String> availableKits = new ArrayList<>();

        // Kit toujours accessible
        availableKits.add("start");

        if (isCreative) {
            // En créatif, tous les kits sont accessibles
            availableKits.add("dev");
            availableKits.add("advanced");
            availableKits.add("explorer");
            availableKits.add("alchemist");
            availableKits.add("nature");
        } else {
            // En survie, accès selon avancements
            if (hasAdvancement(player, new ResourceLocation("minecraft", "husbandry/suit_up")))
                availableKits.add("advanced");

            if (hasAdvancement(player, new ResourceLocation("minecraft", "nether/return_to_sender")))
                availableKits.add("explorer");

            if (hasAdvancement(player, new ResourceLocation("minecraft", "nether/brew_potion")))
                availableKits.add("alchemist");

            if (hasAdvancement(player, new ResourceLocation("minecraft", "husbandry/plant_seed")))
                availableKits.add("nature");
        }

        for (String kit : availableKits) {
            builder.suggest(kit);
        }
        return builder.buildFuture();
    }

    // Vérification d'avancement
    private static boolean hasAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(advancementId);
        if (advancement == null) return false;
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }

    // Donne ou drop l'item selon place dispo
    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // Kits

    private static void giveDevKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.COMMAND_BLOCK, 64));
        giveOrDrop(player, new ItemStack(Items.DEBUG_STICK));
        giveOrDrop(player, new ItemStack(Items.STRUCTURE_BLOCK, 8));
        giveOrDrop(player, new ItemStack(Items.BARRIER, 20)); // invisible block
    }

    private static void giveStartKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.STONE_PICKAXE));
        giveOrDrop(player, new ItemStack(Items.STONE_AXE));
        giveOrDrop(player, new ItemStack(Items.SADDLE));
        giveOrDrop(player, new ItemStack(Items.TORCH, 12));
    }

    private static void giveAdvancedKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.IRON_PICKAXE));
        giveOrDrop(player, new ItemStack(Items.IRON_AXE));
        giveOrDrop(player, new ItemStack(Items.TORCH, 64));
        giveOrDrop(player, new ItemStack(Items.COBBLESTONE, 32));
        giveOrDrop(player, new ItemStack(Items.OAK_LOG, 32));
    }

    private static void giveExplorerKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.IRON_SWORD));
        giveOrDrop(player, new ItemStack(Items.IRON_PICKAXE));
        giveOrDrop(player, new ItemStack(Items.SHIELD));
        giveOrDrop(player, new ItemStack(Items.COOKED_BEEF, 16));
        giveOrDrop(player, new ItemStack(Items.OAK_BOAT));
        giveOrDrop(player, new ItemStack(Items.COMPASS));
        giveOrDrop(player, new ItemStack(Items.WHITE_BED));
        giveOrDrop(player, new ItemStack(Items.MAP));
    }

    private static void giveAlchemistKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.SPLASH_POTION)); // Exemple, config possible
        giveOrDrop(player, new ItemStack(Items.SPLASH_POTION));
        giveOrDrop(player, new ItemStack(Items.SPLASH_POTION));
        giveOrDrop(player, new ItemStack(Items.BREWING_STAND));
        giveOrDrop(player, new ItemStack(Items.GLASS_BOTTLE, 3));
        giveOrDrop(player, new ItemStack(Items.BLAZE_POWDER));
    }

    private static void giveNatureKit(ServerPlayer player) {
        giveOrDrop(player, new ItemStack(Items.OAK_SAPLING, 16));
        giveOrDrop(player, new ItemStack(Items.CARROT, 16));
        giveOrDrop(player, new ItemStack(Items.WHEAT_SEEDS, 16));
        giveOrDrop(player, new ItemStack(Items.IRON_HOE));
        giveOrDrop(player, new ItemStack(Items.WATER_BUCKET));
        giveOrDrop(player, new ItemStack(Items.BONE_MEAL, 32));
    }
}
