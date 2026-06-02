package com.example.fasthopper.mixin;

import com.example.fasthopper.accessor.ExtraRightsAccessor;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.fasthopper.FastHopperConfig;
import com.example.fasthopper.FastHopperMode;

import org.spongepowered.asm.mixin.Unique;
import com.example.fasthopper.ExampleMod;
/*
 * ===============================================
 * Fast Hopper ExtraModeMixin
 * ===============================================
 *
 * 今は安全のため無効化。
 *
 * 理由:
 * ・setCooldown HEAD で追加搬送すると再呼び出しが増える
 * ・Invoker搬送は後で別設計にする
 * ・まずはホッパー全体高速化を安定させる
 *
 * このクラスは mixins.json に残っていてもクラッシュしないように
 * 空のMixinとして残す。
 * ===============================================
 */
@Mixin(HopperBlockEntity.class)
public class ExtraModeMixin
        implements ExtraRightsAccessor
{

    @Unique
    private double fasthopper$extraProgress = 0.0D;

    @Unique
    private int fasthopper$extraRights = 0;

    @Unique
    private boolean fasthopper$insideExtraTransfer = false;

    @Unique
    public int fasthopper$getExtraRights()
    {
        return fasthopper$extraRights;
    }

    @Unique
    public void fasthopper$consumeRight()
    {
        if (fasthopper$extraRights > 0)
        {
            fasthopper$extraRights--;
        }
    }

    @Unique
    public boolean fasthopper$isInsideExtraTransfer()
    {
        return fasthopper$insideExtraTransfer;
    }

    @Unique
    public void fasthopper$setInsideExtraTransfer(
            boolean value
    )
    {
        fasthopper$insideExtraTransfer = value;
    }

    @Inject(
            method = "setCooldown",
            at = @At("TAIL")
    )
    private void fasthopper$extraDebug(
            int time,
            CallbackInfo ci
    ) {
        if (
                FastHopperConfig.CURRENT_MODE
                        != FastHopperMode.EXTRA)
        {
            return;
        }

        if (time <= 0)
        {
            return;
        }

        if (fasthopper$insideExtraTransfer)
        {
            return;
        }

        fasthopper$extraProgress +=
                Math.max(
                        0.0D,
                        FastHopperConfig.getSafeSpeed() - 8.0D
                );

        while (fasthopper$extraProgress >= 8.0D)
        {
            fasthopper$extraProgress -= 8.0D;

//            ExampleMod.LOGGER.info(
//                    "🔥 COOLDOWN CALL {}",
//                    time
//            );

            fasthopper$extraRights++;

//            ExampleMod.LOGGER.info(
//                    "🔥 ADD {} = {}",
//                    System.identityHashCode(this),
//                    fasthopper$extraRights
//            );

//            System.out.println(
//                    "GENERATE = "
//                            + fasthopper$extraRights
//            );

            if (fasthopper$extraRights > 100)
            {
                fasthopper$extraRights = 100;
            }

//            if (fasthopper$extraRights != fasthopper$lastRights)
//            {
//                System.out.println(
//                        "RIGHTS = "
//                                + fasthopper$extraRights
//                );
//
//                fasthopper$lastRights =
//                        fasthopper$extraRights;
//            }

//            if (
//                    fasthopper$extraRights % 50 == 0
//                            &&
//                            fasthopper$extraRights < 100
//            )
//            {
//                System.out.println(
//                        "EXTRA RIGHTS = "
//                                + fasthopper$extraRights
//                );
//            }
        }

//        if (fasthopper$extraProgress % 20 == 0)
//        {
//            System.out.println(
//                    "EXTRA Progress = "
//                            + fasthopper$extraProgress
//            );
//        }
    }
}