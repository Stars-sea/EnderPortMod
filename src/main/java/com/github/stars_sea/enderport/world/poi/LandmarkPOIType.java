package com.github.stars_sea.enderport.world.poi;

import com.github.stars_sea.enderport.EnderPortMod;
import com.github.stars_sea.enderport.mixin.accessor.BeaconBlockEntityAccessor;
import com.github.stars_sea.enderport.world.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LandmarkPOIType {
    public static final RegistryKey<PointOfInterestType> LANDMARK = RegistryKey.of(
            RegistryKeys.POINT_OF_INTEREST_TYPE,
            EnderPortMod.genId("landmark")
    );

    public static boolean isLandmark(@NotNull ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.LODESTONE))
            return true;
        if (Location.isSameWorld(World.NETHER, world) &&
                state.isOf(Blocks.RESPAWN_ANCHOR) &&
                state.get(RespawnAnchorBlock.CHARGES) != 0)
            return true;

        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BeaconBlockEntity beacon)
            return ((BeaconBlockEntityAccessor) beacon).getLevel() > 0;
        if (entity instanceof ConduitBlockEntity conduit)
            return conduit.isActive();
        return false;
    }

    public static Stream<BlockPos> getLandmarks(@NotNull ServerWorld world, BlockPos center, int radius) {
        return world.getPointOfInterestStorage().getPositions(
                type -> type.matchesKey(LANDMARK) ||
                        type.matchesKey(PointOfInterestTypes.LODESTONE),
                pos -> isLandmark(world, pos),
                center, radius,
                PointOfInterestStorage.OccupationStatus.ANY
        );
    }

    /**
     * 取得最近的地标 (10 格之外优先)
     * @return 最近的地标 (10 格之外优先)
     */
    @NotNull
    public static Optional<BlockPos> getNearestLandmark(ServerWorld world, BlockPos center, int radius) {
        Stream<Map.Entry<BlockPos, Double>> stream = getLandmarks(world, center, radius)
                .map(pos -> Map.entry(pos, pos.getSquaredDistance(center)))
                .sorted(Comparator.comparingDouble(Map.Entry::getValue));

        BlockPos nearest = null;
        for (var it = stream.iterator(); it.hasNext(); ) {
            Map.Entry<BlockPos, Double> entry = it.next();
            BlockPos pos = entry.getKey();

            if (entry.getValue() >= 10)
                return Optional.of(pos);
            else nearest = pos;
        }
        return Optional.ofNullable(nearest);
    }
}
