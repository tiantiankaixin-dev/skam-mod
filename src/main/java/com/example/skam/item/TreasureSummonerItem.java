package com.example.skam.item;

import com.example.skam.MobConfig;
import com.example.skam.SkamMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreasureSummonerItem extends Item {
    public static final String TEAM_NAME_PREFIX = "skam_treasure_";

    public TreasureSummonerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        PlayerEntity player = context.getPlayer();

        // 1. 从MobConfig获取可生成的怪物列表 (此部分逻辑不变)
        List<EntityType<? extends HostileEntity>> availableMobs = MobConfig.getValidMobTypes();

        if (availableMobs.isEmpty()) {
            if (player != null) {
                player.sendMessage(Text.literal("没有可用的宝藏怪物！请检查配置文件。").formatted(Formatting.RED), false);
            }
            if (player != null && player.isCreative()) {
                player.sendMessage(Text.literal("提示: 详细错误请查看客户端日志。").formatted(Formatting.YELLOW), false);
            }
            return ActionResult.FAIL;
        }

        BlockPos pos = context.getBlockPos().offset(context.getSide());

        // 2. 随机选择并创建一个基础怪物 (此部分逻辑不变)
        EntityType<?> selectedType = availableMobs.get(Random.create().nextInt(availableMobs.size()));

        HostileEntity mob = (HostileEntity) selectedType.create(serverWorld);
        if (mob == null) {
            if (player != null) {
                player.sendMessage(Text.literal("怪物创建失败！ID: " + selectedType.getUntranslatedName()).formatted(Formatting.RED), false);
            }
            return ActionResult.FAIL;
        }

        mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                Random.create().nextFloat() * 360.0F, 0.0F);
        mob.initialize(serverWorld, serverWorld.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);

        // ==================== 【核心修改】将代码3的逻辑移植到这里 ====================
        Random random = world.getRandom();
        double newHealth;
        Identifier lootTableId;
        Formatting glowColor;
        int tier;

        int chance = random.nextInt(100); // 生成 0-99 的随机数

        if (chance < 80) {
            // 80% 的概率: 等级1 (绿色)
            tier = 1;
            glowColor = Formatting.GREEN;
            lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_1");
            newHealth = 50.0 + random.nextInt(51); // 生命值范围: 50-100
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100000 * 20, 5, false, false));
        } else if (chance < 95) {
            // 15% 的概率: 等级2 (蓝色)
            tier = 2;
            glowColor = Formatting.BLUE;
            lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_2");
            newHealth = 150.0 + random.nextInt(76); // 生命值范围: 150-225
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100000 * 20, 10, false, false));
        } else {
            // 5% 的概率: 等级3 (红色)
            tier = 3;
            glowColor = Formatting.RED;
            lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_3");
            newHealth = 250.0 + random.nextInt(51); // 生命值范围: 250-300
            mob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1000000 * 20, 14, false, false));
        }

        // 应用生命值属性
        EntityAttributeInstance maxHealthAttr = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(newHealth);
            mob.heal((float) newHealth);
        }

        // 使用动态选择的战利品表和颜色来装备和设置怪物
        equipMobAndSetDrops(serverWorld, mob, lootTableId);
        mob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, false, false));
        setupTreasureTeam(serverWorld, mob, glowColor);
        // =========================================================================

        serverWorld.spawnEntity(mob);

        SkamMod.LOGGER.info(String.format("玩家 %s 使用物品召唤了一个等级 %d 的宝藏怪物 %s",
                player != null ? player.getName().getString() : "未知",
                tier,
                mob.getType().getUntranslatedName()
        ));

        world.playSound(null, pos, SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.BLOCKS, 1.0f, 1.0f);
        if (player != null && !player.getAbilities().creativeMode) {
            context.getStack().decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    // equipMobAndSetDrops 方法保持不变
    public static void equipMobAndSetDrops(ServerWorld serverWorld, HostileEntity mob, Identifier lootTableId) {
        LootTable lootTable = serverWorld.getServer().getLootManager().getLootTable(lootTableId);
        if (lootTable == LootTable.EMPTY) {
            SkamMod.LOGGER.error("无法找到或战利品表为空: " + lootTableId);
            return;
        }
        LootContextParameterSet.Builder parameterSetBuilder = new LootContextParameterSet.Builder(serverWorld)
                .add(LootContextParameters.THIS_ENTITY, mob)
                .add(LootContextParameters.ORIGIN, mob.getPos())
                .add(LootContextParameters.DAMAGE_SOURCE, serverWorld.getDamageSources().magic());
        LootContextParameterSet parameterSet = parameterSetBuilder.build(LootContextTypes.ENTITY);
        List<ItemStack> lootItems = lootTable.generateLoot(parameterSet);
        if (lootItems.isEmpty()) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setEquipmentDropChance(slot, 0.0F);
        }
        List<EquipmentSlot> equippedSlots = new ArrayList<>();
        for (ItemStack stack : lootItems) {
            EquipmentSlot preferredSlot = MobEntity.getPreferredEquipmentSlot(stack);
            if (mob.getEquippedStack(preferredSlot).isEmpty()) {
                mob.equipStack(preferredSlot, stack);
                equippedSlots.add(preferredSlot);
            }
        }
        if (!equippedSlots.isEmpty()) {
            Collections.shuffle(equippedSlots);
            EquipmentSlot chosenSlotToDrop = equippedSlots.get(0);
            mob.setEquipmentDropChance(chosenSlotToDrop, 1.0F);
        }
    }

    // setupTreasureTeam 方法保持不变
    public static void setupTreasureTeam(World world, Entity entity, Formatting color) {
        Scoreboard scoreboard = world.getScoreboard();
        String teamName = TEAM_NAME_PREFIX + color.getName();

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
        }

        team.setColor(color);
        team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);

        scoreboard.addPlayerToTeam(entity.getEntityName(), team);
    }
}

