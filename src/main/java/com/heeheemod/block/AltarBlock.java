package com.heeheemod.block;

import com.heeheemod.entity.ModEntities;
import com.heeheemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.Difficulty;

public class AltarBlock extends Block {
    public static final BooleanProperty HAS_EYE = BooleanProperty.create("has_eye");

    public AltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HAS_EYE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_EYE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (state.getValue(HAS_EYE) || !stack.is(ModItems.OJO_AYUWOKI.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Necesita espacio libre arriba para que el mob no aparezca dentro de un bloque
        if (!level.getBlockState(pos.above()).isAir()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Necesitas espacio libre encima del altar"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Nada ocurre... el ambiente es demasiado tranquilo"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }



        if (level instanceof ServerLevel serverLevel) {
            // Ojo incrustado para siempre: el altar queda usado
            level.setBlock(pos, state.setValue(HAS_EYE, true), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            // Rayo solo visual (no hace daño ni prende fuego)
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                bolt.setVisualOnly(true);
                serverLevel.addFreshEntity(bolt);
            }

            // Invocar al ayuwoki
            Entity entity = ModEntities.AYUWOKI.get().create(serverLevel);
            if (entity instanceof Mob mob) {
                mob.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                        level.random.nextFloat() * 360F, 0F);
                mob.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(pos),
                        MobSpawnType.MOB_SUMMONED, null);
                mob.setPersistenceRequired(); // que no desaparezca por despawn
                serverLevel.addFreshEntity(mob);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}