package com.vespercube.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * A 5x5 hut Vesper places block by block: cobblestone floor, plank walls with
 * glass windows, a two block doorway and a flat plank roof.
 */
public final class HouseBlueprint {
    private HouseBlueprint() {
    }

    public record Piece(BlockPos pos, BlockState state) {
    }

    public static List<Piece> smallHouse(BlockPos origin) {
        List<Piece> pieces = new ArrayList<>();

        BlockState floor = Blocks.COBBLESTONE.getDefaultState();
        BlockState wall = Blocks.OAK_PLANKS.getDefaultState();
        BlockState glass = Blocks.GLASS.getDefaultState();
        BlockState roof = Blocks.OAK_PLANKS.getDefaultState();

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                pieces.add(new Piece(origin.add(x, 0, z), floor));
            }
        }

        for (int y = 1; y <= 3; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    boolean edge = x == 0 || x == 4 || z == 0 || z == 4;
                    if (!edge) {
                        continue;
                    }
                    boolean doorway = x == 2 && z == 4 && y <= 2;
                    if (doorway) {
                        continue;
                    }
                    boolean window = y == 2 && ((x + z) % 2 == 0);
                    pieces.add(new Piece(origin.add(x, y, z), window ? glass : wall));
                }
            }
        }

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                pieces.add(new Piece(origin.add(x, 4, z), roof));
            }
        }

        return pieces;
    }
}
