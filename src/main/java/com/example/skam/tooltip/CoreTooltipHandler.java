// 文件: com.example.skam.tooltip/CoreTooltipHandler.java
package com.example.skam.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.item.TooltipContext;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class CoreTooltipHandler {

    public static void register() {
        ItemTooltipCallback.EVENT.register(CoreTooltipHandler::onItemTooltip);
    }

    private static void onItemTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        // 1. 基础检查
        if (!stack.hasNbt() || !stack.getNbt().contains("skam_mods", NbtElement.COMPOUND_TYPE)) {
            return;
        }

        boolean shiftPressed = Screen.hasShiftDown();
        List<Text> coreTooltipLines = CoreTooltipSystem.buildTooltip(stack, shiftPressed);

        if (coreTooltipLines.isEmpty()) {
            return;
        }

        // 1. 设定一个合理的默认插入位置：紧跟在物品名称之后
        int insertionIndex = 1;
        boolean anchorFound = false;

        List<Text> vanillaAttributeHeaders = List.of(
                Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY),
                Text.translatable("item.modifiers.offhand").formatted(Formatting.GRAY),
                Text.translatable("item.modifiers.head").formatted(Formatting.GRAY),
                Text.translatable("item.modifiers.chest").formatted(Formatting.GRAY),
                Text.translatable("item.modifiers.legs").formatted(Formatting.GRAY),
                Text.translatable("item.modifiers.feet").formatted(Formatting.GRAY)
        );

        // 3. 寻找锚点
        for (int i = 1; i < lines.size(); i++) {
            if (vanillaAttributeHeaders.contains(lines.get(i))) {
                insertionIndex = i;
                anchorFound = true;
                break;
            }
        }

        // 4. 【核心修改】检查并移除锚点前的空行
        // 如果找到了锚点，并且锚点不是紧跟在物品名后，且其前一行为空，则移除该空行
        if (anchorFound && insertionIndex > 0) {
            // Text.getString() 对于空Text对象会返回空字符串 ""
            if (lines.get(insertionIndex - 1).getString().isEmpty()) {
                lines.remove(insertionIndex - 1);
                // 因为我们删掉了一行，所以锚点的索引需要减1
                insertionIndex--;
            }
        }
        // 5. 在最终确定的位置插入我们的信息块
        // 确保插入位置不会超出列表范围
        if (insertionIndex > lines.size()) {
            insertionIndex = lines.size();
        }
        lines.addAll(insertionIndex, coreTooltipLines);
    }
}
