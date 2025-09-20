// IconButtonWidget.java
package com.example.skam.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButtonWidget extends ButtonWidget {
    private final Identifier iconTexture;
    private final int iconU, iconV, iconWidth, iconHeight;

    public IconButtonWidget(int x, int y, int width, int height,
                            Identifier iconTexture, int iconU, int iconV,
                            int iconWidth, int iconHeight,
                            PressAction onPress) {
        super(x, y, width, height, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.iconTexture = iconTexture;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderButton(context, mouseX, mouseY, delta);

        // 绘制图标
        RenderSystem.setShaderTexture(0, this.iconTexture);
        int iconX = this.getX() + (this.width - this.iconWidth) / 2;
        int iconY = this.getY() + (this.height - this.iconHeight) / 2;
        context.drawTexture(this.iconTexture, iconX, iconY, this.iconU, this.iconV,
                this.iconWidth, this.iconHeight);
    }
}
