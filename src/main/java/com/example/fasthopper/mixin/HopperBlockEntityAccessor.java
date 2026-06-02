package com.example.fasthopper.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HopperBlockEntity.class)
public interface HopperBlockEntityAccessor
{


    @Invoker("suckInItems")
    static boolean fasthopper$invokeSuckInItems(
            Level level,
            net.minecraft.world.level.block.entity.Hopper hopper
    )
    {
        throw new AssertionError();
    }

    @Invoker("ejectItems")
    static boolean fasthopper$invokeEjectItems(
            Level level,
            BlockPos pos,
            HopperBlockEntity hopper
    )
    {
        throw new AssertionError();
    }

    @org.spongepowered.asm.mixin.gen.Accessor("cooldownTime")
    int fasthopper$getCooldownTime();
}