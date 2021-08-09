package com.github.stars_sea.enderport.world.poi;

import com.github.stars_sea.enderport.EnderPortMod;
import com.github.stars_sea.enderport.mixin.accessor.BeaconBlockEntityAccessor;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LandmarkPOIType {
    public static final PointOfInterestType Landmark = PointOfInterestHelper.register(
            EnderPortMod.genId("landmark"), 0, 1, Blocks.BEACON, Blocks.CONDUIT
    );

    public static boolean isLandmark(@NotNull ServerWorld world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof BeaconBlockEntity beacon)
            return ((BeaconBlockEntityAccessor) beacon).getLevel() > 0;
        if (entity instanceof ConduitBlockEntity conduit)
            return conduit.isActive();
        return false;
    }

    public static Stream<BlockPos> getLandmarks(@NotNull ServerWorld world, BlockPos center, int radius) {
        return world.getPointOfInterestStorage().getPositions(
                Landmark.getCompletionCondition(),
                pos -> isLandmark(world, pos),
                center, radius,
                PointOfInterestStorage.OccupationStatus.ANY
        );
    }

    public static Stream<BlockPos> getSortedLandmarks(ServerWorld world, BlockPos center, int radius) {
        return getLandmarks(world, center, radius).sorted(Comparator.comparingDouble(
                pos -> pos.getSquaredDistance(center)
        ));
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

        BlockPos first = null;
        for (var it = stream.iterator(); it.hasNext(); ) {
            Map.Entry<BlockPos, Double> entry = it.next();

            if (first == null) first = entry.getKey();
            if (entry.getValue() >= 10) return Optional.of(entry.getKey());
        }
        return Optional.ofNullable(first);
    }
}
