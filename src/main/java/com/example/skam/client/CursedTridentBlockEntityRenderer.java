package com.example.skam.client;

import com.example.skam.SkamMod;
import com.example.skam.client.CursedTridentAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class CursedTridentBlockEntityRenderer extends EntityRenderer<TridentEntity> {
    public static final Identifier CURSED_TEXTURE = new Identifier("skam", "textures/entity/cursed_trident.png");
    public static final Identifier NORMAL_TEXTURE = new Identifier("textures/entity/trident.png");

    private final TridentEntityModel normalModel;
    private final ItemRenderer itemRenderer;

    public CursedTridentBlockEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.normalModel = new TridentEntityModel(context.getPart(EntityModelLayers.TRIDENT));
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }

    @Override
    public void render(TridentEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // --- CHANGE 1: Get the level, not just a boolean ---
        // We need the level to calculate the size.
        boolean isCursed = false;
        int cursedLevel = 0;
        if (entity instanceof CursedTridentAccess access) {
            isCursed = access.skam$isCursed();
            // This assumes your CursedTridentAccess interface and Mixin have the skam$getCursedLevel() method I suggested before.
            cursedLevel = access.skam$getCursedLevel();
        }

        if (isCursed) {
            // --- CHANGE 2: Pass the level to the custom rendering method ---
            renderAsCustomModel(entity, yaw, tickDelta, matrices, vertexConsumers, light, cursedLevel);
        } else {
            renderAsNormalTrident(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    // --- CHANGE 3: The method now accepts the 'cursedLevel' ---
    private void renderAsCustomModel(TridentEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int cursedLevel) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) + 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        // --- CHANGE 4 (THE CORE FIX): Calculate scale based on level ---
        // Instead of a fixed 0.8f, we now calculate the size.
        // You can adjust these values to make it bigger or smaller.
        // A base scale of 1.0 means at level 1, it's normal size.
        float baseScale = 1.0f;
        // Each additional level adds 0.75 to the scale.
        float scalePerLevel = 0.75f;
        // Formula: Level 1 = 1.0, Level 2 = 1.75, Level 3 = 2.5
        float scale = baseScale + (cursedLevel - 1) * scalePerLevel;

        // Apply the calculated scale
        matrices.scale(scale, scale, scale);

        // Your original translation and item rendering code is UNCHANGED.
        matrices.translate(0.0, -0.5, 0.0);
        try {
            ItemStack customModelStack = new ItemStack(SkamMod.CURSED_TRIDENT_ENTITY_ITEM);
            itemRenderer.renderItem(
                    customModelStack,
                    net.minecraft.client.render.model.json.ModelTransformationMode.GROUND,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    entity.getId()
            );

        } catch (Exception e) {
            e.printStackTrace();
            renderFallbackBlock(matrices, vertexConsumers, light, entity);
        }

        matrices.pop();
    }


    // NO CHANGES TO THE METHODS BELOW THIS LINE
    private void renderFallbackBlock(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, TridentEntity entity) {
        try {
            ItemStack blockStack = new ItemStack(Items.EMERALD_BLOCK);
            itemRenderer.renderItem(
                    blockStack,
                    net.minecraft.client.render.model.json.ModelTransformationMode.GROUND,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    entity.getId()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderAsNormalTrident(TridentEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) + 90.0F));

        VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(
                vertexConsumers,
                this.normalModel.getLayer(this.getTexture(entity)),
                false,
                entity.isEnchanted()
        );

        this.normalModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(TridentEntity entity) {
        boolean cursed = false;
        if (entity instanceof CursedTridentAccess access) {
            cursed = access.skam$isCursed();
        }
        // This texture is only used by renderAsNormalTrident, so its logic is fine as is.
        return cursed ? CURSED_TEXTURE : NORMAL_TEXTURE;
    }
}
