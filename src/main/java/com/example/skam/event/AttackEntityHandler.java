package com.example.skam.event;

import com.example.skam.item.ModItems;
import com.example.skam.util.ISoulLinkDataAccessor;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AttackEntityHandler implements AttackEntityCallback {
    private static final String SOUL_LINK_TEAM_NAME = "soul_link_green";

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (!world.isClient() && entity instanceof LivingEntity targetEntity) {
            if (player.getStackInHand(hand).getItem() == ModItems.SOUL_LINK_CHARM) {
                Scoreboard scoreboard = world.getServer().getScoreboard();
                Team soulLinkTeam = scoreboard.getTeam(SOUL_LINK_TEAM_NAME);
                if (soulLinkTeam == null) {
                    soulLinkTeam = scoreboard.addTeam(SOUL_LINK_TEAM_NAME);
                    soulLinkTeam.setColor(Formatting.GREEN); // 设置队伍颜色为绿色
                }
                scoreboard.addPlayerToTeam(targetEntity.getUuidAsString(), soulLinkTeam);
                ISoulLinkDataAccessor targetData = (ISoulLinkDataAccessor) targetEntity;
                NbtCompound nbt = targetData.getPersistentData();
                long expiryTime = world.getTime() + (30 * 20); // 30秒
                nbt.putUuid("soul_linker_uuid", player.getUuid());
                nbt.putLong("soul_link_expiry", expiryTime);
                targetEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 30 * 20, 0, false, false, true));

                player.sendMessage(Text.translatable("message.skam.linked_entity").formatted(Formatting.GREEN), true);
            }
        }
        return ActionResult.PASS;
    }
}
