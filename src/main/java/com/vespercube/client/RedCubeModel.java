package com.vespercube.client;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

/**
 * The whole creature is one 32x32x32 model unit cube (2 blocks), textured with a
 * face painted on the -Z side of the texture net. Both Vesper and the boss reuse
 * it, so the model is generic over the render state it animates.
 */
public class RedCubeModel<T extends RedCubeRenderState> extends EntityModel<T> {
    private final ModelPart cube;

    public RedCubeModel(ModelPart root) {
        super(root);
        this.cube = root.getChild("cube");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("cube",
                ModelPartBuilder.create().uv(0, 0).cuboid(-16.0F, -16.0F, -16.0F, 32.0F, 32.0F, 32.0F),
                ModelTransform.of(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 128, 64);
    }

    @Override
    public void setAngles(T state) {
        this.cube.yaw = state.relativeHeadYaw * 0.017453292F;
        this.cube.pitch = state.pitch * 0.017453292F * 0.6F;
        // Slow breathing bob so the cube never looks completely static, plus a
        // small nod every time it opens its mouth.
        float bob = MathHelper.sin(state.age * 0.15F) * 1.5F;
        float nod = state.mouthOpen ? MathHelper.sin(state.age * 0.9F) * 1.2F : 0.0F;
        this.cube.originY = 16.0F + bob + nod;
    }
}
