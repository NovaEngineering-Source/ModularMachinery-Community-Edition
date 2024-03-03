package kport.gugu_utils.common.envtypes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;

import java.util.List;

public class EnvBoime implements EnvironmentType {
    private final Biome[] biomes;
    private String biomeType;

    public EnvBoime(Biome[] biomes) {
        this.biomes = biomes;
    }

    public EnvBoime(BiomeManager.BiomeType type) {
        ImmutableList<BiomeManager.BiomeEntry> entries = BiomeManager.getBiomes(type);
        biomeType = type.name();
        if (entries != null)
            this.biomes = entries.stream().map(e -> e.biome).toArray(Biome[]::new);
        else this.biomes = new Biome[0];
    }

    @Override
    public boolean isMeet(World world, BlockPos pos) {
        Biome currentBiome = world.provider.getBiomeForCoords(pos);
        for (Biome biome : biomes) {
            if (biome.equals(currentBiome)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = Lists.newArrayList(I18n.format("tooltips.gugu-utils.environment.biome"));
        if (biomeType != null) {
            result.add(I18n.format("tooltips.gugu-utils.environment.biome.type", biomeType));
        }
        for (Biome biome : biomes) {
            result.add(biome.getBiomeName());
        }
        return result;
    }
    @Override
    public String getName() {
        return "biomes";
    }
}
