// src/main/java/com/example/skam/client/render/FallingSwordEntityRenderer.java
package com.example.skam.client.render;

import com.example.skam.entity.FallingSwordEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class FallingSwordEntityRenderer extends EntityRenderer<FallingSwordEntity> {
    private final ItemRenderer itemRenderer;

    public FallingSwordEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FallingSwordEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        ItemStack swordStack = getSwordForLevel(entity.getEnchantmentLevel());

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(315));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0));

        matrices.scale(7.5f, 7.5f, 7.5f);

        matrices.translate(0, -0.1, 0);

        this.itemRenderer.renderItem(
                swordStack,
                ModelTransformationMode.FIXED,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                entity.getId()
        );

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private ItemStack getSwordForLevel(int level) {
        return switch (level) {
            case 1 -> new ItemStack(Items.WOODEN_SWORD);
            case 2 -> new ItemStack(Items.STONE_SWORD);
            case 3 -> new ItemStack(Items.IRON_SWORD);
            case 4 -> new ItemStack(Items.DIAMOND_SWORD);
            case 5 -> new ItemStack(Items.NETHERITE_SWORD);
            default -> new ItemStack(Items.WOODEN_SWORD);
        };
    }

    @Override
    public Identifier getTexture(FallingSwordEntity entity) {
        return null; // 使用物品渲染器，不需要纹理
    }
}