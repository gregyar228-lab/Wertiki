package com.skyrotor.item;

import com.skyrotor.entity.HelicopterEntity;
import com.skyrotor.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HelicopterItem extends Item {

    public HelicopterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        Vec3 spawnPos = Vec3.atBottomCenterOf(context.getClickedPos().relative(context.getClickedFace()));

        HelicopterEntity helicopter = ModEntities.HELICOPTER.get().create(serverLevel);
        if (helicopter == null) {
            return InteractionResult.FAIL;
        }

        float yaw = player != null ? player.getYRot() : 0.0F;
        helicopter.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yaw, 0.0F);

        if (!serverLevel.noCollision(helicopter)) {
            return InteractionResult.FAIL;
        }

        serverLevel.addFreshEntity(helicopter);

        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
