package com.vespercube.client;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;

/**
 * 1.21.5 renders entities from immutable render states, so the cube keeps its
 * animation inputs here instead of reading the entity during rendering.
 */
public class RedCubeRenderState extends LivingEntityRenderState {
}
