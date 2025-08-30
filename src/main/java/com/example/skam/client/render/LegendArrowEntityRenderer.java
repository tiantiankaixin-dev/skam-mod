// src/main/java/com/example/skam/client/render/LegendArrowEntityRenderer.java
package com.example.skam.client.render;

import com.example.skam.entity.LegendArrowEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;

public class LegendArrowEntityRenderer extends ProjectileEntityRenderer<LegendArrowEntity> {
    public static final Identifier TEXTURE = new Identifier("minecraft", "textures/entity/projectiles/arrow.png");

    public LegendArrowEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(LegendArrowEntity entity) {
        return TEXTURE;
    }
}
