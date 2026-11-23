package com.skyrotor.entity;

import com.skyrotor.network.ModNetwork;
import com.skyrotor.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class HelicopterEntity extends Entity {

    private static final EntityDataAccessor<Boolean> ENGINE_ON =
            SynchedEntityData.defineId(HelicopterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> POWER =
            SynchedEntityData.defineId(HelicopterEntity.class, EntityDataSerializers.FLOAT);

    // Настройки полёта
    private static final float SPIN_UP = 0.02F;      // за ~2.5 сек раскрутка винта
    private static final float SPIN_DOWN = 0.01F;
    private static final double MAX_SPEED = 0.55D;
    private static final double ACCEL = 0.04D;
    private static final double CLIMB_SPEED = 0.25D;
    private static final double TAKEOFF_SPEED = 0.18D;
    private static final int TAKEOFF_TICKS = 50;
    private static final float TURN_SPEED = 3.0F;

    // Сервер
    private int input;
    private int takeoffTicks;

    // Клиент: плавное движение и анимация винта
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private double lerpYaw;
    public float rotorAngle;
    public float rotorAnglePrev;

    public HelicopterEntity(EntityType<? extends HelicopterEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ENGINE_ON, false);
        this.entityData.define(POWER, 0.0F);
    }

    public boolean isEngineOn() {
        return this.entityData.get(ENGINE_ON);
    }

    public float getPower() {
        return this.entityData.get(POWER);
    }

    public void setInput(int flags) {
        this.input = flags;
    }

    private boolean has(int flag) {
        return (this.input & flag) != 0;
    }

    public void toggleEngine(Player player) {
        if (this.level().isClientSide) {
            return;
        }
        boolean on = !isEngineOn();
        this.entityData.set(ENGINE_ON, on);
        if (on) {
            this.takeoffTicks = TAKEOFF_TICKS;
            this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
            player.displayClientMessage(Component.translatable("message.skyrotor.started"), true);
        } else {
            this.takeoffTicks = 0;
            this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
            player.displayClientMessage(Component.translatable("message.skyrotor.stopped"), true);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            clientTick();
        } else {
            serverTick();
        }
    }

    private void serverTick() {
        boolean hasPilot = !this.getPassengers().isEmpty();
        if (!hasPilot && isEngineOn()) {
            this.entityData.set(ENGINE_ON, false);
            this.takeoffTicks = 0;
        }
        if (!hasPilot) {
            this.input = 0;
        }

        // Раскрутка винта
        float power = getPower();
        if (isEngineOn()) {
            power = Math.min(1.0F, power + SPIN_UP);
        } else {
            power = Math.max(0.0F, power - SPIN_DOWN);
        }
        if (power != getPower()) {
            this.entityData.set(POWER, power);
        }

        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x;
        double my = motion.y;
        double mz = motion.z;

        boolean flying = isEngineOn() && power >= 0.6F;

        if (flying) {
            // Поворот
            float turn = 0;
            if (has(ModNetwork.InputPacket.LEFT)) turn -= TURN_SPEED;
            if (has(ModNetwork.InputPacket.RIGHT)) turn += TURN_SPEED;
            this.setYRot(this.getYRot() + turn);

            // Вертикаль
            double targetY = 0.0D;
            if (this.takeoffTicks > 0) {
                targetY = TAKEOFF_SPEED;
                this.takeoffTicks--;
            }
            if (has(ModNetwork.InputPacket.UP)) {
                targetY = CLIMB_SPEED;
                this.takeoffTicks = 0;
            } else if (has(ModNetwork.InputPacket.DOWN)) {
                targetY = -CLIMB_SPEED;
                this.takeoffTicks = 0;
            }
            my += (targetY - my) * 0.15D;

            // Горизонталь
            double fwd = 0;
            if (has(ModNetwork.InputPacket.FORWARD)) fwd += 1;
            if (has(ModNetwork.InputPacket.BACK)) fwd -= 0.5;

            float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
            mx += -Mth.sin(yawRad) * ACCEL * fwd;
            mz += Mth.cos(yawRad) * ACCEL * fwd;

            double hSpeed = Math.sqrt(mx * mx + mz * mz);
            if (hSpeed > MAX_SPEED) {
                mx *= MAX_SPEED / hSpeed;
                mz *= MAX_SPEED / hSpeed;
            }
            // Сопротивление воздуха
            mx *= 0.92D;
            mz *= 0.92D;
        } else {
            // Двигатель выключен / раскручивается: падаем под действием гравитации
            my -= 0.04D;
            my *= 0.98D;
            if (this.onGround()) {
                mx *= 0.5D;
                mz *= 0.5D;
            } else {
                mx *= 0.98D;
                mz *= 0.98D;
            }
        }

        this.setDeltaMovement(mx, my, mz);
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Упёрлись в потолок/землю: гасим вертикальную скорость
        if (this.verticalCollision) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1));
            this.takeoffTicks = 0;
        }

        this.setXRot(Mth.clamp(this.getXRot() + ((float) (-(mx * mx + mz * mz) * 20.0D) - this.getXRot()) * 0.2F, -12.0F, 12.0F));
    }

    private void clientTick() {
        this.rotorAnglePrev = this.rotorAngle;
        this.rotorAngle += getPower() * 70.0F;

        if (this.lerpSteps > 0) {
            double nx = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double ny = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double nz = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            this.setYRot(this.getYRot() + (float) Mth.wrapDegrees(this.lerpYaw - this.getYRot()) / this.lerpSteps);
            this.lerpSteps--;
            this.setPos(nx, ny, nz);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYaw = yRot;
        this.setXRot(xRot);
        this.lerpSteps = Math.max(2, steps);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            if (player.startRiding(this)) {
                player.displayClientMessage(Component.translatable("message.skyrotor.hint"), false);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || this.level().isClientSide || this.isRemoved()) {
            return false;
        }
        if (source.getEntity() instanceof Player player && !this.hasPassenger(player)) {
            if (!player.getAbilities().instabuild) {
                this.spawnAtLocation(new ItemStack(ModItems.HELICOPTER.get()));
            }
            this.discard();
            return true;
        }
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.55D;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (passenger instanceof Player) {
            // Пилот смотрит вперёд-вдоль вертолёта, но голову вращать может свободно
            passenger.setYBodyRot(this.getYRot());
        }
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.HELICOPTER.get());
    }
}
