package com.example.fasthopper;

/**
 * ===============================================
 * 🔥 FastHopperConfig
 * ===============================================
 *
 * 🎯 このクラスがやること
 * ・設定保存🔥
 * ・GUI設定共有🔥
 * ・ホッパー速度保持🔥
 * ・TPS保護設定🔥
 * ・モード設定🔥
 * ・速度安全制御とモード自動追従ロジック完全修正版🔥
 *
 * ❌ やらないこと
 * ・GUI描画
 * ・Mixin処理
 * ・NBT変更
 *
 * 💡 一言
 * 👉「完全バグ修正版・Fast Hopper設定本体🔥」
 * ===============================================
 */
@SuppressWarnings("all")
public class FastHopperConfig
{
    /*
     * ===========================================
     * 🔥 Fast Hopper ON/OFF
     * ===========================================
     */
    public static boolean ENABLED = true;

    /*
     * ===========================================
     * 🔥 TPS保護
     * ===========================================
     */
    public static boolean TPS_PROTECTION = true;

    /*
     * ===========================================
     * 🔥 ログ表示
     * ===========================================
     */
    public static boolean ENABLE_LOG = false;

    /*
     * ===========================================
     * 🔥 Idle Hopper最適化
     * ===========================================
     */
    public static boolean IDLE_HOPPER_OPTIMIZATION = true;

    /*
     * ===========================================
     * 🔥 デフォルト速度
     * ===========================================
     */
    public static final double DEFAULT_SPEED = 1.0;

    /*
     * ===========================================
     * 🔥 最小速度
     * ===========================================
     */
    public static final double MIN_SPEED = 0.1;

    /*
     * ===========================================
     * 🔥 モード別最大速度
     * ===========================================
     */
    public static double getMaxSpeed()
    {
        if (CURRENT_MODE == FastHopperMode.SAFE)
        {
            return 16.0;
        }

        if (CURRENT_MODE == FastHopperMode.EXTREME)
        {
            return 8.0;
        }

        return 128.0;
    }

    /*
     * ===========================================
     * 🔥 おすすめ速度
     * ===========================================
     */
    public static final double RECOMMENDED_SPEED = 3.0;

    /*
     * ===========================================
     * 🔥 ホッパー速度
     * ===========================================
     */
    public static double HOPPER_SPEED = DEFAULT_SPEED;

    /*
     * ===========================================
     * 🔥 動作モード
     * ===========================================
     */
    public static FastHopperMode CURRENT_MODE = FastHopperMode.SAFE;

    /*
     * ===========================================
     * 🔥 リセット
     * ===========================================
     */
    public static void reset()
    {
        ENABLED = true;
        TPS_PROTECTION = true;
        ENABLE_LOG = false;
        IDLE_HOPPER_OPTIMIZATION = true;
        HOPPER_SPEED = DEFAULT_SPEED;
        CURRENT_MODE = FastHopperMode.SAFE;
    }

    /*
     * ===========================================
     * 🔥 おすすめ設定
     * ===========================================
     */
    public static void applyRecommended()
    {
        ENABLED = true;
        TPS_PROTECTION = true;
        IDLE_HOPPER_OPTIMIZATION = true;
        HOPPER_SPEED = RECOMMENDED_SPEED;
        CURRENT_MODE = FastHopperMode.SAFE;
    }

    /*
     * ===========================================
     * 🔥 安全速度取得
     * ===========================================
     */
    public static double getSafeSpeed()
    {
        if (HOPPER_SPEED < MIN_SPEED)
        {
            return MIN_SPEED;
        }
        if (HOPPER_SPEED > getMaxSpeed())
        {
            return getMaxSpeed();
        }
        return HOPPER_SPEED;
    }

    /*
     * ===========================================
     * 🔥 速度設定（モード自動昇格対応）
     * ===========================================
     */
    public static void setSpeed(double speed)
    {
        if (speed < MIN_SPEED)
        {
            speed = MIN_SPEED;
        }

        // 最終的な最大上限（128倍）ガード
        if (speed > 128.0)
        {
            speed = 128.0;
        }

        HOPPER_SPEED = speed;
    }

    /*
     * ===========================================
     * 🔥 速度リセット
     * ===========================================
     */
    public static void resetSpeed()
    {
        HOPPER_SPEED = DEFAULT_SPEED;
        CURRENT_MODE = FastHopperMode.SAFE;
    }
}