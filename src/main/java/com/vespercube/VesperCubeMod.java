package com.vespercube;

import com.vespercube.entity.CubeTyrantEntity;
import com.vespercube.entity.RedCubeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VesperCubeMod implements ModInitializer {
    public static final String MOD_ID = "vespercube";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final RegistryKey<ItemGroup> SPAWN_EGGS_GROUP = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("spawn_eggs"));

    public static final RegistryKey<EntityType<?>> RED_CUBE_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "red_cube"));

    public static final EntityType<RedCubeEntity> RED_CUBE = Registry.register(
            Registries.ENTITY_TYPE,
            RED_CUBE_KEY,
            EntityType.Builder.create(RedCubeEntity::new, SpawnGroup.CREATURE)
                    .dimensions(2.0F, 2.0F)
                    .maxTrackingRange(10)
                    .build(RED_CUBE_KEY)
    );

    public static final RegistryKey<EntityType<?>> CUBE_TYRANT_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "cube_tyrant"));

    public static final EntityType<CubeTyrantEntity> CUBE_TYRANT = Registry.register(
            Registries.ENTITY_TYPE,
            CUBE_TYRANT_KEY,
            EntityType.Builder.create(CubeTyrantEntity::new, SpawnGroup.MONSTER)
                    .dimensions(3.2F, 3.2F)
                    .maxTrackingRange(12)
                    .build(CUBE_TYRANT_KEY)
    );

    public static final Item RED_CUBE_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "red_cube_spawn_egg"),
            new SpawnEggItem(RED_CUBE, new Item.Settings())
    );

    public static final Item CUBE_TYRANT_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "cube_tyrant_spawn_egg"),
            new SpawnEggItem(CUBE_TYRANT, new Item.Settings())
    );

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(RED_CUBE, RedCubeEntity.createRedCubeAttributes());
        FabricDefaultAttributeRegistry.register(CUBE_TYRANT, CubeTyrantEntity.createCubeTyrantAttributes());

        ItemGroupEvents.modifyEntriesEvent(SPAWN_EGGS_GROUP).register(entries -> {
            entries.add(RED_CUBE_SPAWN_EGG);
            entries.add(CUBE_TYRANT_SPAWN_EGG);
        });

        VesperCommands.register();

        LOGGER.info("Vesper Cube loaded: the cube is hungry.");
    }
}
