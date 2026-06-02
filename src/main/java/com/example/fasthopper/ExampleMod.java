package com.example.fasthopper;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * ===============================================
 * 🔥 ExampleMod
 * ===============================================
 *
 * 🎯 このクラスがやること
 * ・MOD起動管理🔥
 * ・Config登録🔥
 * ・Config同期🔥
 * ・初期化🔥
 *
 * ❌ やらないこと
 * ・ホッパー処理
 * ・Mixin処理
 * ・NBT処理
 * ・GUI描画
 *
 * 💡 一言
 * 👉「MODの起動本体🔥」
 * ===============================================
 */
@Mod(ExampleMod.MODID)
@SuppressWarnings("all")
public class ExampleMod
{
    /*
     * ===============================================
     * 🔥 MOD ID
     * ===============================================
     */
    public static final String MODID = "fasthopper";

    /*
     * ===============================================
     * 🔥 ロガー
     * ===============================================
     */
    public static final Logger LOGGER =
            LogUtils.getLogger();

    /*
     * ===============================================
     * 🔥 コンストラクタ
     * ===============================================
     *
     * 🎯 この関数がやること
     * ・MOD初期化🔥
     * ・Config登録🔥
     * ・イベント登録🔥
     *
     * 💡 一言
     * 👉「MOD起動開始🔥」
     * ===============================================
     */
    public ExampleMod(
            IEventBus modEventBus,
            ModContainer modContainer
    )
    {
        /*
         * ===============================================
         * 🔥 commonSetup登録
         * ===============================================
         */
        modEventBus.addListener(
                this::commonSetup
        );

        /*
         * ===============================================
         * 🔥 Config登録
         * ===============================================
         */
        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                Config.SPEC
        );
    }

    /*
     * ===============================================
     * 🔥 commonSetup
     * ===============================================
     *
     * 🎯 この関数がやること
     * ・Config同期🔥
     * ・初期設定反映🔥
     *
     * 💡 一言
     * 👉「MOD初期化本体🔥」
     * ===============================================
     */
    private void commonSetup(
            FMLCommonSetupEvent event
    )
    {
        /*
         * ===============================================
         * 🔥 Config同期
         * ===============================================
         */
        Config.syncToRuntime();

        /*
         * ===============================================
         * 🔥 起動ログ
         * ===============================================
         */
        LOGGER.info(
                "Fast Hopper 起動🔥"
        );

        LOGGER.info(
                "現在速度倍率: {}",
                FastHopperConfig.HOPPER_SPEED
        );

        LOGGER.info(
                "現在モード: {}",
                FastHopperConfig.CURRENT_MODE
        );
    }
}