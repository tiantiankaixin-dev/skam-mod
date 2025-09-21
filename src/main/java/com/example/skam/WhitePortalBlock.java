
package com.example.skam;

import net.minecraft.world.BlockView;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WhitePortalBlock extends Block implements Waterloggable { // 实现 Waterloggable 接口
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final ConcurrentHashMap<UUID, Long> PLAYERS_IN_PORTAL = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> PORTAL_COOLDOWN = new ConcurrentHashMap<>();
    private static final int PORTAL_DELAY_TICKS = 80; // 4 seconds (20 ticks/sec * 4s)
    private static final int COOLDOWN_TICKS = 200; // 10 seconds

    public WhitePortalBlock(Settings settings) {
        super(settings);
        // 设置方块的默认状态，默认是不含水的
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false));
    }




    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube(); // 返回一个1x1x1的完整方块轮廓
    }
    // 这个方法定义了物理碰撞箱，我们保持为空，以便实体穿过
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty(); // 返回一个空形状，没有物理碰撞
    }



    // --- 6. 新增3个方法，用于实现 Waterloggable 接口 ---
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // 将 "waterlogged" 属性添加到方块的状态管理器中
        builder.add(WATERLOGGED);
    }
    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // 决定方块被放置时的状态（如果放在水里，就自动变成含水状态）
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean isWater = fluidState.getFluid() == Fluids.WATER;
        return this.getDefaultState().with(WATERLOGGED, isWater);
    }
    @Override
    public FluidState getFluidState(BlockState state) {
        // 告诉游戏当前方块内的流体状态是什么
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            if (!PORTAL_COOLDOWN.containsKey(player.getUuid())) {
                if (PLAYERS_IN_PORTAL.putIfAbsent(player.getUuid(), world.getTime()) == null) {
                    SkamMod.LOGGER.info("Player {} entered the portal.", player.getName().getString());
                }
            }
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    public static void portalTickHandler(MinecraftServer server) {
        long currentTime = server.getOverworld().getTime();

        PORTAL_COOLDOWN.entrySet().removeIf(entry -> currentTime > entry.getValue() + COOLDOWN_TICKS);

        for (UUID playerId : PLAYERS_IN_PORTAL.keySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player == null) {
                PLAYERS_IN_PORTAL.remove(playerId);
                continue;
            }

            if (player.getBlockStateAtPos().isOf(SkamMod.WHITE_PORTAL_BLOCK)) {
                long timeEntered = PLAYERS_IN_PORTAL.get(playerId);
                if (currentTime >= timeEntered + PORTAL_DELAY_TICKS) {
                    SkamMod.LOGGER.info("Teleporting player {}.", player.getName().getString());
                    teleportPlayer(player);
                    PLAYERS_IN_PORTAL.remove(playerId);
                    PORTAL_COOLDOWN.put(playerId, currentTime);
                }
            } else {
                SkamMod.LOGGER.info("Player {} left the portal.", player.getName().getString());
                PLAYERS_IN_PORTAL.remove(playerId);
            }
        }
    }

    private static void teleportPlayer(ServerPlayerEntity player) {
        ServerWorld currentWorld = player.getServerWorld();
        ServerWorld targetWorld;

        if (currentWorld.getRegistryKey() == World.OVERWORLD) {
            targetWorld = player.getServer().getWorld(SkamMod.BROKEN_CONTINENT_WORLD_KEY);
        } else if (currentWorld.getRegistryKey() == SkamMod.BROKEN_CONTINENT_WORLD_KEY) {
            targetWorld = player.getServer().getOverworld();
        } else {
            return;
        }

        if (targetWorld == null) {
            SkamMod.LOGGER.error("Target world is NULL! Cannot teleport player {}.", player.getName().getString());
            return;
        }

        BlockPos portalPos = player.getBlockPos();
        BlockPos targetPos = findOrCreatePortal(targetWorld, portalPos);

        if (targetPos == null) {
            SkamMod.LOGGER.error("Could not find or create a safe portal location for player {}.", player.getName().getString());
            return;
        }

        Vec3d teleportPos = Vec3d.ofCenter(targetPos);
        TeleportTarget teleportTarget = new TeleportTarget(teleportPos, player.getVelocity(), player.getYaw(), player.getPitch());

        player.teleport(targetWorld, teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z, teleportTarget.yaw, teleportTarget.pitch);
        targetWorld.playSound(null, targetPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static BlockPos findOrCreatePortal(ServerWorld world, BlockPos originalPos) {
        BlockPos.Mutable searchPos = new BlockPos.Mutable();
        double coordinateScale = world.getDimension().coordinateScale(); // 更通用的坐标缩放获取方式

        int searchX = (int) (originalPos.getX() * coordinateScale);
        int searchZ = (int) (originalPos.getZ() * coordinateScale);

        // First, search for an existing portal
        for (int y = world.getBottomY(); y < world.getTopY(); y++) {
            for (int x = searchX - 16; x <= searchX + 16; x++) {
                for (int z = searchZ - 16; z <= searchZ + 16; z++) {
                    searchPos.set(x, y, z);
                    if (world.getBlockState(searchPos).isOf(SkamMod.WHITE_PORTAL_BLOCK)) {
                        SkamMod.LOGGER.info("Found existing portal at {}", searchPos.toImmutable());
                        return searchPos.toImmutable();
                    }
                }
            }
        }

        // If not found, create a new one at a safe location
        SkamMod.LOGGER.info("No existing portal found. Creating a new one...");
        BlockPos safePos = findSafeLocation(world, new BlockPos(searchX, 128, searchZ));
        if (safePos != null) {
            world.setBlockState(safePos, SkamMod.WHITE_PORTAL_BLOCK.getDefaultState());
            ensureSafePlatform(world, safePos);
            SkamMod.LOGGER.info("Created new portal at {}", safePos);
        }
        return safePos;
    }

    private static BlockPos findSafeLocation(ServerWorld world, BlockPos preferredPos) {
        for (int r = 0; r <= 32; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (Math.abs(x) == r || Math.abs(z) == r) {
                        BlockPos testPos = preferredPos.add(x, 0, z);

                        int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, testPos.getX(), testPos.getZ());

                        if (topY > world.getBottomY()) {
                            BlockPos landingPos = new BlockPos(testPos.getX(), topY, testPos.getZ());
                            if (isLocationSafe(world, landingPos)) {
                                return landingPos;
                            }
                        }
                    }
                }
            }
        }
        SkamMod.LOGGER.warn("Could not find a safe location after extensive search. Returning null.");
        return null;
    }

    // --- 主要修改 ---
    // 这个方法被重写，以确保传送门不会生成在水、熔岩或不安全的地方
    private static boolean isLocationSafe(WorldView world, BlockPos pos) {
        // 1. 检查脚下是否有坚实的地面
        BlockState groundState = world.getBlockState(pos.down());
        if (!groundState.isOpaqueFullCube(world, pos.down())) {
            return false; // 脚下不是实体方块
        }

        // 2. 检查玩家身体和头部空间是否为空
        if (!world.getBlockState(pos).isReplaceable() || !world.getBlockState(pos.up()).isReplaceable()) {
            return false; // 玩家站立的空间被阻挡
        }

        // 3. 检查传送门方块周围3x3x3区域，确保没有水或熔岩等危险
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    mutable.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState state = world.getBlockState(mutable);

                    // 如果方块是熔岩，或者方块内有任何流体（如水），则此位置不安全
                    if (state.isOf(Blocks.LAVA) || !world.getFluidState(mutable).isEmpty()) {
                        return false; // 发现危险方块
                    }
                }
            }
        }

        // 所有检查都通过，位置是安全的
        return true;
    }
    // --- 修改结束 ---

    private static void ensureSafePlatform(ServerWorld world, BlockPos pos) {
        BlockPos below = pos.down();
        if (!world.getBlockState(below).isOpaqueFullCube(world, below)) {
            SkamMod.LOGGER.info("Creating a safety platform at {}", below);
            world.setBlockState(below, Blocks.OBSIDIAN.getDefaultState());
        }
    }

    public static void removePlayerFromPortalLogic(UUID playerId) {
        PLAYERS_IN_PORTAL.remove(playerId);
        PORTAL_COOLDOWN.remove(playerId);
    }
}
