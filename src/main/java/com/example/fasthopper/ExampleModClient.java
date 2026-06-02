package com.example.fasthopper;

import com.example.fasthopper.gui.FastHopperConfigScreen;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.ModContainer;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/*
 * ===============================================
 * 🔥 ExampleModClient
 * ===============================================
 *
 * 🎯 このクラスがやること
 * ・クライアント初期化🔥
 * ・GUI登録🔥
 * ・Config画面登録🔥
 * ・クライアントログ🔥
 *
 * ❌ やらないこと
 * ・ホッパー処理
 * ・Mixin処理
 * ・NBT変更
 * ・サーバー処理
 *
 * 💡 一言
 * 👉「クライアントGUI本体🔥」
 * ===============================================
 */
@SuppressWarnings("all")
@Mod(
        value = ExampleMod.MODID,
        dist = Dist.CLIENT
)
@EventBusSubscriber(
        modid = ExampleMod.MODID,
        value = Dist.CLIENT
)
public class ExampleModClient
{
    /*
     * ===============================================
     * 🔥 コンストラクタ
     * ===============================================
     *
     * 🎯 この関数がやること
     * ・独自GUI登録🔥
     * ・Mods画面Config対応🔥
     *
     * 💡 一言
     * 👉「GUI登録本体🔥」
     * ===============================================
     */
    public ExampleModClient(
            ModContainer container
    )
    {
        /*
         * ===============================================
         * 🔥 完全独自GUI登録
         * ===============================================
         */
        container.registerExtensionPoint(
                IConfigScreenFactory.class,

                (
                        minecraft,
                        parent
                ) ->
                        new FastHopperConfigScreen(parent)
        );
    }

    /*
     * ===============================================
     * 🔥 Client Setup
     * ===============================================
     *
     * 🎯 この関数がやること
     * ・クライアント初期化🔥
     * ・ログ表示🔥
     *
     * 💡 一言
     * 👉「クライアント起動🔥」
     * ===============================================
     */
    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event
    )
    {
        /*
         * ===============================================
         * 🔥 起動ログ
         * ===============================================
         */
        ExampleMod.LOGGER.info(
                "🔥 Fast Hopper Client 起動"
        );

        /*
         * ===============================================
         * 🔥 プレイヤー名ログ
         * ===============================================
         */
        ExampleMod.LOGGER.info(
                "Player: {}",
                Minecraft.getInstance()
                        .getUser()
                        .getName()
        );
    }
}