package com.vespercube.client;

import com.vespercube.entity.CubeTyrantEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CubeTyrantRenderer extends MobEntityRenderer<CubeTyrantEntity, CubeTyrantRenderState,
        RedCubeModel<CubeTyrantRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.of("vespercube", "textures/entity/cube_tyrant.png");
    private static final Identifier TALKING_TEXTURE =
            Identifier.of("vespercube", "textures/entity/cube_tyrant_talking.png");

    public CubeTyrantRenderer(EntityRendererFactory.Context context) {
        super(context, new RedCubeModel<CubeTyrantRenderState>(context.getPart(VesperCubeClient.RED_CUBE_LAYER)), 2.0F);
    }

    @Override
    public CubeTyrantRenderState createRenderState() {
        return new CubeTyrantRenderState();
    }

    @Override
    public void updateRenderState(CubeTyrantEntity entity, CubeTyrantRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.mouthOpen = entity.isTalking() && entity.age % 6 < 3;
    }

    @Override
    public void scale(CubeTyrantRenderState state, MatrixStack matrices) {
        matrices.scale(1.6F, 1.6F, 1.6F);
    }

    @Override
    public Identifier getTexture(CubeTyrantRenderState state) {
        return state.mouthOpen ? TALKING_TEXTURE : TEXTURE;
    }
}
