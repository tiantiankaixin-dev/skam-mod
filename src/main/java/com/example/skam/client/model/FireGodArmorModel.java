// 文件路径: src/main/java/com/example/skam/client/model/FireGodArmorModel.java
package com.example.skam.client.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public class FireGodArmorModel<T extends LivingEntity> extends BipedEntityModel<T> {

	private final ModelPart head;
	private final ModelPart hat;
	private final ModelPart body;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public FireGodArmorModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.hat = root.getChild("hat");
		this.body = root.getChild("body");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");

	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();


		root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(1.0F)),
				ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1047F, 0.0873F, 0.0F));
		root.addChild("hat", ModelPartBuilder.create().uv(64, 16).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(1.5F)),
				ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1047F, 0.0873F, 0.0F));

		ModelPartData body = root.addChild("body", ModelPartBuilder.create().uv(0, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(1.01F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		body.addChild("body2", ModelPartBuilder.create().uv(80, 20).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F))
						.uv(56, 40).cuboid(-1.0F, 2.0F, 2.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
						.uv(48, 58).cuboid(-3.0F, 2.0F, 2.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
						.uv(60, 32).cuboid(-5.0F, 2.0F, 3.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(56, 58).cuboid(1.0F, 2.0F, 2.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
						.uv(60, 36).cuboid(-7.0F, 2.0F, 4.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(8, 62).cuboid(3.0F, 2.0F, 3.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(62, 12).cuboid(5.0F, 2.0F, 4.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(62, 48).cuboid(3.0F, 4.0F, 2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(62, 52).cuboid(2.0F, 3.0F, 2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(64, 40).cuboid(-5.0F, 4.0F, 2.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(48, 12).cuboid(7.0F, 1.0F, 6.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(64, 44).cuboid(5.0F, 0.0F, 5.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(64, 56).cuboid(6.0F, 3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(64, 60).cuboid(-7.0F, 0.0F, 5.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 48).cuboid(-12.0F, 1.0F, 6.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(64, 64).cuboid(-8.0F, 3.0F, 6.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(32, 40).cuboid(4.0F, -1.0F, 6.0F, 10.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(32, 44).cuboid(6.0F, -3.0F, 6.0F, 10.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(48, 4).cuboid(11.0F, -5.0F, 6.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 52).cuboid(8.0F, 3.0F, 6.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 48).cuboid(-14.0F, -1.0F, 6.0F, 10.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(48, 0).cuboid(-16.0F, -3.0F, 6.0F, 10.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(48, 8).cuboid(-18.0F, -5.0F, 6.0F, 7.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 52).cuboid(-13.0F, 3.0F, 6.0F, 5.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 24).cuboid(-15.0F, 5.0F, 7.0F, 11.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 28).cuboid(4.0F, 5.0F, 7.0F, 11.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(32, 32).cuboid(-17.0F, 7.0F, 7.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 16).cuboid(5.0F, 7.0F, 7.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(32, 36).cuboid(-19.0F, 9.0F, 7.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 20).cuboid(7.0F, 9.0F, 7.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(24, 48).cuboid(-12.0F, 11.0F, 7.0F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F))
						.uv(32, 48).cuboid(10.0F, 11.0F, 7.0F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 65).cuboid(-18.0F, 11.0F, 7.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(38, 65).cuboid(16.0F, 11.0F, 8.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
						.uv(22, 60).cuboid(14.0F, 11.0F, 8.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
						.uv(30, 60).cuboid(-16.0F, 11.0F, 7.0F, 2.0F, 6.0F, 2.0F, new Dilation(0.0F))
						.uv(14, 52).cuboid(-14.0F, 11.0F, 7.0F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F))
						.uv(54, 48).cuboid(12.0F, 11.0F, 8.0F, 2.0F, 8.0F, 2.0F, new Dilation(0.0F))
						.uv(0, 56).cuboid(-10.0F, 11.0F, 8.0F, 2.0F, 7.0F, 2.0F, new Dilation(0.0F))
						.uv(40, 56).cuboid(8.0F, 11.0F, 8.0F, 2.0F, 7.0F, 2.0F, new Dilation(0.0F)),
				ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		root.addChild("right_arm", ModelPartBuilder.create().uv(24, 16).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(1.0F)), ModelTransform.of(-5.0F, 2.0F, 0.0F, -0.1745F, 0.0F, 0.0F));
		root.addChild("left_arm", ModelPartBuilder.create().uv(0, 32).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(1.0F)), ModelTransform.of(5.0F, 2.0F, 0.0F, 0.2094F, 0.0F, 0.0F));
		root.addChild("right_leg", ModelPartBuilder.create().uv(32, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(1.0F)), ModelTransform.of(-1.9F, 12.0F, 0.0F, 0.192F, 0.0F, 0.0349F));
		root.addChild("left_leg", ModelPartBuilder.create().uv(16, 32).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(1.0F)), ModelTransform.of(1.9F, 12.0F, 0.0F, -0.1745F, 0.0F, -0.0349F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
	}

	public void setVisible(EquipmentSlot slot) {
		this.head.visible = false;
		this.hat.visible = false;
		this.body.visible = false;
		this.rightArm.visible = false;
		this.leftArm.visible = false;
		this.rightLeg.visible = false;
		this.leftLeg.visible = false;

		switch (slot) {
			case HEAD:
				this.head.visible = true;
				this.hat.visible = true;
				break;
			case CHEST:
				this.body.visible = true;
				this.rightArm.visible = true;
				this.leftArm.visible = true;
				break;
			case LEGS:
				this.body.visible = true;
				this.rightLeg.visible = true;
				this.leftLeg.visible = true;
				break;
			case FEET:
				break;
			default:
				break;
		}
	}
}


