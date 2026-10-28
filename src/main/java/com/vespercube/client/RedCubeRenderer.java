package com.vespercube.client;

import com.vespercube.entity.RedCubeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RedCubeRenderer
        extends MobEntityRenderer<RedCubeEntity, RedCubeRenderState, RedCubeModel> {
    private static final Identifier TEXTURE =
            Identifier.of("vespercube", "textures/entity/red_cube.png");

    public RedCubeRenderer(EntityRendererFactory.Context context) {
        super(context, new RedCubeModel(context.getPart(VesperCubeClient.RED_CUBE_LAYER)), 1.2F);
    }

    @Override
    public RedCubeRenderState createRenderState() {
        return new RedCubeRenderState();
    }

    @Override
    public Identifier getTexture(RedCubeRenderState state) {
        return TEXTURE;
    }
}
