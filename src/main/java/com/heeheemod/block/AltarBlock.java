package com.heeheemod.block;

import com.heeheemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

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

    // Click derecho CON un item en la mano: colocar el ojo
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (!state.getValue(HAS_EYE) && stack.is(ModItems.OJO_AYUWOKI.get())) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(HAS_EYE, true), Block.UPDATE_ALL);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                // Aquí puedes disparar lo que haga el altar al activarse (invocar al ayuwoki, etc.)
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Click derecho con la mano vacía: recuperar el ojo
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (state.getValue(HAS_EYE)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(HAS_EYE, false), Block.UPDATE_ALL);
                ItemStack eye = new ItemStack(ModItems.OJO_AYUWOKI.get());
                if (!player.getInventory().add(eye)) {
                    player.drop(eye, false);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    // Si rompen el altar con el ojo puesto, el ojo se suelta
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && state.getValue(HAS_EYE)) {
            Block.popResource(level, pos, new ItemStack(ModItems.OJO_AYUWOKI.get()));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}