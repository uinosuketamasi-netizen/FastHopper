package com.example.fasthopper;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/*
 * ============================================================
 * 🔥 FastHopperSlider
 * ============================================================
 *
 * 🎯 このクラスがやること
 * ・Speed Slider本体🔥
 * ・棒位置管理🔥
 * ・文字同期🔥
 * ・Runtime同期🔥
 * ・入力欄との無限ループ競合を完全修正🔥
 *
 * ❌ やらないこと
 * ・Config保存ファイル直接操作
 * ・GUI全体管理
 *
 * 💡 一言
 * 👉「完全バグ修正版・Speed Slider専用クラス🔥」
 * ============================================================
 */
@SuppressWarnings("all")
public class FastHopperSlider
        extends AbstractSliderButton
{
    /*
     * ============================================================
     * 🔥 EditBox参照
     * ============================================================
     */
    private final EditBox speedBox;

    /*
     * ============================================================
     * 🔥 内部的な自動同期ループを防ぐためのセーフティガード
     * ============================================================
     */
    private boolean fasthopper$isInternalSync = false;

    /*
     * ============================================================
     * 🔥 コンストラクタ
     * ============================================================
     */
    public FastHopperSlider(
            int x,
            int y,
            int width,
            int height,
            EditBox speedBox
    )
    {
        super(
                x,
                y,
                width,
                height,
                Component.literal("Speed"),
                (FastHopperConfig.HOPPER_SPEED - 0.1)
                        / (
                        FastHopperConfig.getMaxSpeed() - 0.1
                )
        );

        this.speedBox = speedBox;

        updateMessage();
    }

    /*
     * ============================================================
     * 🔥 表示更新
     * ============================================================
     */
    @Override
    protected void updateMessage()
    {
        double speed =
                0.1 + (
                        value
                                * (getMaxSpeed() - 0.1)
                );

        setMessage(
                Component.literal(
                        String.format(
                                "Speed : %.1f",
                                speed
                        )
                )
        );
    }

    /*
     * ============================================================
     * 🔥 値反映（スライダーのドラッグ操作時に実行）
     * ============================================================
     */
    @Override
    protected void applyValue()
    {
        // 外部（EditBoxのResponderなど）からの同期中の場合は逆流を防ぐためスキップ
        if (fasthopper$isInternalSync) return;

        double speed =
                0.1 + (
                        value
                                * (getMaxSpeed() - 0.1)
                );

        /*
         * ===========================================
         * 🔥 同値更新防止
         * ===========================================
         */
        if (
                Math.abs(
                        FastHopperConfig.HOPPER_SPEED
                                - speed
                ) < 0.01
        )
        {
            return;
        }

        /*
         * ===========================================
         * 🔥 Runtime同期
         * ===========================================
         */
        FastHopperConfig.HOPPER_SPEED = speed;

        /*
         * ===========================================
         * 🔥 Runtime即同期高速化
         * ===========================================
         */
        com.example.fasthopper.Config.saveRuntimeToConfig();

        /*
         * ===========================================
         * 🚨 バグ修正：EditBoxへの安全なメッセージ同期
         * ===========================================
         * ユーザーが文字入力欄を直接カチカチとタイピング操作している最中は、
         * つまみ側の微細なドラッグ判定で文字入力（ドットなど）を強制上書き破壊
         * しないようにセーフティガードを敷きます。
         */
        if (speedBox != null && !speedBox.isFocused())
        {
            try {
                // 無限ループ防止フラグを立ててから値を送り込む
                fasthopper$isInternalSync = true;
                speedBox.setValue(String.format("%.1f", speed));
                fasthopper$isInternalSync = false;
            } catch (Exception ignored) {
                fasthopper$isInternalSync = false;
            }
        }
    }

    /*
     * ============================================================
     * 🔥 外部同期（キーボード入力やボタン操作をスライダーのつまみ位置に滑らかに反映）
     * ============================================================
     */
    public void syncFromSpeed(
            double speed
    )
    {
        // ループによるスライダー位置のガタつきを防止
        fasthopper$isInternalSync = true;

        this.value =
                Mth.clamp(
                        (speed - 0.1)
                                / (getMaxSpeed() - 0.1),
                        0.0,
                        1.0
                );

        updateMessage();

        fasthopper$isInternalSync = false;
    }

    /*
     * ============================================================
     * 🔥 最大速度取得
     * ============================================================
     */
    private double getMaxSpeed()
    {
        return FastHopperConfig.getMaxSpeed();
    }
}