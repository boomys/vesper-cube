package com.vespercube.client;

import com.vespercube.entity.RedCubeEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * The whole creature is one 32x32x32 model unit cube (2 blocks), textured with a
 * face painted on the -Z side of the texture net.
 */
public class RedCubeModel extends EntityModel<RedCubeEntity> {
    private final ModelPart cube;

    public RedCubeModel(ModelPart root) {
        this.cube = root.getChild("cube");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("cube",
                ModelPartBuilder.create().uv(0, 0).cuboid(-16.0F, -16.0F, -16.0F, 32.0F, 32.0F, 32.0F),
                ModelTransform.pivot(0.0F, 16.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 64);
    }

    @Override
    public void setAngles(RedCubeEntity entity, float limbAngle, float limbDistance,
                          float animationProgress, float headYaw, float headPitch) {
        this.cube.yaw = headYaw * 0.017453292F;
        this.cube.pitch = headPitch * 0.017453292F * 0.6F;
        // Slow breathing bob so the cube never looks completely static.
        this.cube.pivotY = 16.0F + MathHelper.sin(animationProgress * 0.15F) * 1.5F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        this.cube.render(matrices, vertices, light, overlay, color);
    }
}
