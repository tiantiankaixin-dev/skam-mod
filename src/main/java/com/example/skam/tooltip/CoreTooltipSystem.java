package com.example.skam.tooltip;

import com.example.skam.item.core.CoreType;
import com.example.skam.nbt.CoreBonusData;
import com.example.skam.nbt.NbtInjector;
import com.example.skam.util.CoreNbtApplicator;
import com.example.skam.util.SkamAttributeConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class CoreTooltipSystem {

    // ... (内部类 AttributeRenderer 和 ATTRIBUTE_RENDERERS 列表保持不变)
    private record AttributeRenderer(
            BiFunction<SkamAttributeConfig.CoreAttributes, Integer, Double> valueExtractor,
            String translationKey,
            String format
    ) {
        public Text render(SkamAttributeConfig.CoreAttributes attributes, int level, Formatting lineFormatting) {
            double value = valueExtractor.apply(attributes, level);
            if (value == 0) return null;

            String sign = value > 0 ? "+" : "";
            Text prefix = Text.literal(" › ").formatted(Formatting.DARK_GRAY);

            Text valueText;
            if (format.contains("%%")) {
                valueText = Text.literal(sign + String.format(format, value * 100));
            } else {
                valueText = Text.literal(sign + String.format(format, value));
            }

            MutableText mainPart = valueText.copy()
                    .append("")
                    .append(Text.translatable(translationKey));

            return prefix.copy().append(mainPart.formatted(lineFormatting));
        }
    }

    private static final List<AttributeRenderer> ATTRIBUTE_RENDERERS = List.of(
            new AttributeRenderer((attr, lvl) -> attr.damage_per_level * lvl, "attribute.name.generic.attack_damage", "%.2f"),
            new AttributeRenderer((attr, lvl) -> attr.attack_speed_per_level * lvl, "attribute.name.generic.attack_speed", "%.2f"),
            new AttributeRenderer((attr, lvl) -> attr.attack_knockback_per_level * lvl, "attribute.name.generic.attack_knockback", "%.2f"),
            new AttributeRenderer((attr, lvl) -> attr.health_per_level * lvl, "attribute.name.generic.max_health", "%.1f"),
            new AttributeRenderer((attr, lvl) -> attr.armor_per_level * lvl, "attribute.name.generic.armor", "%.1f"),
            new AttributeRenderer((attr, lvl) -> attr.armor_toughness_per_level * lvl, "attribute.name.generic.armor_toughness", "%.1f"),
            new AttributeRenderer((attr, lvl) -> attr.knockback_resistance_per_level * lvl, "attribute.name.generic.knockback_resistance", "%.1f"),
            new AttributeRenderer((attr, lvl) -> attr.movement_speed_per_level * lvl, "attribute.name.generic.movement_speed", "%.3f"),
            new AttributeRenderer((attr, lvl) -> attr.luck_per_level * lvl, "attribute.name.generic.luck", "%.1f"),
            new AttributeRenderer((attr, lvl) -> attr.projectile_damage_per_level * lvl, "tooltip.skam.projectile_damage", "%.2f"),
            new AttributeRenderer((attr, lvl) -> attr.projectile_speed_per_level * lvl, "tooltip.skam.projectile_speed", "%.0f%%")
    );


    private static Text createSeparator() {
       return Text.literal("--------------------").formatted(Formatting.GOLD);
    }

    public static List<Text> buildTooltip(ItemStack stack, boolean isShiftPressed) {
        List<Text> tooltipLines = new ArrayList<>();

        // 1. 优先获取物品的核心数据和最大容量
        int maxCapacity = CoreNbtApplicator.getMaxCoreCapacity(stack);
        Map<CoreType, Integer> appliedCores = CoreNbtApplicator.readAppliedCoresFromStack(stack);

        // 2. 修改提前退出的条件：只有当容量为0且没有任何核心时，才不显示任何信息
        if (maxCapacity == 0 && appliedCores.isEmpty()) {
            return tooltipLines;
        }

        // 3. 无论是否有核心，只要容量大于0，就显示容量信息
        int currentTotalLevel = appliedCores.values().stream().mapToInt(Integer::intValue).sum();
        tooltipLines.add(Text.translatable("tooltip.skam.core_capacity", currentTotalLevel, maxCapacity)
                .formatted(Formatting.YELLOW));

        // 4. 仅当存在已应用的核心时，才显示核心详情
        if (!appliedCores.isEmpty()) {
            if (!isShiftPressed) {
                tooltipLines.add(Text.translatable("tooltip.skam.shift_for_details").formatted(Formatting.LIGHT_PURPLE));
            }

            tooltipLines.add(Text.translatable("tooltip.skam.applied_cores").formatted(Formatting.WHITE, Formatting.BOLD));

            SkamAttributeConfig config = SkamAttributeConfig.get();

            appliedCores.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .forEach(entry -> {
                        CoreType type = entry.getKey();
                        int effectiveLevel = Math.min(entry.getValue(), type.getMaxLevel());
                        SkamAttributeConfig.CoreAttributes attributes = config.getAttributesFor(type);
                        Formatting coreThemeColor = type.getColor();

                        MutableText coreNameLine = Text.literal(" ")
                                .append(Text.literal(String.valueOf(type.getIconChar())).formatted(coreThemeColor))
                                .append(" ")
                                .append(Text.translatable(type.getNameTranslationKey()).formatted(coreThemeColor))
                                .append(Text.literal(" (等级 " + effectiveLevel + ")").formatted(Formatting.DARK_GRAY));

                        if (effectiveLevel >= type.getMaxLevel() && type.getMaxLevel() != Integer.MAX_VALUE) {
                            coreNameLine.append(Text.literal(" [MAX]").formatted(Formatting.GOLD, Formatting.BOLD));
                        }
                        tooltipLines.add(coreNameLine);

                        if (isShiftPressed) {
                            ATTRIBUTE_RENDERERS.forEach(renderer -> {
                                Text line = renderer.render(attributes, effectiveLevel, coreThemeColor);
                                if (line != null) {
                                    tooltipLines.add(line);
                                }
                            });
                            addSpecialEffectLore(tooltipLines, type, attributes, effectiveLevel, coreThemeColor);
                        }
                    });

            if (isShiftPressed) {
                CoreBonusData totalData = NbtInjector.calculateTotalBonuses(appliedCores);
                if (totalData.totalCritChanceBonus > 0) {
                    tooltipLines.add(createSeparator());
                    MutableText summaryTitle = Text.literal(" ")
                            .append(Text.translatable("tooltip.skam.summary").formatted(Formatting.WHITE, Formatting.BOLD));
                    tooltipLines.add(summaryTitle);
                    Text critText = Text.translatable("tooltip.skam.crit_chance_bonus", String.format("%.0f%%", totalData.totalCritChanceBonus * 100))
                            .formatted(Formatting.LIGHT_PURPLE);
                    Text prefix = Text.literal(" › ").formatted(Formatting.DARK_GRAY);
                    tooltipLines.add(prefix.copy().append(critText));
                }
            }
        }

        return tooltipLines;
    }

    // ... (addSpecialEffectLore 方法保持不变)
    private static void addSpecialEffectLore(List<Text> tooltipLines, CoreType type, SkamAttributeConfig.CoreAttributes attributes, int level, Formatting coreColor) {
        Text prefix = Text.literal(" › ").formatted(Formatting.DARK_GRAY);
        MutableText effectLine = null;

        if (type == CoreType.VAMPIRE) {
            double chance = attributes.vampire_chance_per_level * level;
            double minHeal = attributes.vampire_min_heal_per_level * level;
            double maxHeal = attributes.vampire_max_heal_per_level * level;
            if (chance > 0 && maxHeal > 0) {
                effectLine = Text.translatable("tooltip.skam.vampire_effect",
                                String.format("%.0f%%", chance * 100),
                                String.format("%.1f", minHeal),
                                String.format("%.1f", maxHeal))
                        .formatted(coreColor);
            }
        }

        if (type == CoreType.STRENGTH) {
            double bonus = attributes.damage_multiplier_per_level * level;
            if (bonus > 0) {
                effectLine = Text.literal("+" + String.format("%.0f%%", bonus * 100))
                        .append(" ")
                        .append(Text.translatable("tooltip.skam.strength_effect"))
                        .formatted(coreColor);
            }
        }

        if (effectLine != null) {
            tooltipLines.add(prefix.copy().append(effectLine));
        }
    }
}
