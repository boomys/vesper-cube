package com.vespercube.client;

import com.vespercube.entity.RedCubeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RedCubeRenderer extends MobEntityRenderer<RedCubeEntity, RedCubeModel> {
    private static final Identifier TEXTURE =
            Identifier.of("vespercube", "textures/entity/red_cube.png");

    public RedCubeRenderer(EntityRendererFactory.Context context) {
        super(context, new RedCubeModel(context.getPart(VesperCubeClient.RED_CUBE_LAYER)), 1.2F);
    }

    @Override
    public Identifier getTexture(RedCubeEntity entity) {
        return TEXTURE;
    }
}
