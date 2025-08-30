package com.example.skam.command;

import com.example.skam.util.CoreNbtApplicator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// 无需导入 Map

public class SkamCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("setcorecapacity")
                .requires(source -> source.hasPermissionLevel(2)) // 仅限 OP (或权限等级2及以上) 使用
                .then(CommandManager.argument("capacity", IntegerArgumentType.integer(0)) // 容量参数，最小为0
                        .executes(SkamCommands::setCapacity)));
    }

    private static int setCapacity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ItemStack stack = player.getMainHandStack();
        int capacity = IntegerArgumentType.getInteger(context, "capacity");

        if (stack.isEmpty()) {
            context.getSource().sendError(Text.literal("你必须在主手上持有物品！").formatted(Formatting.RED));
            return 0;
        }

        // 获取或创建 NBT
        NbtCompound rootNbt = stack.getOrCreateNbt();
        NbtCompound skamNbt = rootNbt.contains("skam_mods", 10) ? rootNbt.getCompound("skam_mods") : new NbtCompound();
        rootNbt.put("skam_mods", skamNbt);

        // 设置新的容量值
        skamNbt.putInt("max_capacity", capacity);

        // *** 关键修复 ***
        // 重新应用修改以更新Lore和属性。
        // 新的方法只需要 ItemStack，它会自己从NBT读取所有需要的信息。
        CoreNbtApplicator.applyCoreModifications(stack);

        context.getSource().sendFeedback(() -> Text.literal("成功将手中物品的核心容量设置为 ").formatted(Formatting.GREEN)
                .append(Text.literal(String.valueOf(capacity)).formatted(Formatting.YELLOW)), true);

        return 1;
    }
}
