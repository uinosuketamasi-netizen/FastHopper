package com.example.fasthopper;

import net.neoforged.neoforge.common.ModConfigSpec;

/*
 * ===============================================
 * 🔥 Config
 * ===============================================
 *
 * 🎯 このクラスがやること
 * ・NeoForge設定GUI管理🔥
 * ・カテゴリ分け🔥
 * ・config保存🔥
 * ・ゲーム内設定変更🔥
 * ・FastHopperConfig連携🔥
 * ・GUI説明文管理🔥
 * ・Runtime同期不整合バグ完全修正版🔥
 * ・0.1～128倍対応🔥
 * ・SAFE / EXTREME / EXTRA対応🔥
 * ・永続保存🔥
 *
 *
 * ❌ やらないこと
 * ・ホッパー処理
 * ・Mixin処理
 * ・NBT変更
 *
 * 💡 一言
 * 👉「完全バグ修正版・Fast Hopper設定本体🔥」
 * ===============================================
 */
@SuppressWarnings("all")
public class Config
{
    /*
     * ===============================================
     * 🔥 Builder
     * ===============================================
     */
    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    /*
     * ===============================================
     * 🔥 一般設定カテゴリ
     * ===============================================
     */
    static
    {
        BUILDER
                .comment("一般設定")
                .translation(
                        "fasthopper.configuration.general"
                )
                .push("general");
    }

    /*
     * ===============================================
     * 🔥 デバッグログ
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue ENABLE_LOG =
            BUILDER
                    .comment(
                            "ホッパー動作ログを表示します。",
                            "問題調査やデバッグ用です。",
                            "通常プレイではOFF推奨。"
                    )
                    .translation(
                            "fasthopper.configuration.enableLog"
                    )
                    .define(
                            "enableLog",
                            false
                    );

    /*
     * ===============================================
     * 🔥 一般カテゴリ終了
     * ===============================================
     */
    static
    {
        BUILDER.pop();
    }

    /*
     * ===============================================
     * 🔥 パフォーマンスカテゴリ
     * ===============================================
     */
    static
    {
        BUILDER
                .comment("パフォーマンス設定")
                .translation(
                        "fasthopper.configuration.performance"
                )
                .push("performance");
    }

    /*
     * ===============================================
     * 🔥 ホッパー速度倍率
     * ===============================================
     */
    public static final ModConfigSpec.DoubleValue HOPPER_SPEED =
            BUILDER
                    .comment(
                            "ホッパー搬送速度を変更します。",
                            "",
                            "0.1x = 約10分の1速度",
                            "1.0x = バニラ速度",
                            "8.0x = 1tick限界",
                            "",
                            "SAFE:",
                            "0.1～16倍",
                            "安全重視モード",
                            "",
                            "EXTREME:",
                            "0.1～8倍",
                            "1tick限界モード",
                            "",
                            "EXTRA:",
                            "0.1～128倍",
                            "平均搬送モード",
                            "",
                            "実測例:",
                            "16x = 約1.18秒",
                            "32x = 約0.50秒",
                            "64x = 約0.23秒",
                            "128x = 約0.14秒"
                    )
                    .translation(
                            "fasthopper.configuration.speed"
                    )
                    .defineInRange(
                            "hopperSpeed",
                            1.0,
                            0.1,
                            128.0
                    );

    /*
     * ===============================================
     * 🔥 現在モード保存
     * ===============================================
     */
    public static final ModConfigSpec.ConfigValue<String> CURRENT_MODE =
            BUILDER
                    .comment(
                            "現在のFast Hopperモード"
                    )
                    .translation(
                            "fasthopper.configuration.currentMode"
                    )
                    .define(
                            "currentMode",
                            FastHopperMode.SAFE.name()
                    );

    /*
     * ===============================================
     * 🔥 パフォーマンスカテゴリ終了
     * ===============================================
     */
    static
    {
        BUILDER.pop();
    }

    /*
     * ===============================================
     * 🔥 安全設定カテゴリ
     * ===============================================
     */
    static
    {
        BUILDER
                .comment("安全設定")
                .translation(
                        "fasthopper.configuration.safety"
                )
                .push("safety");
    }

    /*
     * ===============================================
     * 🔥 安全モード
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue SAFE_MODE =
            BUILDER
                    .comment(
                            "危険な高速設定を自動制限します。",
                            "TPS低下やラグを防止します。",
                            "通常はON推奨。"
                    )
                    .translation(
                            "fasthopper.configuration.safeMode"
                    )
                    .define(
                            "safeMode",
                            true
                    );

    /*
     * ===============================================
     * 🔥 Idle Hopper停止
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue
            IDLE_HOPPER_OPTIMIZATION =
            BUILDER
                    .comment(
                            "空ホッパーを省エネ化します。",
                            "大量ホッパー環境で",
                            "TPS改善が期待できます。",
                            "通常はON推奨。"
                    )
                    .translation(
                            "fasthopper.configuration.idleOptimization"
                    )
                    .define(
                            "idleHopperOptimization",
                            true
                    );

    /*
     * ===============================================
     * 🔥 TPS保護
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue TPS_PROTECTION =
            BUILDER
                    .comment(
                            "サーバーTPS低下時に",
                            "ホッパー速度を自動調整します。"
                    )
                    .translation(
                            "fasthopper.configuration.tpsProtection"
                    )
                    .define(
                            "tpsProtection",
                            true
                    );

    /*
     * ===============================================
     * 🔥 Fast Hopper有効
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue ENABLED =
            BUILDER
                    .comment(
                            "Fast Hopper全体を",
                            "有効 / 無効化します。"
                    )
                    .translation(
                            "fasthopper.configuration.enabled"
                    )
                    .define(
                            "enabled",
                            true
                    );

    /*
     * ===============================================
     * 🔥 チャンク保護
     * ===============================================
     */
    public static final ModConfigSpec.BooleanValue CHUNK_PROTECTION =
            BUILDER
                    .comment(
                            "未読み込みチャンクへの",
                            "危険アクセスを防止します。"
                    )
                    .translation(
                            "fasthopper.configuration.chunkProtection"
                    )
                    .define(
                            "chunkProtection",
                            true
                    );

    /*
     * ===============================================
     * 🔥 安全カテゴリ終了
     * ===============================================
     */
    static
    {
        BUILDER.pop();
    }

    /*
     * ===============================================
     * 🔥 SPEC
     * ===============================================
     */
    public static final ModConfigSpec SPEC =
            BUILDER.build();

    /*
     * ===============================================
     * 🔥 Config → Runtime同期
     * ===============================================
     */
    public static void syncToRuntime()
    {
        /*
         * ===========================================
         * 🔥 モード同期
         * ===========================================
         * Configに保存されたモードを
         * Runtimeへ同期します。
         */

        try
        {
            FastHopperConfig.CURRENT_MODE =
                    FastHopperMode.valueOf(
                            CURRENT_MODE.get()
                    );
        }
        catch (Exception ignored)
        {
            FastHopperConfig.CURRENT_MODE =
                    FastHopperMode.SAFE;
        }

        /*
         * ===========================================
         * 🔥 速度同期と安全チェック
         * ===========================================
         */
        double fileSpeed = HOPPER_SPEED.get();

        /*
         * ===========================================
         * 🔥 SAFEモード安全制限
         * ===========================================
         * SAFEモード時は
         * 最大16倍へ制限
         */

        if (SAFE_MODE.get() && FastHopperConfig.CURRENT_MODE == FastHopperMode.SAFE)
        {
            if (fileSpeed > 16.0)
            {
                fileSpeed = 16.0;
            }
        }

        FastHopperConfig.setSpeed(fileSpeed);

        /*
         * ===========================================
         * 🔥 その他の各種フラグ同期
         * ===========================================
         */
        FastHopperConfig.ENABLE_LOG = ENABLE_LOG.get();
        FastHopperConfig.TPS_PROTECTION = TPS_PROTECTION.get();
        FastHopperConfig.IDLE_HOPPER_OPTIMIZATION = IDLE_HOPPER_OPTIMIZATION.get();
        FastHopperConfig.ENABLED = ENABLED.get();
    }

    /*
     * ===============================================
     * 🔥 Runtime → Config保存
     * ===============================================
     */
    public static void saveRuntimeToConfig()
    {


        /*
         * ===========================================
         * 🔥 Runtime → Config保存
         * ===========================================
         * 現在のRuntime状態を
         * Configへ安全に保存します。
         *
         * GUI・コマンド・ロード後の
         * 設定不一致を防止します。
         */
        HOPPER_SPEED.set(
                FastHopperConfig.getSafeSpeed()
        );

        CURRENT_MODE.set(
                FastHopperConfig.CURRENT_MODE.name()
        );

        ENABLE_LOG.set(
                FastHopperConfig.ENABLE_LOG
        );

        ENABLED.set(
                FastHopperConfig.ENABLED
        );

        TPS_PROTECTION.set(
                FastHopperConfig.TPS_PROTECTION
        );

        IDLE_HOPPER_OPTIMIZATION.set(
                FastHopperConfig.IDLE_HOPPER_OPTIMIZATION
        );
    }

    /*
     * ===============================================
     * 🔥 Config永続保存
     * ===============================================
     */
    public static void save()
    {
        /*
         * ===========================================
         * 🔥 Runtime → Config同期
         * ===========================================
         */
        saveRuntimeToConfig();

        /*
         * ===========================================
         * 🔥 保存ログ
         * ===========================================
         */
        if (ENABLE_LOG.get())
        {
            ExampleMod.LOGGER.info(
                    "🔥 Fast Hopper Config 保存完了"
            );
        }

        /*
         * ===========================================
         * 🔥 Configファイル保存
         * ===========================================
         */
        SPEC.save();

        /*
         * ===========================================
         * 🔥 保存後最終同期
         * ===========================================
         * 保存後にRuntimeとConfigを
         * 最終同期します。
         *
         * 設定値のズレを防止します。
         */
        syncToRuntime();
    }
}