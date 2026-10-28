package com.vespercube.client;

import com.vespercube.entity.RedCubeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RedCubeRenderer
        extends MobEntityRenderer<RedCubeEntity, RedCubeRenderState, RedCubeModel<RedCubeRenderState>> {
    private static final Identifier TEXTURE =
            Identifier.of("vespercube", "textures/entity/red_cube.png");
    private static final Identifier TALKING_TEXTURE =
            Identifier.of("vespercube", "textures/entity/red_cube_talking.png");

    public RedCubeRenderer(EntityRendererFactory.Context context) {
        super(context, new RedCubeModel<RedCubeRenderState>(context.getPart(VesperCubeClient.RED_CUBE_LAYER)), 1.2F);
    }

    @Override
    public RedCubeRenderState createRenderState() {
        return new RedCubeRenderState();
    }

    @Override
    public void updateRenderState(RedCubeEntity entity, RedCubeRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        // Flap the mouth about five times a second while Vesper is speaking.
        state.mouthOpen = entity.isTalking() && entity.age % 8 < 4;
    }

    @Override
    public Identifier getTexture(RedCubeRenderState state) {
        return state.mouthOpen ? TALKING_TEXTURE : TEXTURE;
    }
}
