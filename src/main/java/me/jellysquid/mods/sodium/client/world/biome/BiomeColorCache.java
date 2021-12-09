package me.jellysquid.mods.sodium.client.world.biome;

import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ColorResolver;

import java.util.Arrays;

public class BiomeColorCache {
    private static final int BLENDED_COLORS_DIM = 16 + 2 * 2;

    private final ColorResolver resolver;
    private final WorldSlice slice;

    private final int[] blendedColors;
    private final int[] cache;

    private final int radius;
    private final int dim;

    private final int minX, minY, minZ;

    private final int blendedColorsMinX;
    private final int blendedColorsMinY;
    private final int blendedColorsMinZ;

    public BiomeColorCache(ColorResolver resolver, WorldSlice slice) {
        this.resolver = resolver;
        this.slice = slice;
        this.radius = Minecraft.getInstance().options.biomeBlendRadius;

        SectionPos origin = this.slice.getOrigin();

        this.minX = origin.minBlockX() - (this.radius + 2);
        this.minY = origin.minBlockY() - (this.radius + 2);
        this.minZ = origin.minBlockZ() - (this.radius + 2);

        this.dim = 16 + ((this.radius + 2) * 2);

        this.blendedColorsMinX = origin.minBlockX() - 2;
        this.blendedColorsMinY = origin.minBlockY() - 2;
        this.blendedColorsMinZ = origin.minBlockZ() - 2;

        this.cache = new int[this.dim * this.dim * this.dim];
        this.blendedColors = new int[BLENDED_COLORS_DIM * BLENDED_COLORS_DIM * BLENDED_COLORS_DIM];

        Arrays.fill(this.cache, -1);
        Arrays.fill(this.blendedColors, -1);
    }

    public int getBlendedColor(BlockPos pos) {
        int x2 = pos.getX() - this.blendedColorsMinX;
        int y2 = pos.getY() - this.blendedColorsMinY;
        int z2 = pos.getZ() - this.blendedColorsMinZ;

        int index = (y2 * BLENDED_COLORS_DIM * BLENDED_COLORS_DIM) + (x2 * BLENDED_COLORS_DIM) + z2;
        int color = this.blendedColors[index];

        if (color == -1) {
            this.blendedColors[index] = color = this.calculateBlendedColor(pos.getX(), pos.getY(), pos.getZ());
        }

        return color;
    }

    private int calculateBlendedColor(int posX, int posY, int posZ) {
        if (this.radius == 0) {
            return this.getColor(posX, posY, posZ);
        }

        int diameter = (this.radius * 2) + 1;
        int area = diameter * diameter * diameter;

        int r = 0;
        int g = 0;
        int b = 0;

        int minX = posX - this.radius;
        int minY = posY - this.radius;
        int minZ = posZ - this.radius;

        int maxX = posX + this.radius;
        int maxY = posY + this.radius;
        int maxZ = posZ + this.radius;

        for (int x2 = minX; x2 <= maxX; x2++) {
            for (int y2 = minY; y2 <= maxY; y2++) {
                for (int z2 = minZ; z2 <= maxZ; z2++) {
                    int color = this.getColor(x2, y2, z2);

                    r += ColorARGB.unpackRed(color);
                    g += ColorARGB.unpackGreen(color);
                    b += ColorARGB.unpackBlue(color);
                }
            }
        }

        return ColorARGB.pack(r / area, g / area, b / area, 255);
    }

    private int getColor(int x, int y, int z) {
        int index = ((y - this.minY) * this.dim * this.dim) + ((x - this.minX) * this.dim) + (z - this.minZ);
        int color = this.cache[index];

        if (color == -1) {
            this.cache[index] = color = this.calculateColor(x, y, z);
        }

        return color;
    }

    private int calculateColor(int x, int y, int z) {
        return this.resolver.getColor(this.slice.getBiome(x, y, z), x, z);
    }
}
