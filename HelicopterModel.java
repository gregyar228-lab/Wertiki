package com.skyrotor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.skyrotor.SkyRotorMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Простая модель вертолёта: корпус, хвост, лыжи, несущий и хвостовой винты. */
public class HelicopterModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(SkyRotorMod.MODID, "helicopter"), "main");

    private final ModelPart root;
    private final ModelPart rotor;
    private final ModelPart tailRotor;

    public HelicopterModel(ModelPart root) {
        this.root = root;
        this.rotor = root.getChild("rotor");
        this.tailRotor = root.getChild("tail_rotor");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        PartPose base = PartPose.offset(0.0F, 24.0F, 0.0F);

        // Корпус и кабина (перёд = -Z)
        p.addOrReplaceChild("cabin", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-10.0F, -18.0F, -14.0F, 20.0F, 14.0F, 26.0F), base);
        p.addOrReplaceChild("canopy", CubeListBuilder.create().texOffs(0, 44)
                .addBox(-9.0F, -17.0F, -19.0F, 18.0F, 10.0F, 5.0F), base);

        // Хвост
        p.addOrReplaceChild("tail_boom", CubeListBuilder.create().texOffs(100, 0)
                .addBox(-2.0F, -16.0F, 12.0F, 4.0F, 4.0F, 22.0F), base);
        p.addOrReplaceChild("tail_fin", CubeListBuilder.create().texOffs(100, 30)
                .addBox(-0.5F, -26.0F, 30.0F, 1.0F, 12.0F, 5.0F), base);

        // Лыжи и стойки
        p.addOrReplaceChild("skid_l", CubeListBuilder.create().texOffs(130, 0)
                .addBox(-12.0F, -2.0F, -14.0F, 2.0F, 2.0F, 26.0F), base);
        p.addOrReplaceChild("skid_r", CubeListBuilder.create().texOffs(130, 0)
                .addBox(10.0F, -2.0F, -14.0F, 2.0F, 2.0F, 26.0F), base);
        p.addOrReplaceChild("struts", CubeListBuilder.create().texOffs(130, 30)
                .addBox(-11.0F, -6.0F, -8.0F, 1.0F, 4.0F, 2.0F)
                .addBox(-11.0F, -6.0F, 4.0F, 1.0F, 4.0F, 2.0F)
                .addBox(10.0F, -6.0F, -8.0F, 1.0F, 4.0F, 2.0F)
                .addBox(10.0F, -6.0F, 4.0F, 1.0F, 4.0F, 2.0F), base);

        // Мачта
        p.addOrReplaceChild("mast", CubeListBuilder.create().texOffs(60, 64)
                .addBox(-1.0F, -23.0F, -1.0F, 2.0F, 5.0F, 2.0F), base);

        // Несущий винт (вращается вокруг Y)
        p.addOrReplaceChild("rotor", CubeListBuilder.create()
                .texOffs(0, 80).addBox(-30.0F, -0.5F, -2.0F, 60.0F, 1.0F, 4.0F)
                .texOffs(0, 100).addBox(-2.0F, -0.5F, -30.0F, 4.0F, 1.0F, 60.0F),
                PartPose.offset(0.0F, 0.5F, 0.0F));

        // Хвостовой винт (вращается вокруг X)
        p.addOrReplaceChild("tail_rotor", CubeListBuilder.create()
                .texOffs(0, 64).addBox(-0.5F, -5.0F, -1.0F, 1.0F, 10.0F, 2.0F)
                .texOffs(30, 64).addBox(-0.5F, -1.0F, -5.0F, 1.0F, 2.0F, 10.0F),
                PartPose.offset(2.5F, 8.0F, 32.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    public void setupAnim(float rotorAngleDeg) {
        float rad = (float) Math.toRadians(rotorAngleDeg);
        this.rotor.yRot = rad;
        this.tailRotor.xRot = rad * 1.5F;
    }

    public void render(PoseStack pose, VertexConsumer buffer, int light, int overlay) {
        this.root.render(pose, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
