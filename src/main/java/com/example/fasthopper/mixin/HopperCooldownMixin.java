package com.example.fasthopper.mixin;

import com.example.fasthopper.Config;
import com.example.fasthopper.ExampleMod;
import com.example.fasthopper.FastHopperConfig;

import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * ===============================================
 * Fast Hopper Cooldown Mixin
 * ===============================================
 *
 * 目的:
 * ・ホッパー全体を高速化
 * ・バニラ搬送処理は壊さない
 * ・吸い込み / 押し出し / 仕分け / コンパレーター互換を優先
 *
 * 重要:
 * ・追加搬送はしない
 * ・Invokerは使わない
 * ・cooldownだけ短縮する
 * ===============================================
 */
@SuppressWarnings("all")
@Mixin(HopperBlockEntity.class)
public abstract class HopperCooldownMixin
{
    @Shadow
    private int cooldownTime;

    @Unique
    private static double fasthopper$lastLoggedSpeed = -1.0D;

    @Unique
    private static int fasthopper$lastLoggedCooldown = -1;

    @Inject(
            method = "setCooldown",
            at = @At("TAIL")
    )
    private void fasthopper$setCooldown(
            int cooldown,
            CallbackInfo ci
    )
    {
//        ExampleMod.LOGGER.info(
//                "🔥 COOLDOWN INPUT = {}",
//                cooldown
//        );
        if (!FastHopperConfig.ENABLED)
        {
            return;
        }

        if (cooldown <= 0)
        {
            return;
        }

        double speed =
                FastHopperConfig.getSafeSpeed();

        speed =
                Math.max(
                        0.1D,
                        speed
                );

        if (
                Config.TPS_PROTECTION.get()
                        &&
                        speed > 128.0D
        )
        {
            speed = 128.0D;
        }

        int newCooldown =
                Math.max(
                        1,
                        (int)Math.round(
                                8.0D / speed
                        )
                );

        this.cooldownTime =
                newCooldown;

        if (
                FastHopperConfig.ENABLE_LOG
                        &&
                        (
                                Math.abs(speed - fasthopper$lastLoggedSpeed) > 0.001D
                                        ||
                                        newCooldown != fasthopper$lastLoggedCooldown
                        )
        )
        {
            fasthopper$lastLoggedSpeed = speed;
            fasthopper$lastLoggedCooldown = newCooldown;

            ExampleMod.LOGGER.info(
                    "⚡ Fast Hopper | Speed={}x | Cooldown={}tick",
                    speed,
                    newCooldown
            );
        }
    }
}