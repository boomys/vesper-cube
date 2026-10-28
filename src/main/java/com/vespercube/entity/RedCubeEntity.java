package com.vespercube.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Vesper, the red cube companion: it follows the nearest player, builds a small
 * house every minute or so, and starts tearing blocks apart when it gets hurt.
 */
public class RedCubeEntity extends PathAwareEntity {
    private static final int BUILD_COOLDOWN_TICKS = 1200;
    private static final double FOLLOW_DISTANCE = 4.0;
    private static final double SEARCH_RADIUS = 32.0;

    private final List<HouseBlueprint.Piece> blueprint = new ArrayList<>();
    private int buildIndex;
    private int buildTicker;
    private int buildCooldown = 200;
    private int angryTicker;
    private float lastHealth = -1.0F;
    private boolean buildsEnabled = true;

    public RedCubeEntity(EntityType<? extends RedCubeEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 15;
    }

    public static DefaultAttributeContainer.Builder createRedCubeAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 60.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            return;
        }

        this.trackHealth();
        PlayerEntity owner = this.nearestPlayer();

        if (this.angryTicker > 0) {
            this.angryTicker--;
            if (this.age % 6 == 0) {
                this.smashBlocks();
            }
            return;
        }

        if (!this.blueprint.isEmpty()) {
            this.advanceBuild();
            return;
        }

        if (owner != null && this.squaredDistanceTo(owner) > FOLLOW_DISTANCE * FOLLOW_DISTANCE) {
            this.getNavigation().startMovingTo(owner, 1.15);
        }

        if (this.buildCooldown > 0) {
            this.buildCooldown--;
            return;
        }

        if (this.buildsEnabled && owner != null && this.age % 20 == 0) {
            this.startBuild(owner);
        }
    }

    private void trackHealth() {
        if (this.lastHealth < 0.0F) {
            this.lastHealth = this.getHealth();
            return;
        }
        if (this.getHealth() < this.lastHealth) {
            // Somebody hit Vesper: stop building and start demolishing.
            this.angryTicker = 240;
            this.blueprint.clear();
            this.buildIndex = 0;
        }
        this.lastHealth = this.getHealth();
    }

    private PlayerEntity nearestPlayer() {
        List<PlayerEntity> players = this.getWorld().getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(SEARCH_RADIUS),
                player -> player.isAlive() && !player.isSpectator());

        PlayerEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (PlayerEntity player : players) {
            double distance = this.squaredDistanceTo(player);
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    private void startBuild(PlayerEntity owner) {
        BlockPos base = owner.getBlockPos();
        int x = base.getX() + (this.random.nextBoolean() ? 3 : -8);
        int z = base.getZ() + (this.random.nextBoolean() ? 3 : -8);
        int y = this.getWorld().getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x + 2, z + 2);

        this.blueprint.addAll(HouseBlueprint.smallHouse(new BlockPos(x, y, z)));
        this.buildIndex = 0;
        this.buildTicker = 0;
        this.getNavigation().stop();
        owner.sendMessage(Text.translatable("entity.vespercube.red_cube.build_start"), false);
    }

    private void advanceBuild() {
        if (this.buildTicker++ % 2 != 0) {
            return;
        }

        while (this.buildIndex < this.blueprint.size()) {
            HouseBlueprint.Piece piece = this.blueprint.get(this.buildIndex++);
            BlockPos pos = piece.pos();
            if (!this.getWorld().getBlockState(pos).isReplaceable()) {
                continue;
            }
            this.getWorld().setBlockState(pos, piece.state(), 3);
            this.getWorld().playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE,
                    SoundCategory.NEUTRAL, 0.7F, 1.1F);
            return;
        }

        this.blueprint.clear();
        this.buildIndex = 0;
        this.buildCooldown = BUILD_COOLDOWN_TICKS;

        PlayerEntity owner = this.nearestPlayer();
        if (owner != null) {
            owner.sendMessage(Text.translatable("entity.vespercube.red_cube.build_done"), false);
        }
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.6, 0.6, 0.6, 0.02);
        }
    }

    private void smashBlocks() {
        World world = this.getWorld();
        BlockPos center = this.getBlockPos();

        for (int attempt = 0; attempt < 6; attempt++) {
            BlockPos pos = center.add(this.random.nextInt(7) - 3,
                    this.random.nextInt(3), this.random.nextInt(7) - 3);
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || state.getHardness(world, pos) < 0.0F) {
                continue;
            }

            world.breakBlock(pos, false, this, 3);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EAT.value(),
                    SoundCategory.HOSTILE, 1.0F, 0.6F);
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        6, 0.3, 0.3, 0.3, 0.05);
            }
            return;
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        this.buildsEnabled = !this.buildsEnabled;
        player.sendMessage(Text.translatable(this.buildsEnabled
                ? "entity.vespercube.red_cube.builds_on"
                : "entity.vespercube.red_cube.builds_off"), false);
        return ActionResult.SUCCESS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }
}
