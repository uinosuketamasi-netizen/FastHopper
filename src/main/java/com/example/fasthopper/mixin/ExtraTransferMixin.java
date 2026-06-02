package com.example.fasthopper.mixin;

import com.example.fasthopper.accessor.ExtraRightsAccessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import com.example.fasthopper.FastHopperConfig;
import com.example.fasthopper.FastHopperMode;

import net.minecraft.world.level.block.HopperBlock;

@Mixin(HopperBlockEntity.class)
public class ExtraTransferMixin {
    @Inject(
            method = "pushItemsTick",
            at = @At("TAIL")
    )
    private static void fasthopper$pushTick(
            Level level,
            BlockPos pos,
            BlockState state,
            HopperBlockEntity entity,
            CallbackInfo ci
    ) {
        if (
                FastHopperConfig.CURRENT_MODE
                        == FastHopperMode.EXTRA
        )
        {
            boolean enabled =
                    state.getValue(HopperBlock.ENABLED);

//            ExampleMod.LOGGER.info(
//                    "🔥 ENABLED = {}",
//                    enabled
//            );

            if (!enabled)
            {
                return;
            }

//                ExampleMod.LOGGER.info(
//                        "🔥 EXTRA TEST {}",
//                        pos
//                );


//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA EXECUTE"
//            );


//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA EJECT START"
//            );

            if (entity.isEmpty())
            {
                return;
            }

//            ExampleMod.LOGGER.info(
//                    "🔥 GAME TIME = {}",
//                    level.getGameTime()
//            );

            ExtraRightsAccessor rights =
                    (ExtraRightsAccessor)entity;

            rights.fasthopper$setInsideExtraTransfer(
                    true
            );

//            ExampleMod.LOGGER.info(
//                    "🔥 RIGHTS = {}",
//                    rights.fasthopper$getExtraRights()
//            );

//            ExampleMod.LOGGER.info(
//                    "🔥 START RIGHTS = {}",
//                    rights.fasthopper$getExtraRights()
//            );

            int transfersThisTick = 0;
            int maxTransfersPerTick = 16;

            while (
                    rights.fasthopper$getExtraRights() > 0
                            &&
                            transfersThisTick < maxTransfersPerTick
            )
            {

                rights.fasthopper$consumeRight();

//                ExampleMod.LOGGER.info(
//                        "🔥 CONSUME {} = {}",
//                        System.identityHashCode(entity),
//                        rights.fasthopper$getExtraRights()
//                );

                transfersThisTick++;

//                ExampleMod.LOGGER.info(
//                        "🔥 RIGHTS AFTER = {}",
//                        rights.fasthopper$getExtraRights()
//                );

//                ExampleMod.LOGGER.info(
//                        "🔥 SLOT EMPTY = {}",
//                        entity.isEmpty()
//                );

                HopperBlockEntityAccessor
                        .fasthopper$invokeEjectItems(
                                level,
                                pos,
                                entity
                        );

//                ExampleMod.LOGGER.info(
//                        "🔥 EJECT {} = {}",
//                        i,
//                        result
//                );

                HopperBlockEntityAccessor
                        .fasthopper$invokeSuckInItems(
                                level,
                                entity
                        );

//                ExampleMod.LOGGER.info(
//                        "🔥 SUCK {} = {}",
//                        i,
//                        suckResult
//                );

//                ExampleMod.LOGGER.info(
//                        "🔥 COOLDOWN = {}",
//                        ((HopperBlockEntityAccessor)(Object)entity)
//                                .fasthopper$getCooldownTime()
//                );
            }

            rights.fasthopper$setInsideExtraTransfer(
                    false
            );

//            ExampleMod.LOGGER.info(
//                    "🔥 TOTAL EJECT = {}",
//                    ejectCount
//            );

//            entity.setCooldown(0);

//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA EJECT END"
//            );

//            boolean result =
//                    HopperBlockEntityAccessor
//                            .fasthopper$invokeTryMoveItems(
//                                    level,
//                                    pos,
//                                    state,
//                                    entity,
//                                    () -> true
//                            );
//
//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA CALL = {}",
//                    result
//            );

//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA READY {}",
//                    transferAmount
//            );

//            ExampleMod.LOGGER.info(
//                    "🔥 EXTRA AMOUNT = {} | EMPTY={}",
//                    transferAmount,
//                    entity.isEmpty()
//            );


//                boolean result =
//                        HopperBlockEntityAccessor
//                                .fasthopper$invokeTryMoveItems(
//                                        level,
//                                        pos,
//                                        state,
//                                        entity,
//                                        () -> true
//                                );
//
//                ExampleMod.LOGGER.info(
//                        "🔥 EXTRA CALL = {}",
//                        result
//                );

//                ExampleMod.LOGGER.info(
//                        "🔥 EXTRA TRYMOVE = {}",
//                        result
//                );
        }
    }

//    @Inject(
//            method = "ejectItems",
//            at = @At("RETURN")
//    )
//    private static void fasthopper$ejectDebug(
//            Level level,
//            BlockPos pos,
//            HopperBlockEntity hopper,
//            CallbackInfoReturnable<Boolean> cir
//    ) {
//        if (cir.getReturnValue()) {
//            ExampleMod.LOGGER.info(
//                    "🔥 EJECT SUCCESS {}",
//                    pos
//            );
//        }
//    }
}