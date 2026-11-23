package com.skyrotor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.skyrotor.SkyRotorMod;
import com.skyrotor.entity.HelicopterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HelicopterRenderer extends EntityRenderer<HelicopterEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SkyRotorMod.MODID, "textures/entity/helicopter.png");
    private static final float SCALE = 1.3F;

    private final HelicopterModel model;

    public HelicopterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 1.2F;
        this.model = new HelicopterModel(context.bakeLayer(HelicopterModel.LAYER));
    }

    @Override
    public void render(HelicopterEntity entity, float entityYaw, float partialTicks, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0.0D, 1.5D * SCALE, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        pose.scale(-SCALE, -SCALE, SCALE);

        this.model.setupAnim(Mth.lerp(partialTicks, entity.rotorAnglePrev, entity.rotorAngle));
        this.model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light, OverlayTexture.NO_OVERLAY);

        pose.popPose();
        super.render(entity, entityYaw, partialTicks, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(HelicopterEntity entity) {
        return TEXTURE;
    }
}
