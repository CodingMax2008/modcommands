package com.example.examplemod;

import org.slf4j.Logger;

import com.example.examplemod.commands.FurnaceCommand;
import com.example.examplemod.commands.KitCommand;
import com.example.examplemod.commands.TopCommand;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExampleMod.MODID)
public class ExampleMod
{
    // ID du mod (doit correspondre au fichier mods.toml)
    public static final String MODID = "examplemod";
    // Logger pour afficher des messages dans la console
    private static final Logger LOGGER = LogUtils.getLogger();

    // Deferred Registers pour enregistrer blocs, items et onglets créatifs au bon moment
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Enregistrement d’un bloc custom simple
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Enregistrement de l’item correspondant au bloc (pour le prendre en main)
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Enregistrement d’un item custom (ici un aliment avec nutrition et saturation)
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEat().nutrition(1).saturationMod(2f).build())));

    // Création d’un onglet créatif personnalisé où on mettra nos items
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT) // Positionnement avant l’onglet Combat
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()) // Icône de l’onglet
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get()); // Ajout de l’item dans l’onglet
            }).build());

    // Constructeur du mod, c’est ici qu’on enregistre tout ce qu’on veut écouter ou initialiser
    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Inscription des méthodes d'initialisation
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onRegisterCommands); // Écouteur pour enregistrer les commandes

        // Enregistrement des registres (blocs, items, onglets)
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Inscription sur le bus d’événements Minecraft Forge pour recevoir des événements serveur/général
        MinecraftForge.EVENT_BUS.register(this);

        // Enregistrement de la configuration du mod
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // Méthode appelée lors de la phase de setup commun (client + serveur)
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM COMMON SETUP");

        // Affiche dans la console la clé du bloc dirt si configuré
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        // Log des items configurés (exemple)
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Ajoute notre bloc custom à l’onglet créatif des blocs de construction
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // Enregistre nos commandes personnalisées dans Minecraft
    private void onRegisterCommands(RegisterCommandsEvent event)
    {
        // Appelle la méthode static register() de chaque commande pour les enregistrer
        TopCommand.register(event.getDispatcher());
        FurnaceCommand.register(event.getDispatcher());
        KitCommand.register(event.getDispatcher());

        // Log pour confirmer l'enregistrement
        LOGGER.info("Commandes enregistrées");
    }

    // Exemple d’événement serveur, ici on log simplement un message au démarrage
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    // Classe statique pour gérer les événements côté client uniquement
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        // Méthode appelée lors du setup client
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
