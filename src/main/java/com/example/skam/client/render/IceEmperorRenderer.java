package com.example.skam.client.render;

import com.example.skam.SkamMod;
import com.example.skam.entity.IceEmperorEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

public class IceEmperorRenderer extends MobEntityRenderer<IceEmperorEntity, ZombieEntityModel<IceEmperorEntity>> {
    private static final Identifier TEXTURE = new Identifier(SkamMod.MOD_ID, "textures/entity/emperorice.png");

    public IceEmperorRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.7f);
    }

    @Override
    public Identifier getTexture(IceEmperorEntity entity) {
        return TEXTURE;
    }
}