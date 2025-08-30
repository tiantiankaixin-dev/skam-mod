package com.example.skam.client.render;

import com.example.skam.SkamMod;
import com.example.skam.entity.LightningEmperorEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.DrownedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class LightningEmperorRenderer extends MobEntityRenderer<LightningEmperorEntity, DrownedEntityModel<LightningEmperorEntity>> {
    private static final Identifier TEXTURE = new Identifier(SkamMod.MOD_ID, "textures/entity/emperorlightning.png");

    public LightningEmperorRenderer(EntityRendererFactory.Context context) {
        super(context, new DrownedEntityModel<>(context.getPart(EntityModelLayers.DROWNED)), 0.7f);
    }

    @Override
    public Identifier getTexture(LightningEmperorEntity entity) {
        return TEXTURE;
    }
}