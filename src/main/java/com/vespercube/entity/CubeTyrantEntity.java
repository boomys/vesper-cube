package com.vespercube.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The arena boss: a much larger, corrupted cube. It shows a boss bar, slams the
 * ground for area damage, and speeds up once it drops below half health.
 */
public class CubeTyrantEntity extends HostileEntity {
    private static final TrackedData<Boolean> TALKING =
            DataTracker.registerData(CubeTyrantEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private final ServerBossBar bossBar = new ServerBossBar(
            Text.translatable("entity.vespercube.cube_tyrant"),
            BossBar.Color.RED,
            BossBar.Style.PROGRESS);

    private int slamCooldown = 120;
    private int talkTicks;
    private boolean enraged;

    public CubeTyrantEntity(EntityType<? extends CubeTyrantEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 500;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createCubeTyrantAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 320.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.ATTACK_DAMAGE, 12.0)
                .add(EntityAttributes.ARMOR, 8.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TALKING, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.25, true));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
        if (this.age % 20 == 0) {
            for (ServerPlayerEntity player : serverWorld.getPlayers(
                    player -> player.isAlive() && player.squaredDistanceTo(this) < 64.0 * 64.0)) {
                this.bossBar.addPlayer(player);
            }
        }

        if (this.talkTicks > 0 && --this.talkTicks == 0) {
            this.dataTracker.set(TALKING, false);
        }

        if (!this.enraged && this.getHealth() < this.getMaxHealth() * 0.5F) {
            this.enrage(serverWorld);
        }

        if (--this.slamCooldown <= 0) {
            this.slam(serverWorld);
            this.slamCooldown = this.enraged ? 70 : 130;
        }
    }

    private void enrage(ServerWorld world) {
        this.enraged = true;
        this.dataTracker.set(TALKING, true);
        this.talkTicks = 60;
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.4F, 0.7F);
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 1.5, this.getZ(), 60, 1.2, 1.2, 1.2, 0.05);
        for (ServerPlayerEntity player : world.getPlayers(player -> player.squaredDistanceTo(this) < 64.0 * 64.0)) {
            player.sendMessage(Text.translatable("entity.vespercube.cube_tyrant.enraged"), false);
        }
    }

    private void slam(ServerWorld world) {
        world.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.HOSTILE, 1.6F, 0.5F);
        world.spawnParticles(ParticleTypes.EXPLOSION,
                this.getX(), this.getY() + 0.5, this.getZ(), 12, 4.0, 0.4, 4.0, 0.0);

        double radius = this.enraged ? 7.0 : 5.0;
        for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class,
                this.getBoundingBox().expand(radius), PlayerEntity::isAlive)) {
            player.damage(world, world.getDamageSources().mobAttack(this), this.enraged ? 9.0F : 6.0F);
            Vec3d push = player.getPos().subtract(this.getPos()).normalize().multiply(1.2).add(0.0, 0.7, 0.0);
            player.addVelocity(push.x, push.y, push.z);
            player.velocityModified = true;
        }
    }

    public boolean isTalking() {
        return this.dataTracker.get(TALKING);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        this.bossBar.clearPlayers();

        if (!(this.getWorld() instanceof ServerWorld world)) {
            return;
        }

        this.dropStack(world, new ItemStack(Items.NETHERITE_INGOT));
        this.dropStack(world, new ItemStack(Items.EMERALD, 16));
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY() + 2.0, this.getZ(), 6, 2.0, 2.0, 2.0, 0.0);
        for (ServerPlayerEntity player : world.getPlayers(
                player -> player.squaredDistanceTo(this) < 80.0 * 80.0)) {
            player.sendMessage(Text.translatable("entity.vespercube.cube_tyrant.defeated"), false);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        this.bossBar.clearPlayers();
        super.remove(reason);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RAVAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.6F;
    }
}
