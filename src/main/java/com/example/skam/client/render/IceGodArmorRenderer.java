package com.example.skam.client.render;

import com.example.skam.SkamMod;
import com.example.skam.client.SkamModClient;
import com.example.skam.client.model.FireGodArmorModel; // 复用火神套装的模型类
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class IceGodArmorRenderer implements net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer {

    private static final Identifier TEXTURE = new Identifier("skam", "textures/models/armor/ice_god_armor.png");
    private FireGodArmorModel<LivingEntity> armorModel;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        if (this.armorModel == null) {
            this.armorModel = new FireGodArmorModel<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(SkamModClient.ICE_GOD_ARMOR_LAYER));
        }

        contextModel.copyBipedStateTo(this.armorModel);
        this.armorModel.setVisible(slot);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(TEXTURE));
        this.armorModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
