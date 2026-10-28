package com.vespercube;

import com.vespercube.entity.CubeTyrantEntity;
import com.vespercube.entity.RedCubeEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;

import java.util.Set;

/**
 * {@code /vesper say <text>} makes the companion talk (mouth animation),
 * {@code /vesper arena} teleports you and Vesper into the arena dimension and
 * wakes the boss, {@code /vesper leave} brings you back to the overworld.
 */
public final class VesperCommands {
    public static final RegistryKey<net.minecraft.world.World> ARENA = RegistryKey.of(
            RegistryKeys.WORLD, Identifier.of(VesperCubeMod.MOD_ID, "arena"));

    private static final int ARENA_Y = 64;

    private VesperCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("vesper")
                        .then(CommandManager.literal("say")
                                .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                        .executes(context -> say(context.getSource(),
                                                StringArgumentType.getString(context, "text")))))
                        .then(CommandManager.literal("arena")
                                .executes(context -> arena(context.getSource())))
                        .then(CommandManager.literal("leave")
                                .executes(context -> leave(context.getSource())))));
    }

    private static int say(ServerCommandSource source, String text) {
        RedCubeEntity cube = nearestCube(source);
        if (cube == null) {
            source.sendError(Text.translatable("command.vesper.no_cube"));
            return 0;
        }
        cube.speak(text);
        return 1;
    }

    private static int arena(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.translatable("command.vesper.players_only"));
            return 0;
        }

        ServerWorld arena = source.getServer().getWorld(ARENA);
        if (arena == null) {
            source.sendError(Text.translatable("command.vesper.no_arena"));
            return 0;
        }

        buildArenaFloor(arena);

        player.teleport(arena, 0.5, ARENA_Y + 1, 6.5, Set.of(), 180.0F, 0.0F, true);
        player.sendMessage(Text.translatable("command.vesper.arena.enter"), false);

        RedCubeEntity cube = nearestCube(source);
        if (cube != null) {
            spawnCompanion(cube, arena, 2.5, ARENA_Y + 1, 8.5);
        }

        boolean bossAlive = !arena.getEntitiesByClass(CubeTyrantEntity.class,
                new Box(-64, -64, -64, 64, 320, 64), CubeTyrantEntity::isAlive).isEmpty();

        if (!bossAlive) {
            CubeTyrantEntity boss = VesperCubeMod.CUBE_TYRANT.create(arena, SpawnReason.COMMAND);
            if (boss != null) {
                boss.refreshPositionAndAngles(0.5, ARENA_Y + 1, -14.5, 0.0F, 0.0F);
                arena.spawnEntity(boss);
                player.sendMessage(Text.translatable("entity.vespercube.cube_tyrant.awaken"), false);
            }
        }
        return 1;
    }

    private static int leave(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.translatable("command.vesper.players_only"));
            return 0;
        }

        RedCubeEntity cube = nearestCube(source);
        ServerWorld overworld = source.getServer().getOverworld();
        BlockPos spawn = overworld.getSpawnPos();
        int y = overworld.getTopY(Heightmap.Type.MOTION_BLOCKING, spawn.getX(), spawn.getZ());

        player.teleport(overworld, spawn.getX() + 0.5, y, spawn.getZ() + 0.5,
                Set.of(), player.getYaw(), player.getPitch(), true);
        player.sendMessage(Text.translatable("command.vesper.arena.leave"), false);

        if (cube != null) {
            spawnCompanion(cube, overworld, spawn.getX() + 2.5, y, spawn.getZ() + 0.5);
        }
        return 1;
    }

    /**
     * There is no cross-dimension move helper for arbitrary entities in 1.21.5,
     * so the companion is recreated in the destination world.
     */
    private static void spawnCompanion(RedCubeEntity original, ServerWorld world,
                                       double x, double y, double z) {
        RedCubeEntity copy = VesperCubeMod.RED_CUBE.create(world, SpawnReason.COMMAND);
        if (copy == null) {
            return;
        }
        copy.refreshPositionAndAngles(x, y, z, original.getYaw(), 0.0F);
        copy.setPersistent();
        world.spawnEntity(copy);
        original.discard();
    }

    private static void buildArenaFloor(ServerWorld arena) {
        for (int x = -12; x <= 12; x++) {
            for (int z = -20; z <= 14; z++) {
                arena.setBlockState(new BlockPos(x, ARENA_Y - 1, z),
                        Blocks.POLISHED_BLACKSTONE.getDefaultState(), 3);
                for (int y = ARENA_Y; y <= ARENA_Y + 6; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!arena.getBlockState(pos).isAir()) {
                        arena.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }

        // Four pillars so the arena reads as a room rather than a floating plate.
        for (int[] corner : new int[][]{{-11, -19}, {11, -19}, {-11, 13}, {11, 13}}) {
            for (int y = ARENA_Y; y < ARENA_Y + 7; y++) {
                arena.setBlockState(new BlockPos(corner[0], y, corner[1]),
                        Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), 3);
            }
        }
    }

    private static RedCubeEntity nearestCube(ServerCommandSource source) {
        ServerWorld world = source.getWorld();
        BlockPos origin = BlockPos.ofFloored(source.getPosition());

        return world.getEntitiesByClass(RedCubeEntity.class,
                        new Box(origin).expand(64.0), RedCubeEntity::isAlive)
                .stream()
                .min((a, b) -> Double.compare(a.squaredDistanceTo(source.getPosition()),
                        b.squaredDistanceTo(source.getPosition())))
                .orElse(null);
    }
}
