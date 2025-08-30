package com.example.skam.entity.client;

import com.example.skam.Skam;
import com.example.skam.entity.FloatingShipEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class FloatingShipRenderer extends EntityRenderer<FloatingShipEntity> {
    private final BakedModel model;
    public FloatingShipRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        ModelIdentifier modelId = new ModelIdentifier(new Identifier(Skam.MOD_ID, "entity/floating_ship"), "inventory");
        this.model = MinecraftClient.getInstance().getBakedModelManager().getModel(modelId);
    }
    @Override
    public Identifier getTexture(FloatingShipEntity entity) {
        return new Identifier("minecraft", "textures/block/stone.png");
    }

    @Override
    public void render(FloatingShipEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.5, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - yaw));
        matrices.translate(-0.5, -0.5, -0.5);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getSolid());
        renderBakedModel(matrices, vertexConsumer, model, light);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    private void renderBakedModel(MatrixStack matrices, VertexConsumer vertices, BakedModel model, int light) {
        Random random = Random.create();
        MatrixStack.Entry entry = matrices.peek();
        List<BakedQuad> quads = model.getQuads(null, null, random);
        for (BakedQuad quad : quads) {
            vertices.quad(entry, quad, 1.0f, 1.0f, 1.0f, light, OverlayTexture.DEFAULT_UV);
        }
        for (Direction direction : Direction.values()) {
            quads = model.getQuads(null, direction, random);
            for (BakedQuad quad : quads) {
                vertices.quad(entry, quad, 1.0f, 1.0f, 1.0f, light, OverlayTexture.DEFAULT_UV);
            }
        }
    }
}
