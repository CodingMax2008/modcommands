package com.example.examplemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FurnaceCommand {

    private static final Map<Item, Item> smeltMap = new HashMap<>();

    static {
        // Minerais + lingots
        smeltMap.put(Items.IRON_ORE, Items.IRON_INGOT);
        smeltMap.put(Items.GOLD_ORE, Items.GOLD_INGOT);
        smeltMap.put(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        smeltMap.put(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        smeltMap.put(Items.COPPER_ORE, Items.COPPER_INGOT);
        smeltMap.put(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        smeltMap.put(Items.NETHER_GOLD_ORE, Items.GOLD_INGOT);
        smeltMap.put(Items.RAW_IRON, Items.IRON_INGOT);
        smeltMap.put(Items.RAW_GOLD, Items.GOLD_INGOT);
        smeltMap.put(Items.RAW_COPPER, Items.COPPER_INGOT);

        // Blocs divers
        smeltMap.put(Items.SAND, Items.GLASS);
        smeltMap.put(Items.COBBLESTONE, Items.STONE);
        smeltMap.put(Items.STONE, Items.SMOOTH_STONE);
        smeltMap.put(Items.NETHERRACK, Items.NETHER_BRICK);
        smeltMap.put(Items.CLAY_BALL, Items.BRICK);
        smeltMap.put(Items.CLAY, Items.TERRACOTTA);

        // Nourriture
        smeltMap.put(Items.POTATO, Items.BAKED_POTATO);
        smeltMap.put(Items.BEEF, Items.COOKED_BEEF);
        smeltMap.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        smeltMap.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        smeltMap.put(Items.MUTTON, Items.COOKED_MUTTON);
        smeltMap.put(Items.RABBIT, Items.COOKED_RABBIT);
        smeltMap.put(Items.COD, Items.COOKED_COD);
        smeltMap.put(Items.SALMON, Items.COOKED_SALMON);

        // Terracotta → émaillés
        smeltMap.put(Items.TERRACOTTA, Items.WHITE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.BLACK_TERRACOTTA, Items.BLACK_GLAZED_TERRACOTTA);
        smeltMap.put(Items.BLUE_TERRACOTTA, Items.BLUE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.BROWN_TERRACOTTA, Items.BROWN_GLAZED_TERRACOTTA);
        smeltMap.put(Items.CYAN_TERRACOTTA, Items.CYAN_GLAZED_TERRACOTTA);
        smeltMap.put(Items.GRAY_TERRACOTTA, Items.GRAY_GLAZED_TERRACOTTA);
        smeltMap.put(Items.GREEN_TERRACOTTA, Items.GREEN_GLAZED_TERRACOTTA);
        smeltMap.put(Items.LIGHT_BLUE_TERRACOTTA, Items.LIGHT_BLUE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.LIGHT_GRAY_TERRACOTTA, Items.LIGHT_GRAY_GLAZED_TERRACOTTA);
        smeltMap.put(Items.LIME_TERRACOTTA, Items.LIME_GLAZED_TERRACOTTA);
        smeltMap.put(Items.MAGENTA_TERRACOTTA, Items.MAGENTA_GLAZED_TERRACOTTA);
        smeltMap.put(Items.ORANGE_TERRACOTTA, Items.ORANGE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.PINK_TERRACOTTA, Items.PINK_GLAZED_TERRACOTTA);
        smeltMap.put(Items.PURPLE_TERRACOTTA, Items.PURPLE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.RED_TERRACOTTA, Items.RED_GLAZED_TERRACOTTA);
        smeltMap.put(Items.WHITE_TERRACOTTA, Items.WHITE_GLAZED_TERRACOTTA);
        smeltMap.put(Items.YELLOW_TERRACOTTA, Items.YELLOW_GLAZED_TERRACOTTA);

        // Bois et bûches vers charcoal
        Item[] toCharcoal = {
            Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG,
            Items.MANGROVE_LOG, Items.CHERRY_LOG, Items.BAMBOO_BLOCK, Items.CRIMSON_STEM, Items.WARPED_STEM,
            Items.OAK_WOOD, Items.SPRUCE_WOOD, Items.BIRCH_WOOD, Items.JUNGLE_WOOD, Items.ACACIA_WOOD, Items.DARK_OAK_WOOD,
            Items.MANGROVE_WOOD, Items.CHERRY_WOOD, Items.CRIMSON_HYPHAE, Items.WARPED_HYPHAE,
            Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_JUNGLE_LOG,
            Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_MANGROVE_LOG, Items.STRIPPED_CHERRY_LOG,
            Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_WARPED_STEM,
            Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_JUNGLE_WOOD,
            Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_MANGROVE_WOOD,
            Items.STRIPPED_CHERRY_WOOD, Items.STRIPPED_CRIMSON_HYPHAE, Items.STRIPPED_WARPED_HYPHAE
        };
        for (Item log : toCharcoal) {
            smeltMap.put(log, Items.CHARCOAL);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("furnace")
            .requires(source -> source.hasPermission(2))
            .executes(FurnaceCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Level world = player.level();
        ItemStack held = player.getMainHandItem();

        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Tu ne tiens rien en main !"));
            return 0;
        }

        Item smeltedItem = smeltMap.get(held.getItem());
        if (smeltedItem == null) {
            context.getSource().sendFailure(Component.literal("Cet item ne peut pas être fondu/cuit !"));
            return 0;
        }

        int count = held.getCount();
        held.shrink(count);

        ItemStack result = new ItemStack(smeltedItem, count);
        boolean added = player.getInventory().add(result);
        if (!added) {
            player.drop(result, false);
        }

        world.playSound(null, player.blockPosition(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f);

        context.getSource().sendSuccess(() ->
            Component.literal("§a" + count + " item(s) fondu(s) avec succès !"), false
        );

        return 1;
    }
}