package com.vespercube.client;

import com.vespercube.VesperCubeMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class VesperCubeClient implements ClientModInitializer {
    public static final EntityModelLayer RED_CUBE_LAYER =
            new EntityModelLayer(Identifier.of("vespercube", "red_cube"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(RED_CUBE_LAYER, RedCubeModel::getTexturedModelData);
        EntityRendererRegistry.register(VesperCubeMod.RED_CUBE, RedCubeRenderer::new);
    }
}
