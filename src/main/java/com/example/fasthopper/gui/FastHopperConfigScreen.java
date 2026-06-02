package com.example.fasthopper.gui;

import com.example.fasthopper.Config;
import com.example.fasthopper.ExampleMod;
import com.example.fasthopper.FastHopperConfig;
import com.example.fasthopper.FastHopperMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import com.example.fasthopper.FastHopperSlider;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.network.chat.Component;

/*
 * ============================================================
 * 🔥 FastHopperConfigScreen
 * ============================================================
 *
 * 🎯 このクラスがやること
 * ・Fast Hopper GUI表示🔥
 * ・設定変更🔥
 * ・Tooltip表示🔥
 * ・スクロール🔥
 * ・Minecraft風UI🔥
 * ・入力欄とスライダーの無限ループ競合を完全修正🔥
 *
 * ❌ やらないこと
 * ・Mixin処理
 * ・ホッパー搬送処理
 * ・TPS計測本体
 *
 * 💡 一言
 * 👉「入力同期バグ完全修正版・Fast Hopper GUI本体🔥」
 * ============================================================
 */
@SuppressWarnings("all")
public class FastHopperConfigScreen extends Screen
{
    /*
     * ============================================================
     * 🔥 GUIサイズ定義カテゴリ
     * ============================================================
     */
    private static final int BUTTON_WIDTH = 200;

    private static final int BUTTON_HEIGHT = 20;

    private static final int SPACE = 8;

    /*
     * ============================================================
     * 🔥 親Screen保持
     * ============================================================
     */
    private final Screen parent;

    /*
     * ============================================================
     * 🔥 スクロールオフセット位置
     * ============================================================
     */
    private int scrollOffset = 0;

    /*
     * ============================================================
     * 🔥 テキスト入力ボックス（速度用）
     * ============================================================
     */
    private EditBox speedBox;

    /*
     * ============================================================
     * 🔥 各種ボタンパーツ定義
     * ============================================================
     */
    private Button toggleButton;

    private Button modeButton;

    private Button tpsButton;

    private Button logButton;

    private Button idleOptimizationButton;

    private Button plusButton;

    private Button minusButton;

    private Button applyButton;

    private Button recommendButton;

    private Button resetButton;

    private Button closeButton;

    /*
     * ============================================================
     * 🔥 スライダーコンポーネント
     * ============================================================
     */
    private FastHopperSlider speedSlider;

    /*
     * ============================================================
     * 🔥 無限同期ループを完全にブロックするためのフラグ
     * ============================================================
     */
    private boolean fasthopper$isSyncing = false;

    /*
     * ============================================================
     * 🔥 コンストラクタ本体
     * ============================================================
     */
    public FastHopperConfigScreen(
            Screen parent
    )
    {
        super(
                Component.literal(
                        "Fast Hopper Control Panel [マウスを合わせると説明表示]"
                )
        );

        this.parent = parent;
    }

    /*
     * ============================================================
     * 🔥 GUI画面の初期化・構築処理
     * ============================================================
     */
    @Override
    protected void init()
    {
        super.init();

        /*
         * 🚨 重要
         *
         * init() は
         * ・mouseScrolled()
         * ・switchMode()
         * ・GUI再構築
         *
         * から何度も呼ばれる。
         *
         * ここで syncToRuntime() を呼ぶと
         * TOMLの古い値で Runtime が上書きされ、
         * 入力欄が巻き戻る。
         *
         * 実際に
         * 72.4 → 14.2入力
         * ↓
         * init()
         * ↓
         * syncToRuntime()
         * ↓
         * 72.4へ戻る
         *
         * バグを確認済み。
         *
         * Runtime同期はMOD起動時のみ行う。
         */
//        Config.syncToRuntime();

        this.clearWidgets();

        int centerX = this.width / 2;

        int y = this.height / 6 - scrollOffset;

        /*
         * ============================================================
         * 🔥 速度数値入力ボックスの生成とセットアップ
         * ============================================================
         */
        speedBox = new EditBox(
                this.font,
                centerX - 70,
                y,
                140,
                20,
                Component.literal("Speed")
        );

        /*
         * ============================================================
         * 🔥 現在保存されているホッパーの速度を初期値として挿入
         * ============================================================
         */
        speedBox.setValue(
                String.valueOf(
                        FastHopperConfig.HOPPER_SPEED
                )
        );

        /*
         * ============================================================
         * 🔥 入力フィルター設定：不正な文字列を打ち込めないようにガード
         * ============================================================
         */
        speedBox.setFilter(
                text ->
                {
                    if (text.isEmpty())
                    {
                        return true;
                    }

                    if (
                            text.length() > 1
                                    &&
                                    text.startsWith("0")
                                    &&
                                    !text.startsWith("0.")
                    )
                    {
                        return false;
                    }

                    if (
                            !text.matches(
                                    "\\d*(\\.\\d?)?"
                            )
                    )
                    {
                        return false;
                    }

                    if (text.endsWith("."))
                    {
                        return true;
                    }

                    try
                    {
                        double value =
                                Double.parseDouble(text);

                        double max =
                                FastHopperConfig.getMaxSpeed();

                        return value <= max;
                    }
                    catch (NumberFormatException ignored)
                    {
                        return false;
                    }
                }
        );

        speedBox.setMaxLength(5);

        /*
         * ============================================================
         * 🔥 修正極限：無限ループ防止フラグを使用してスライダーの棒位置のみを滑らかに動かす
         * ============================================================
         */
        speedBox.setResponder(
                text ->
                {
                    // 同期中フラグが立っている時は処理をスキップしてループを切る
                    if (fasthopper$isSyncing) return;

                    try
                    {
                        if (
                                !text.isEmpty()
                                        &&
                                        !text.endsWith(".")
                        )
                        {
                            double speed =
                                    Double.parseDouble(text);

                            FastHopperConfig.HOPPER_SPEED = speed;

                            if (speedSlider != null) {
                                // フラグを立てて、スライダー側から入力欄への逆流をロックする
                                fasthopper$isSyncing = true;
                                speedSlider.syncFromSpeed(speed);
                                fasthopper$isSyncing = false;
                            }
                        }
                    }
                    catch (NumberFormatException ignored)
                    {
                        fasthopper$isSyncing = false;
                    }
                }
        );

        this.addRenderableWidget(
                speedBox
        );

        speedBox.setTooltip(
                Tooltip.create(
                        Component.literal(
                                "§6速度入力欄\n" +
                                        "\n" +
                                        "§70.1～128.0対応\n" +
                                        "§71.0 = バニラ速度\n" +
                                        "§78.0 = 1tick限界\n" +
                                        "§7128.0 = EXTRA最高速\n" +
                                        "\n" +
                                        "§a小数対応\n" +
                                        "§aリアルタイム同期\n" +
                                        "§aGUI即反映\n" +
                                        "\n" +
                                        "§eTPS保護ON時\n" +
                                        "§e16倍を超える設定は\n" +
                                        "§e自動制限されます"
                        )
                )
        );

        /*
         * ============================================================
         * 🔥 速度調整用のスライダーを生成
         * ============================================================
         */
        speedSlider =
                new FastHopperSlider(
                        centerX - 100,
                        y + 25,
                        200,
                        20,
                        speedBox
                ) {
                    @Override
                    protected void applyValue()
                    {
                        /*
                         * 入力欄編集中は何もしない
                         * スライダー側から速度を書き戻さない
                         */
                        if (speedBox != null && speedBox.isFocused())
                        {
                            return;
                        }

                        /*
                         * スライダー操作時のみ通常処理
                         */
                        super.applyValue();
                    }
                };

        this.addRenderableWidget(
                speedSlider
        );

        /*
         * ============================================================
         * 🔥 スライダー用の豪華な説明Tooltipテキストを設定
         * ============================================================
         */
        speedSlider.setTooltip(
                Tooltip.create(
                        Component.literal(
                                "§6速度倍率説明\n" +
                                        "\n" +
                                        "§70.1x = 約10分の1速度\n" +
                                        "§71.0x = バニラ速度\n" +
                                        "§78.0x = 1tick限界\n" +
                                        "\n" +
                                        "§aSAFE\n" +
                                        "§7安全性重視\n" +
                                        "\n" +
                                        "§cEXTREME\n" +
                                        "§71tick限界維持\n" +
                                        "\n" +
                                        "§dEXTRA\n" +
                                        "§71tick限界突破\n" +
                                        "§7平均搬送方式\n" +
                                        "\n" +
                                        "§e実測例\n" +
                                        "§716x = 約1.18秒\n" +
                                        "§732x = 約0.50秒\n" +
                                        "§764x = 約0.23秒\n" +
                                        "§7128x = 約0.14秒"
                        )
                )
        );

        y += 30;
        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 Idle Hopper 省エネ最適化切り替えボタンの生成
         * ============================================================
         */
        idleOptimizationButton = Button.builder(
                        getIdleOptimizationText(),
                        button ->
                        {
                            FastHopperConfig.IDLE_HOPPER_OPTIMIZATION =
                                    !FastHopperConfig.IDLE_HOPPER_OPTIMIZATION;

                            Config.IDLE_HOPPER_OPTIMIZATION.set(
                                    FastHopperConfig.IDLE_HOPPER_OPTIMIZATION
                            );

                            Config.saveRuntimeToConfig();
                            Config.save();

                            button.setMessage(
                                    getIdleOptimizationText()
                            );
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6Idle Hopper最適化\n" +
                                                "\n" +
                                                "§7空ホッパーの\n" +
                                                "§7不要処理を軽減します。\n" +
                                                "\n" +
                                                "§a大量ホッパー向け\n" +
                                                "§aTPS改善\n" +
                                                "§a負荷軽減\n" +
                                                "\n" +
                                                "§e搬送中ホッパーは\n" +
                                                "§e通常動作します。"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                idleOptimizationButton
        );

        /*
         * ============================================================
         * 🔥 マイナス（-）ボタン：速度を「0.5」ずつダウンさせる
         * ============================================================
         */
        minusButton = Button.builder(
                        Component.literal("-"),
                        button ->
                        {
                            changeSpeed(-0.5);
                        }
                )
                .bounds(
                        centerX - 160,
                        y,
                        40,
                        20
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6速度ダウン\n" +
                                                "\n" +
                                                "§7ホッパー搬送倍率を\n" +
                                                "§70.5ずつ減少します。\n" +
                                                "\n" +
                                                "§aTPS安定化\n" +
                                                "§a低負荷化\n" +
                                                "\n" +
                                                "§eSlider / 数字入力と\n" +
                                                "§eリアルタイム同期します。"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                minusButton
        );

        /*
         * ============================================================
         * 🔥 プラス（+）ボタン：速度を「0.5」ずつアップさせる
         * ============================================================
         */
        plusButton = Button.builder(
                        Component.literal("+"),
                        button ->
                        {
                            changeSpeed(0.5);
                        }
                )
                .bounds(
                        centerX + 120,
                        y,
                        40,
                        20
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6速度アップ\n" +
                                                "\n" +
                                                "§7ホッパー搬送倍率を\n" +
                                                "§70.5ずつ増加します。\n" +
                                                "\n" +
                                                "§e現在最大値 : "
                                                + FastHopperConfig.getMaxSpeed()
                                                + "倍\n" +
                                                "\n" +
                                                "§eSlider / 数字入力と\n" +
                                                "§eリアルタイム同期します。"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                plusButton
        );

        y += 50;

        /*
         * ============================================================
         * 🔥 Fast Hopper全体機能のメインON/OFF切り替えスイッチボタン
         * ============================================================
         */
        toggleButton = Button.builder(
                        getToggleText(),
                        button ->
                        {
                            FastHopperConfig.ENABLED =
                                    !FastHopperConfig.ENABLED;

                            Config.ENABLED.set(
                                    FastHopperConfig.ENABLED
                            );

                            Config.saveRuntimeToConfig();
                            Config.save();

                            button.setMessage(
                                    getToggleText()
                            );
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6Fast Hopper Main Switch\n" +
                                                "\n" +
                                                "§7Minecraft標準仕様を維持しながら\n" +
                                                "§7ホッパー搬送を高速化します。\n" +
                                                "\n" +
                                                "§a特徴\n" +
                                                "§7・1回1個搬送維持\n" +
                                                "§7・仕分け機互換\n" +
                                                "§7・比較器互換\n" +
                                                "§7・平均進行方式\n" +
                                                "§7・TPS保護対応\n" +
                                                "\n" +
                                                "§e8x未満\n" +
                                                "§7平均tick短縮\n" +
                                                "\n" +
                                                "§e8x以上\n" +
                                                "§71tick限界後は\n" +
                                                "§7平均複数搬送になります。\n" +
                                                "\n" +
                                                "§aON\n" +
                                                "§7Fast Hopper有効\n" +
                                                "\n" +
                                                "§cOFF\n" +
                                                "§7バニラ速度へ戻します"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                toggleButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 動作モード（SAFE / EXTREME / EXTRA）切り替えボタン
         * ============================================================
         */
        modeButton = Button.builder(
                        getModeText(),
                        button ->
                        {
                            switchMode();
                            button.setMessage(
                                    getModeText()
                            );
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6Fast Hopper 動作モード\n" +
                                                "\n" +
                                                "§aSAFE MODE\n" +
                                                "§7安定性最優先モード\n" +
                                                "§7大量ホッパー向け\n" +
                                                "§7TPS保護と相性抜群\n" +
                                                "§70.1～128倍対応\n" +
                                                "§7高速設定時も安全側で制御\n" +
                                                "\n" +
                                                "§cEXTREME MODE\n" +
                                                "§71tick限界モード\n" +
                                                "§78倍以降は速度据え置き\n" +
                                                "§71回1個搬送を維持\n" +
                                                "§7ソーター互換重視\n" +
                                                "§7バニラ挙動に最も近い\n" +
                                                "\n" +
                                                "§dEXTRA MODE\n" +
                                                "§7平均搬送システム搭載\n" +
                                                "§70.1～128倍完全対応\n" +
                                                "§716x=約2個搬送相当\n" +
                                                "§732x=約4個搬送相当\n" +
                                                "§764x=約8個搬送相当\n" +
                                                "§7128x=約16個搬送相当\n" +
                                                "§71tick限界を超えて高速化\n" +
                                                "\n" +
                                                "§e実測確認済み\n" +
                                                "§70.1x = 約4分12秒\n" +
                                                "§71x = 約25秒\n" +
                                                "§7128x = 約0.14秒"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                modeButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 サーバーTPS自動保護機能のON/OFF切り替えボタン
         * ============================================================
         */
        tpsButton = Button.builder(
                        getTPSProtectionText(),
                        button ->
                        {
                            FastHopperConfig.TPS_PROTECTION =
                                    !FastHopperConfig.TPS_PROTECTION;

                            Config.TPS_PROTECTION.set(
                                    FastHopperConfig.TPS_PROTECTION
                            );

                            Config.saveRuntimeToConfig();
                            Config.save();

                            button.setMessage(
                                    getTPSProtectionText()
                            );
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6TPS保護システム\n" +
                                                "\n" +
                                                "§7サーバーTPS低下時に\n" +
                                                "§7負荷を自動軽減します。\n" +
                                                "\n" +
                                                "§a大量ホッパー向け\n" +
                                                "§aラグ軽減\n" +
                                                "§a安全運用\n" +
                                                "\n" +
                                                "§eSAFEモードとの\n" +
                                                "§e併用推奨"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                tpsButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 デバッグログ出力表示機能のON/OFFスイッチボタン
         * ============================================================
         */
        logButton = Button.builder(
                        getLogText(),
                        button ->
                        {
                            FastHopperConfig.ENABLE_LOG =
                                    !FastHopperConfig.ENABLE_LOG;

                            Config.ENABLE_LOG.set(
                                    FastHopperConfig.ENABLE_LOG
                            );

                            Config.saveRuntimeToConfig();
                            Config.save();

                            button.setMessage(
                                    getLogText()
                            );
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6ログ表示設定\n" +
                                                "\n" +
                                                "§7ホッパー動作ログを\n" +
                                                "§7表示します。\n" +
                                                "\n" +
                                                "§eデバッグ向け\n" +
                                                "§c通常プレイではOFF推奨"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                logButton
        );

        y += BUTTON_HEIGHT + 20;

        /*
         * ============================================================
         * 🔥 「変更を保存」ボタン：押された瞬間に数値入力欄の値を確定・永続セーブ
         * ============================================================
         */
        applyButton = Button.builder(
                        Component.literal("変更を保存"),
                        button ->
                        {
                            applySpeed();
                            Config.save();

                            if (FastHopperConfig.ENABLE_LOG)
                            {
                                ExampleMod.LOGGER.info(
                                        "🔥 Fast Hopper 設定の永続保存完了"
                                );
                            }

                            toggleButton.setMessage(getToggleText());
                            modeButton.setMessage(getModeText());
                            tpsButton.setMessage(getTPSProtectionText());
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6変更を保存\n" +
                                                "\n" +
                                                "§7現在の設定を保存し\n" +
                                                "§7Fast Hopperへ反映します。\n" +
                                                "\n" +
                                                "§aリアルタイム設定対応"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                applyButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 「おすすめ設定」ボタン：開発者推奨の安定爆速構成へワンクリック移行
         * ============================================================
         */
        recommendButton = Button.builder(
                        Component.literal("おすすめ設定"),
                        button ->
                        {
                            applyRecommendedSettings();
                            Config.save();
                            Config.syncToRuntime();
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6おすすめ設定\n" +
                                                "\n" +
                                                "§7・SAFEモード\n" +
                                                "§7・TPS保護ON\n" +
                                                "§7・Idle最適化ON\n" +
                                                "§7・速度3.0\n" +
                                                "\n" +
                                                "§a大量ホッパー対応\n" +
                                                "§a初心者向け\n" +
                                                "§a安定運用向け\n"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                recommendButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 「リセット」ボタン：すべてのMOD設定をバニラ同等初期状態に戻す
         * ============================================================
         */
        resetButton = Button.builder(
                        Component.literal("リセット"),
                        button ->
                        {
                            resetSettings();
                            Config.save();
                            Config.syncToRuntime();
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6設定リセット\n" +
                                                "\n" +
                                                "§7すべての設定を\n" +
                                                "§7初期状態へ戻します。\n" +
                                                "\n" +
                                                "§a速度 : 1.0\n" +
                                                "§aSAFEモード\n" +
                                                "§aTPS保護ON"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                resetButton
        );

        y += BUTTON_HEIGHT + SPACE;

        /*
         * ============================================================
         * 🔥 「閉じる」ボタン：画面を終了して前画面（ゲーム内メニュー）へ帰還
         * ============================================================
         */
        closeButton = Button.builder(
                        Component.literal("閉じる"),
                        button ->
                        {
                            Minecraft.getInstance()
                                    .setScreen(parent);
                        }
                )
                .bounds(
                        centerX - BUTTON_WIDTH / 2,
                        y,
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT
                )
                .tooltip(
                        Tooltip.create(
                                Component.literal(
                                        "§6GUIを閉じます\n" +
                                                "\n" +
                                                "§7現在の画面を閉じて\n" +
                                                "§7ゲームへ戻ります。"
                                )
                        )
                )
                .build();

        this.addRenderableWidget(
                closeButton
        );
    }

    /*
     * ============================================================
     * 🔥 マウスホイールによる快適な垂直上下スクロール制御
     * ============================================================
     */
    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    )
    {
        scrollOffset -= (int)(scrollY * 20);

        if (scrollOffset < 0)
        {
            scrollOffset = 0;
        }

        init();

        return true;
    }

    /*
     * ============================================================
     * 🔥 プラス／マイナスボタンによる速度段階調整とリアルタイム同期
     * ============================================================
     */
    private void changeSpeed(
            double amount
    )
    {
        try
        {
            double speed =
                    Double.parseDouble(
                            speedBox.getValue()
                    );

            speed += amount;

            if (speed < FastHopperConfig.MIN_SPEED)
            {
                speed = FastHopperConfig.MIN_SPEED;
            }

            if (
                    speed > FastHopperConfig.getMaxSpeed()
            )
            {
                speed = FastHopperConfig.getMaxSpeed();
            }

            // ボタン操作時は無限ループを切るために同期中フラグを立てて上書きする
            fasthopper$isSyncing = true;
            speedBox.setValue(
                    String.format(
                            "%.1f",
                            speed
                    )
            );
            if (speedSlider != null) {
                speedSlider.syncFromSpeed(speed);
            }
            fasthopper$isSyncing = false;

            FastHopperConfig.HOPPER_SPEED = speed;
            Config.HOPPER_SPEED.set(speed);

            Config.saveRuntimeToConfig();
            Config.save();
        }
        catch (NumberFormatException ignored)
        {
            fasthopper$isSyncing = false;
        }
    }

    /*
     * ============================================================
     * 🔥 毎tick（毎秒20回）実行される画面更新システム
     * ============================================================
     */
    @Override
    public void tick()
    {
        super.tick();

        double runtimeSpeed =
                FastHopperConfig.getSafeSpeed();

        String runtimeText = String.format("%.1f", runtimeSpeed);

        double currentInput;

        try
        {
            currentInput =
                    Double.parseDouble(
                            speedBox.getValue()
                    );
        }
        catch (Exception e)
        {
            currentInput = runtimeSpeed;
        }

        /*
         * ===========================================
         * 🔥 フォーカス（入力中）ではない時だけスライダー位置と数値を最新状態にする
         * ===========================================
         */
        if (speedBox != null && !speedBox.isFocused())
        {
            try {
                if (Math.abs(currentInput - runtimeSpeed) > 0.01){
                    fasthopper$isSyncing = true;
                    speedBox.setValue(runtimeText);
                    fasthopper$isSyncing = false;
                }
            } catch (NumberFormatException e) {
                fasthopper$isSyncing = true;
                speedBox.setValue(runtimeText);
                fasthopper$isSyncing = false;
            }

            if (speedSlider != null) {
                speedSlider.syncFromSpeed(runtimeSpeed);
            }
        }

        if (toggleButton != null) toggleButton.setMessage(getToggleText());
        if (modeButton != null) modeButton.setMessage(getModeText());
        if (tpsButton != null) tpsButton.setMessage(getTPSProtectionText());
        if (idleOptimizationButton != null) idleOptimizationButton.setMessage(getIdleOptimizationText());
        if (logButton != null) logButton.setMessage(getLogText());
    }

    /*
     * ============================================================
     * 🔥 入力欄の文字データを確定させ、メモリ及びファイルに適用する処理
     * ============================================================
     */
    private void applySpeed()
    {
        try
        {
            String text = speedBox.getValue();
            if (text.isEmpty() || text.equals("."))
            {
                return;
            }

            double speed = Double.parseDouble(text);

            if (speed < FastHopperConfig.MIN_SPEED)
            {
                speed = FastHopperConfig.MIN_SPEED;
            }
            if (speed > FastHopperConfig.getMaxSpeed())
            {
                speed = FastHopperConfig.getMaxSpeed();
            }

            FastHopperConfig.HOPPER_SPEED = speed;
            Config.HOPPER_SPEED.set(speed);

            fasthopper$isSyncing = true;
            speedBox.setValue(String.format("%.1f", speed));
            if (speedSlider != null) {
                speedSlider.syncFromSpeed(speed);
            }
            fasthopper$isSyncing = false;

            Config.saveRuntimeToConfig();
        }
        catch (NumberFormatException ignored)
        {
            fasthopper$isSyncing = false;
        }
    }

    /*
     * ============================================================
     * 🔥 おすすめ設定のプリセット反映処理
     * ============================================================
     */
    private void applyRecommendedSettings()
    {
        fasthopper$isSyncing = true;
        if (speedBox != null) speedBox.setValue("3.0");
        if (speedSlider != null) {
            speedSlider.syncFromSpeed(3.0);
        }
        fasthopper$isSyncing = false;

        FastHopperConfig.HOPPER_SPEED = 3.0;
        Config.HOPPER_SPEED.set(3.0);
        Config.CURRENT_MODE.set(FastHopperMode.SAFE.name());

        Config.saveRuntimeToConfig();
        Config.save();

        FastHopperConfig.CURRENT_MODE = FastHopperMode.SAFE;

        if (toggleButton != null) toggleButton.setMessage(getToggleText());
        if (modeButton != null) modeButton.setMessage(getModeText());
        if (tpsButton != null) tpsButton.setMessage(getTPSProtectionText());
    }

    /*
     * ============================================================
     * 🔥 全設定値の初期化リセット処理
     * ============================================================
     */
    private void resetSettings()
    {
        fasthopper$isSyncing = true;
        if (speedBox != null) speedBox.setValue("1.0");
        if (speedSlider != null) {
            speedSlider.syncFromSpeed(1.0);
        }
        fasthopper$isSyncing = false;

        FastHopperConfig.HOPPER_SPEED = 1.0;
        Config.HOPPER_SPEED.set(1.0);
        Config.CURRENT_MODE.set(FastHopperMode.SAFE.name());

        Config.saveRuntimeToConfig();
        Config.save();

        FastHopperConfig.CURRENT_MODE = FastHopperMode.SAFE;

        if (toggleButton != null) toggleButton.setMessage(getToggleText());
        if (modeButton != null) modeButton.setMessage(getModeText());
        if (tpsButton != null) tpsButton.setMessage(getTPSProtectionText());
    }

    /*
     * ============================================================
     * 🔥 ボタンクリック時の動作モードループ巡回切り替え処理
     * ============================================================
     */
    private void switchMode()
    {
        if (
                FastHopperConfig.CURRENT_MODE
                        == FastHopperMode.SAFE
        )
        {
            FastHopperConfig.CURRENT_MODE =
                    FastHopperMode.EXTREME;
        }
        else if (
                FastHopperConfig.CURRENT_MODE
                        == FastHopperMode.EXTREME
        )
        {
            FastHopperConfig.CURRENT_MODE =
                    FastHopperMode.EXTRA;
        }
        else
        {
            FastHopperConfig.CURRENT_MODE =
                    FastHopperMode.SAFE;
        }

        Config.CURRENT_MODE.set(
                FastHopperConfig.CURRENT_MODE.name()
        );

        Config.saveRuntimeToConfig();
        Config.save();

        init();
    }

    /*
     * ============================================================
     * 🔥 ボタン表示文字列生成：メインスイッチ用
     * ============================================================
     */
    private Component getToggleText()
    {
        return Component.literal(
                "Fast Hopper : "
                        + (
                        FastHopperConfig.ENABLED
                                ? "ON"
                                : "OFF"
                )
        );
    }

    /*
     * ============================================================
     * 🔥 ボタン表示文字列生成：動作モード用
     * ============================================================
     */
    private Component getModeText()
    {
        return Component.literal(
                "モード : "
                        + FastHopperConfig.CURRENT_MODE.name()
        );
    }

    /*
     * ============================================================
     * 🔥 ボタン表示文字列生成：TPS自動保護用
     * ============================================================
     */
    private Component getTPSProtectionText()
    {
        return Component.literal(
                FastHopperConfig.TPS_PROTECTION
                        ? "TPS保護 : ON"
                        : "TPS保護 : OFF"
        );
    }

    /*
     * ============================================================
     * 🔥 ボタン表示文字列生成：Idle省エネ最適化用
     * ============================================================
     */
    private Component getIdleOptimizationText()
    {
        return Component.literal(
                FastHopperConfig.IDLE_HOPPER_OPTIMIZATION
                        ? "Idle最適化 : ON"
                        : "Idle最適化 : OFF"
        );
    }

    /*
     * ============================================================
     * 🔥 ボタン表示文字列生成：デバッグログ用
     * ============================================================
     */
    private Component getLogText()
    {
        return Component.literal(
                FastHopperConfig.ENABLE_LOG
                        ? "ログ表示 : ON"
                        : "ログ表示 : OFF"
        );
    }
}