package foundationgames.blasttravel.entity;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.config.BTConfig;
import foundationgames.blasttravel.entity.cannon.CannonBehavior;
import foundationgames.blasttravel.entity.cannon.ConcretePowderCannonBehavior;
import foundationgames.blasttravel.entity.cannon.EntityCannonBehavior;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CannonEntity extends Entity {
    public static final Component UI_TITLE = Component.translatable("container.blasttravel.cannon");
    public static final Component NO_GUNPOWDER_DIALOG = Component.translatable("dialog.blasttravel.no_gunpowder").withStyle(ChatFormatting.RED);
    public static final Component FULL_CANNON_DIALOG = Component.translatable("dialog.blasttravel.full_cannon").withStyle(ChatFormatting.RED);

    public static final CannonBehavior NONE = new CannonBehavior(Items.AIR, stack -> false).register();
    public static final CannonBehavior GOLDEN = new CannonBehavior(Items.GOLD_BLOCK, BlastTravel.id("textures/entity/cannon/golden.png")).register();
    public static final CannonBehavior MOSSY = new CannonBehavior(Items.MOSS_BLOCK, BlastTravel.id("textures/entity/cannon/mossy.png")).register();
    public static final CannonBehavior LAZULI = new CannonBehavior(Items.LAPIS_BLOCK, BlastTravel.id("textures/entity/cannon/lazuli.png")).register();
    public static final CannonBehavior AMETHYST = new CannonBehavior(Items.AMETHYST_BLOCK, BlastTravel.id("textures/entity/cannon/amethyst.png")).register();
    public static final CannonBehavior TNT = new EntityCannonBehavior(Items.TNT, BlastTravel.id("textures/entity/cannon/tnt.png"), EntityCannonBehavior::tntFactory).register();
    public static final CannonBehavior ANVIL = new EntityCannonBehavior(Items.ANVIL, stack -> stack.is(Items.ANVIL) || stack.is(Items.CHIPPED_ANVIL) || stack.is(Items.DAMAGED_ANVIL), BlastTravel.id("textures/entity/cannon/anvil.png"), EntityCannonBehavior::fallingBlockFactory).register();
    public static final CannonBehavior POWDER = new ConcretePowderCannonBehavior().register();

    public static final EntityDataAccessor<Integer> BEHAVIOR = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> CHAINED = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> BEHAVIOR_STACK = SynchedEntityData.defineId(CannonEntity.class, EntityDataSerializers.ITEM_STACK);

    public static final int MAX_ANIMATION = 12;
    private static final double TINY_MOTION_SQR = 1.0E-7;

    private boolean chained;
    private boolean firing;
    private boolean powered;
    private boolean alwaysModifiable;
    private boolean droppedOnBreak;
    private int animation = 0;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final Container inventory = new Container() {
        @Override public int getContainerSize() { return items.size(); }
        @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }
        @Override public ItemStack removeItem(int slot, int amount) { var stack = ContainerHelper.removeItem(items, slot, amount); updateStateFromInventory(); return stack; }
        @Override public ItemStack removeItemNoUpdate(int slot) { var stack = ContainerHelper.takeItem(items, slot); updateStateFromInventory(); return stack; }
        @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); updateStateFromInventory(); }
        @Override public void setChanged() { updateStateFromInventory(); }
        @Override public boolean stillValid(Player player) { return !CannonEntity.this.isRemoved() && player.distanceToSqr(CannonEntity.this) < 64.0; }
        @Override public void clearContent() { items.clear(); updateStateFromInventory(); }
    };

    public CannonEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    public CannonEntity(Level world) {
        this(BlastTravel.CANNON.get(), world);
    }

    @Override
    public void tick() {
        if (this.firing && !this.isVehicle()) {
            this.firing = false;
        }

        super.tick();

        if (!this.chained && this.getFirstPassenger() instanceof Player player) {
            // Aim from the player's actual view rotation. Using head rotation here can
            // lag/jump while riding, especially in third person.
            this.setYRot(player.getYRot());
            this.setXRot(player.getXRot());
        }

        if (this.entityData.get(CHAINED) != this.chained) {
            if (!level().isClientSide) {
                this.entityData.set(CHAINED, this.chained);
            } else {
                setChained(this.entityData.get(CHAINED));
            }
        }

        if (this.animation > 0) {
            this.animation--;
        }

        if (this.level().isClientSide) {
            if (this.hasFuse()) {
                var pos = this.position().add(0, 0.75, 0).add(viewVector(this.getXRot() - 90, this.getYRot()).scale(0.75));
                this.level().addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        } else {
            boolean hasPower = this.level().getBestNeighborSignal(this.blockPosition()) > 0 || this.level().getBestNeighborSignal(this.blockPosition().below()) > 0;
            if (hasPower != this.powered) {
                if (hasPower) this.fireServer();
                this.powered = hasPower;
            }

            if (this.chained) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.movementTick();
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
        }
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        if (this.level().isClientSide && passenger instanceof Player player && player.isLocalPlayer()) {
            if (chained) {
                player.setYRot(this.getYRot());
                player.setXRot(this.getXRot());
            } else {
                player.setXRot(Math.min(18, player.getXRot()));
            }
        }
    }

    public boolean canPlayerModify(Player player) {
        return this.alwaysModifiable || player.mayBuild();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player == this.getFirstPassenger()) {
            return super.interact(player, hand);
        }

        if (player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                if (this.canPlayerModify(player)) {
                    player.openMenu(new MenuProvider() {
                        @Override public Component getDisplayName() { return UI_TITLE; }
                        @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player user) { return new CannonScreenHandler(id, inv, CannonEntity.this.inventory); }
                    });
                } else {
                    level().playSound(null, blockPosition(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.5F, 1.5F);
                }
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.isVehicle() && !this.getBehavior().occupiesCannon(this.inventory.getItem(2))) {
            if (!level().isClientSide) {
                player.setYRot(this.getYRot());
                player.setXRot(this.getXRot());
                player.startRiding(this);
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(FULL_CANNON_DIALOG, true);
        }

        return super.interact(player, hand);
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        if (attacker instanceof Player player && player != this.getFirstPassenger()) {
            if (player.mayBuild() && (player.isCreative() || player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PickaxeItem)) {
                if (!this.level().isClientSide) {
                    this.breakCannon(player);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1, 0.8F);
                this.level().levelEvent(2001, this.blockPosition(), net.minecraft.world.level.block.Block.getId(Blocks.ANVIL.defaultBlockState()));
                return true;
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.STONE_HIT, SoundSource.BLOCKS, 1, 0.5F);
        return true;
    }

    private void breakCannon(Player player) {
        if (this.droppedOnBreak || this.isRemoved()) {
            return;
        }
        this.droppedOnBreak = true;

        if (this.isVehicle()) {
            this.ejectPassengers();
        }

        SimpleContainer temp = new SimpleContainer(this.inventory.getContainerSize());
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                temp.setItem(i, stack.copy());
                this.items.set(i, ItemStack.EMPTY);
            }
        }
        this.updateStateFromInventory();

        Containers.dropContents(this.level(), this.blockPosition(), temp);
        if (!player.isCreative()) {
            Containers.dropItemStack(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(BlastTravel.CANNON_ITEM.get()));
        }

        this.discard();
    }

    private void movementTick() {
        var vel = this.getDeltaMovement();
        var damped = new Vec3(vel.x * 0.9, this.onGround() ? 0 : Math.max(vel.y - 0.07, -0.7), vel.z * 0.9);
        this.setDeltaMovement(clampTinyMotion(damped));
        this.hasImpulse = true;
    }

    private static Vec3 clampTinyMotion(Vec3 velocity) {
        return velocity.lengthSqr() < TINY_MOTION_SQR ? Vec3.ZERO : velocity;
    }

    public ItemStack getBehaviorStack() {
        if (!this.level().isClientSide) {
            return this.inventory.getItem(2);
        }
        return this.entityData.get(BEHAVIOR_STACK);
    }

    public void handleInput(boolean firing) {
        if (this.level().isClientSide) {
            if (firing && !this.firing) {
                BTNetworking.c2sRequestFire(this);
            }
            this.firing = firing;
        }
    }

    public void applyClientAim(float yaw, float pitch, @Nullable Player requestedBy) {
        if (this.chained) {
            return;
        }
        if (requestedBy != null && requestedBy.distanceToSqr(this) > 25.0) {
            return;
        }

        this.setYRot(yaw);
        this.setXRot(Mth.clamp(pitch, -90.0F, 18.0F));
    }

    public void fireServer() {
        this.fireServer(null);
    }

    public void fireServer(@Nullable Player requestedBy) {
        if (this.level() instanceof ServerLevel world) {
            var gunpowder = this.inventory.getItem(0);
            if (gunpowder.is(Items.GUNPOWDER) && gunpowder.getCount() > 0) {
                Player firedPlayer = null;
                var behaviorStack = this.getBehaviorStack();
                var vel = getDeltaMovement().add(this.getLaunchDirection().scale(Math.sqrt(gunpowder.getCount()) * BTConfig.launchVelocityFactor()));
                var launchPos = this.getLaunchPosition();

                this.getBehavior().onFired(this, behaviorStack, vel);

                Player playerToLaunch = null;
                if (this.getFirstPassenger() instanceof Player player) {
                    playerToLaunch = player;
                } else if (requestedBy != null && requestedBy.distanceToSqr(this) < 16.0) {
                    // Some modern client/server vehicle paths can remove the rider before the fire
                    // packet reaches the server. The C2S packet can only be sent while the local
                    // player is riding the cannon, so keep that player as a safe fallback target.
                    playerToLaunch = requestedBy;
                }

                if (playerToLaunch != null) {
                    this.launchPlayer(playerToLaunch, vel, launchPos);
                    firedPlayer = playerToLaunch;
                }

                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1);
                for (var to : world.players()) {
                    BTNetworking.s2cFireCannon(to, this, firedPlayer, vel, launchPos);
                }

                this.updateStateFromInventory();
            } else {
                if (this.getFirstPassenger() instanceof Player player) {
                    player.stopRiding();
                    player.displayClientMessage(NO_GUNPOWDER_DIALOG, true);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1, 0.8F);
            }
        }
    }

    private void launchPlayer(Player player, Vec3 velocity, Vec3 launchPos) {
        ServerPlayer serverPlayer = player instanceof ServerPlayer foundServerPlayer ? foundServerPlayer : null;
        if (player.getVehicle() == this) {
            player.stopRiding();
            if (serverPlayer != null) {
                serverPlayer.connection.send(new ClientboundSetPassengersPacket(this));
            }
        }

        player.setPos(launchPos.x, launchPos.y, launchPos.z);
        player.setDeltaMovement(velocity);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        ((PlayerEntityDuck) player).blasttravel$setCannonFlightVelocity(velocity);
        ((PlayerEntityDuck) player).blasttravel$setCannonFlight(true);

        if (serverPlayer != null) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    public Vec3 getLaunchDirection() {
        var direction = viewVector(this.getXRot(), this.getYRot());
        if (direction.lengthSqr() < 1.0E-6) {
            direction = this.getLookAngle();
        }
        return direction.lengthSqr() < 1.0E-6 ? Vec3.ZERO : direction.normalize();
    }

    public Vec3 getLaunchPosition() {
        var direction = this.getLaunchDirection();
        return this.position().add(0, 0.9, 0).add(direction.scale(1.25));
    }

    public void fireClient() {
        if (!level().isClientSide) return;

        this.animate();
        final int ringParticles = 18;
        for (int i = 0; i < ringParticles; i++) {
            double angle = (2D / ringParticles) * Math.PI * i;
            var arc = new Vec3(Math.sin(angle), Math.cos(angle), 0)
                    .xRot(-this.getXRot() * Mth.DEG_TO_RAD)
                    .yRot(-this.getYRot() * Mth.DEG_TO_RAD);

            var pos = this.position().add(0, 0.75, 0).add(viewVector(this.getXRot() - 4, this.getYRot()).scale(1.69F)).add(arc.scale(0.15F));
            var vel = arc.scale(0.14);
            this.level().addParticle(BlastTravel.CANNON_BLAST.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        }
    }

    public void animate() { this.animation = MAX_ANIMATION; }

    protected int getBehaviorId() { return this.entityData.get(BEHAVIOR); }
    protected void setBehaviorId(int id) { this.entityData.set(BEHAVIOR, id); }

    public boolean hasFuse() { return this.isVehicle() || this.getBehavior().occupiesCannon(this.inventory.getItem(2)); }
    public boolean hasChains() { return this.chained; }
    public float getAnimation(float tickDelta) { return Math.max(0, this.animation - tickDelta) / MAX_ANIMATION; }
    public int getAnimationTick() { return this.animation; }

    private void setChained(boolean chained) {
        if (chained != this.chained) {
            this.level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_CHAIN, SoundSource.BLOCKS, 1, 1.2F);
            if (chained) {
                this.setDeltaMovement(Vec3.ZERO);
            }
        }
        this.chained = chained;
    }

    public @Nullable Player getClientPlayer() {
        return this.getFirstPassenger() instanceof Player player ? player : null;
    }

    public CannonBehavior getBehavior() { return CannonBehavior.byId(getBehaviorId()); }

    protected void updateStateFromInventory() {
        if (!this.level().isClientSide) {
            for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
                var stack = this.inventory.getItem(slot);
                if (slot == 1) {
                    setChained(stack.is(Items.CHAIN));
                } else if (slot == 2) {
                    this.setBehaviorId(CannonBehavior.idForStack(stack));
                    this.entityData.set(BEHAVIOR_STACK, stack.copy());
                }
            }
        }
    }

    @Override
    public ItemStack getPickResult() { return new ItemStack(BlastTravel.CANNON_ITEM.get()); }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() { return !this.isRemoved(); }

    @Override
    public boolean isPushable() { return !this.chained; }

    @Override
    public void push(Entity entity) {
        if (!this.chained) {
            super.push(entity);
        }
    }

    @Override
    public void push(double x, double y, double z) {
        if (!this.chained) {
            super.push(x, y, z);
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (this.hasPassenger(passenger)) {
            callback.accept(passenger, this.getX(), this.getY() + 0.75, this.getZ());
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(BEHAVIOR, 0);
        this.entityData.define(CHAINED, false);
        this.entityData.define(BEHAVIOR_STACK, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag.getCompound("Items"), this.items);
        this.powered = tag.getBoolean("powered");
        this.alwaysModifiable = tag.getBoolean("alwaysModifiable");
        this.updateStateFromInventory();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        var inv = new CompoundTag();
        ContainerHelper.saveAllItems(inv, this.items);
        tag.put("Items", inv);
        tag.putBoolean("powered", this.powered);
        tag.putBoolean("alwaysModifiable", this.alwaysModifiable);
    }


    private static Vec3 viewVector(float pitch, float yaw) {
        float pitchRad = pitch * Mth.DEG_TO_RAD;
        float yawRad = -yaw * Mth.DEG_TO_RAD;
        float h = Mth.cos(yawRad);
        float i = Mth.sin(yawRad);
        float j = Mth.cos(pitchRad);
        float k = Mth.sin(pitchRad);
        return new Vec3(i * j, -k, h * j);
    }
}
