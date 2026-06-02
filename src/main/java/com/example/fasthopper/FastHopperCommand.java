package com.example.fasthopper;

import com.example.fasthopper.gui.FastHopperConfigScreen;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;

import net.minecraft.commands.Commands;

import net.minecraft.network.chat.Component;

import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.RegisterCommandsEvent;

/*
 * ============================================================
 * 🔥 FastHopperCommand
 * ============================================================
 *
 * 🎯 このクラスがやること
 * ・Fast Hopperコマンド登録🔥
 * ・GUI起動🔥
 * ・速度変更・モード自動昇格同期🔥
 * ・mode変更・大文字小文字バグ解消🔥
 * ・TAB補完🔥
 * ・help表示🔥
 *
 * ❌ やらないこと
 * ・Mixin処理
 * ・NBT変更
 * ・ホッパー搬送処理
 *
 * 💡 一言
 * 👉「完全バグ修正版・Fast Hopperコマンド本体🔥」
 * ============================================================
 */
@SuppressWarnings("all")
@EventBusSubscriber
public class FastHopperCommand
{
    /*
     * ============================================================
     * 🔥 mode TAB補完
     * ============================================================
     */
    private static CompletableFuture<Suggestions> suggestModes(
            CommandContext<?> context,
            SuggestionsBuilder builder
    )
    {
        builder.suggest("safe");
        builder.suggest("extreme");
        builder.suggest("extra");

        return builder.buildFuture();
    }

    /*
     * ============================================================
     * 🔥 コマンド登録
     * ============================================================
     */
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(

                Commands.literal("fasthopper")

                        /*
                         * ============================================================
                         * 🔥 GUIオープンコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("gui")
                                        .executes(context ->
                                        {
                                            Minecraft.getInstance().setScreen(
                                                    new FastHopperConfigScreen(
                                                            Minecraft.getInstance().screen
                                                    )
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Fast Hopper設定画面を開きました"
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                        )

                        /*
                         * ============================================================
                         * 🔥 speedコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("speed")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "現在のホッパー速度: "
                                                                    + FastHopperConfig.getSafeSpeed()
                                                                    + "x"
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })

                                        .then(
                                                Commands.argument(
                                                                "value",
                                                                DoubleArgumentType.doubleArg(0.1, 128.0)
                                                        )
                                                        .executes(context ->
                                                        {
                                                            double speed = DoubleArgumentType.getDouble(context, "value");

                                                            // 🚨バグ修正：高い速度が入力されたら、切り捨てるのではなく、
                                                            // 自動的に対応する上位の高速モードへ格上げ・追従させる
                                                            if (speed > 64.0) {
                                                                FastHopperConfig.CURRENT_MODE = FastHopperMode.EXTRA;
                                                            } else if (speed > 16.0) {
                                                                if (FastHopperConfig.CURRENT_MODE == FastHopperMode.SAFE) {
                                                                    FastHopperConfig.CURRENT_MODE = FastHopperMode.EXTREME;
                                                                }
                                                            }

                                                            /*
                                                             * 🔥 速度適用 & 永続化保存
                                                             * 順序を「モード確定後」にすることで、Configへの即時保存を安全化
                                                             */
                                                            FastHopperConfig.setSpeed(speed);
                                                            Config.HOPPER_SPEED.set(speed);
                                                            Config.CURRENT_MODE.set(FastHopperConfig.CURRENT_MODE.name());
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            // 実際の安全確定後の速度を取得してログの矛盾を消滅させる
                                                            double actualSpeed = FastHopperConfig.getSafeSpeed();

                                                            String level = "§a標準";
                                                            String slowLog = "";

                                                            if (actualSpeed < 1.0) {
                                                                int skipTicks = (int)Math.ceil(1.0 / actualSpeed);
                                                                slowLog = " §7[" + skipTicks + "tick搬送]";
                                                            }

                                                            if (actualSpeed <= 0.1) level = "§1凍結";
                                                            else if (actualSpeed <= 0.2) level = "§3超低速";
                                                            else if (actualSpeed <= 0.5) level = "§7半速";
                                                            else if (actualSpeed <= 1.0) level = "§a標準";
                                                            else if (actualSpeed <= 2.0) level = "§2搬送強化";
                                                            else if (actualSpeed <= 3.0) level = "§e処理強化";
                                                            else if (actualSpeed <= 6.0) level = "§6限界高速";
                                                            else if (actualSpeed <= 8.0) level = "§6限界動作";
                                                            else if (actualSpeed <= 16.0) level = "§c危険高速";
                                                            else if (actualSpeed <= 32.0) level = "§4極限処理";
                                                            else if (actualSpeed <= 64.0) level = "§4限界超過";
                                                            else level = "§7実験動作";

                                                            final String finalLevel = level;
                                                            final String finalSlowLog = slowLog;

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ ホッパー速度変更\n" +
                                                                                    "\n" +
                                                                                    "§7設定速度 : §e" + actualSpeed + "x\n" +
                                                                                    "§7現在モード : §6" + FastHopperConfig.CURRENT_MODE.name() + "\n" +
                                                                                    "§7状態 : " + finalLevel + "\n" +
                                                                                    (actualSpeed < 8.0
                                                                                            ? "§7平均搬送 : §b" + String.format("%.2f", 8.0 / actualSpeed) + " tick / 搬送\n"
                                                                                            : "§7平均搬送 : §d" + String.format("%.2f", actualSpeed / 8.0) + " 搬送 / tick\n")
                                                                                    + finalSlowLog + "\n§7固定高速ではなく progress平均搬送方式"
                                                                    ),
                                                                    true
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 idleコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("idle")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e===== Idle Hopper =====\n" +
                                                                    "\n§7現在状態 : " + (FastHopperConfig.IDLE_HOPPER_OPTIMIZATION ? "§aON" : "§cOFF") +
                                                                    "\n\n§7空ホッパー省エネ最適化\n\n§7/fasthopper idle <on|off>"
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                Commands.literal("on")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.IDLE_HOPPER_OPTIMIZATION = true;
                                                            Config.IDLE_HOPPER_OPTIMIZATION.set(true);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ Idle Hopper最適化 : ON\n" +
                                                                                    "\n" +
                                                                                    "§7空ホッパーを省エネ化します。\n" +
                                                                                    "§7大量ホッパー環境でTPS改善が期待できます。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("off")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.IDLE_HOPPER_OPTIMIZATION = false;
                                                            Config.IDLE_HOPPER_OPTIMIZATION.set(false);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§c✔ Idle Hopper最適化 : OFF\n" +
                                                                                    "\n" +
                                                                                    "§7空ホッパー最適化を無効化しました。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 tpsコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("tps")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e===== TPS Protection =====\n" +
                                                                    "\n§7現在状態 : " + (FastHopperConfig.TPS_PROTECTION ? "§aON" : "§cOFF") +
                                                                    "\n\n§7TPS低下時の自動保護\n\n§7/fasthopper tps <on|off>"
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                Commands.literal("on")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.TPS_PROTECTION = true;
                                                            Config.TPS_PROTECTION.set(true);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ TPS保護 : ON\n" +
                                                                                    "\n" +
                                                                                    "§7TPS低下時に\n" +
                                                                                    "§7ホッパー負荷を自動調整します。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("off")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.TPS_PROTECTION = false;
                                                            Config.TPS_PROTECTION.set(false);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§c✔ TPS保護 : OFF\n" +
                                                                                    "\n" +
                                                                                    "§7TPS保護を無効化しました。\n" +
                                                                                    "§c超高速時はTPS低下に注意してください。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 logコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("log")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e===== Debug Log =====\n" +
                                                                    "\n§7現在状態 : " + (FastHopperConfig.ENABLE_LOG ? "§aON" : "§cOFF") +
                                                                    "\n\n§7保存ログ・同期ログ表示\n\n§7/fasthopper log <on|off>"
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                Commands.literal("on")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.ENABLE_LOG = true;
                                                            Config.ENABLE_LOG.set(true);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ デバッグログ : ON\n" +
                                                                                    "\n" +
                                                                                    "§7保存ログ・同期ログ・平均搬送ログを表示します。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("off")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.ENABLE_LOG = false;
                                                            Config.ENABLE_LOG.set(false);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§c✔ デバッグログ : OFF\n" +
                                                                                    "\n" +
                                                                                    "§7Fast Hopperログ表示を停止しました。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 toggleコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("toggle")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e===== Fast Hopper =====\n" +
                                                                    "\n§7現在状態 : " + (FastHopperConfig.ENABLED ? "§aON" : "§cOFF") +
                                                                    "\n\n§7/fasthopper toggle <on|off>"
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                Commands.literal("on")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.ENABLED = true;
                                                            Config.ENABLED.set(true);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ Fast Hopper : ON\n" +
                                                                                    "\n" +
                                                                                    "§7ホッパー高速化を有効化しました。\n" +
                                                                                    "§7現在モード : §6" + FastHopperConfig.CURRENT_MODE.name()
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                Commands.literal("off")
                                                        .executes(context ->
                                                        {
                                                            FastHopperConfig.ENABLED = false;
                                                            Config.ENABLED.set(false);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§c✔ Fast Hopper : OFF\n" +
                                                                                    "\n" +
                                                                                    "§7ホッパー動作をバニラ仕様へ戻しました。"
                                                                    ),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 statsコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("stats")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(() -> Component.literal("§e===== Fast Hopper Stats ====="), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§e現在速度: §a" + FastHopperConfig.getSafeSpeed() + "x"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§e最小倍率: §b" + FastHopperConfig.MIN_SPEED + "x"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§e最大倍率: §c" + FastHopperConfig.getMaxSpeed() + "x"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§eモード: §6" + FastHopperConfig.CURRENT_MODE.name()), false);

                                            double speed = FastHopperConfig.getSafeSpeed();

                                            if (FastHopperConfig.CURRENT_MODE == FastHopperMode.EXTRA)
                                            {
                                                if (speed < 8.0)
                                                {
                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "§e理論搬送: §b"
                                                                            + String.format("%.2f", 8.0 / speed)
                                                                            + " tick / 搬送"
                                                            ),
                                                            false
                                                    );
                                                }
                                                else
                                                {
                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "§e理論搬送: §d約 "
                                                                            + String.format("%.2f", speed / 8.0)
                                                                            + " 搬送 / tick"
                                                            ),
                                                            false
                                                    );
                                                }
                                            }
                                            else
                                            {
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal(
                                                                "§e理論搬送: §a1tick限界"
                                                        ),
                                                        false
                                                );
                                            }

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§7SAFE=安全重視 / EXTREME=1tick限界 / EXTRA=平均搬送"
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§7実測: 0.1x=4分12秒  1x=25.33秒  128x=0.14秒"
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e状態: "
                                                                    + (FastHopperConfig.ENABLED ? "§aON" : "§cOFF")
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§eTPS保護: "
                                                                    + (FastHopperConfig.TPS_PROTECTION ? "§aON" : "§cOFF")
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§eIdle最適化: "
                                                                    + (FastHopperConfig.IDLE_HOPPER_OPTIMIZATION ? "§aON" : "§cOFF")
                                                    ),
                                                    false
                                            );

                                            context.getSource().sendSuccess(() -> Component.literal("§6=============================="), false);

                                            return 1;
                                        })
                        )

                        /*
                         * ============================================================
                         * 🔥 resetコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("reset")
                                        .executes(context ->
                                        {
                                            FastHopperConfig.reset();
                                            Config.saveRuntimeToConfig();
                                            Config.save();

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§a✔ Fast Hopper設定初期化\n" +
                                                                    "\n§7全設定をデフォルト状態へ戻しました。"
                                                    ),
                                                    true
                                            );
                                            return 1;
                                        })
                        )

                        /*
                         * ============================================================
                         * 🔥 modeコマンド (大文字小文字バグ、および丸め込みを完全修正)
                         * ============================================================
                         */
                        .then(
                                Commands.literal("mode")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "§e===== Fast Hopper Mode =====\n" +
                                                                    "\n§7現在モード : §6" + FastHopperConfig.CURRENT_MODE.name() +
                                                                    "\n\n§7/fasthopper mode <safe|extreme|extra>"
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })
                                        .then(
                                                Commands.argument("mode", StringArgumentType.word())
                                                        .suggests(FastHopperCommand::suggestModes)
                                                        .executes(context ->
                                                        {
                                                            String modeName = StringArgumentType.getString(context, "mode").toUpperCase();
                                                            FastHopperMode mode;

                                                            try {
                                                                mode = FastHopperMode.valueOf(modeName);
                                                            } catch (IllegalArgumentException e) {
                                                                context.getSource().sendFailure(Component.literal("§cエラー: 無効なモード名です。"));
                                                                return 0;
                                                            }

                                                            /*
                                                             * 🚨 バグ修正：モードが格下げ（例：EXTRA➔SAFE）されたとき、
                                                             * 現在の速度が上限を超えていたら、Config保存でバグを起こさないよう
                                                             * 新しいモードの最大上限値へ安全に丸め込む
                                                             */
                                                            FastHopperConfig.CURRENT_MODE = mode;
                                                            if (FastHopperConfig.HOPPER_SPEED > FastHopperConfig.getMaxSpeed()) {
                                                                FastHopperConfig.HOPPER_SPEED = FastHopperConfig.getMaxSpeed();
                                                            }

                                                            // Configデータへの完全同期保存
                                                            Config.CURRENT_MODE.set(mode.name());
                                                            Config.HOPPER_SPEED.set(FastHopperConfig.HOPPER_SPEED);
                                                            Config.saveRuntimeToConfig();
                                                            Config.save();

                                                            final FastHopperMode finalMode = mode;
                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "§a✔ Fast Hopperモード変更\n" +
                                                                                    "\n" +
                                                                                    "§7現在モード : §6" + finalMode.name() + "\n" +
                                                                                    "\n" +
                                                                                    (finalMode == FastHopperMode.SAFE
                                                                                            ? "§aTPS保護重視\n§7最大速度 : §e16x"
                                                                                            : (finalMode == FastHopperMode.EXTREME
                                                                                               ? "§c超高速搬送モード\n§7最大速度 : §e64x"
                                                                                               : "§6真の超高速・複数搬送モード\n§7最大速度 : §e128x"))
                                                                    ),
                                                                    true
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )

                        /*
                         * ============================================================
                         * 🔥 helpコマンド
                         * ============================================================
                         */
                        .then(
                                Commands.literal("help")
                                        .executes(context ->
                                        {
                                            context.getSource().sendSuccess(() -> Component.literal("§6=============================="), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§e⚡ Fast Hopper Command Help ⚡"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§6=============================="), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§a/fasthopper gui §7▶ 設定GUI画面を開きます"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§a/fasthopper speed <値> §7▶ 搬送速度を変更します (自動モード昇格対応)"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§a/fasthopper mode <種類> §7▶ 動作モードを変更します (SAFE/EXTREME/EXTRA)"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§a/fasthopper stats §7▶ 現在の稼働ステータスを詳細表示します"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§a/fasthopper reset §7▶ 全設定を初期値にリセットします"), false);
                                            context.getSource().sendSuccess(() -> Component.literal("§6=============================="), false);
                                            return 1;
                                        })
                        )
        );
    }
}