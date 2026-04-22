package com.algoblock.gl.ui.app;

import com.algoblock.core.engine.GameCoreService;
import com.algoblock.core.engine.SubmissionResult;
import com.algoblock.gl.ui.tea.CmdHandler;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javazoom.jl.player.Player;

public class AppCmdHandler implements CmdHandler<AppCmd, AppMsg> {
    private final GameCoreService service;
    private final ExecutorService commandExecutor;
    private final ExecutorService audioExecutor;

    public AppCmdHandler(GameCoreService service) {
        this.service = service;
        this.commandExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ui-command-executor");
            t.setDaemon(true);
            return t;
        });
        this.audioExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "ui-audio-executor");
            t.setDaemon(true);
            return t;
        });

        // 预热音频系统，防止首次播放音频时因懒加载(如枚举音频设备)导致的卡顿
        this.audioExecutor.submit(() -> {
            try {
                javax.sound.sampled.AudioSystem.getMixerInfo();
                Class.forName("javazoom.jl.player.Player");
                // 尝试加载一个短音效并实例化 Player 以触发内部的类加载和 JavaSound 初始化
                try (InputStream is = getClass().getResourceAsStream("/assets/audio/sfx/interact.mp3")) {
                    if (is != null) {
                        Player dummy = new Player(is);
                        dummy.close();
                    }
                }
            } catch (Throwable t) {
                // ignore
            }
        });
    }

    @Override
    public void handle(AppCmd cmd, Consumer<AppMsg> dispatch) {
        if (cmd instanceof AppCmd.Submit submit) {
            commandExecutor.submit(() -> {
                SubmissionResult result = service.submit(submit.level(), submit.source(), submit.elapsedSeconds());
                dispatch.accept(new AppMsg.SubmitFinished(result));
            });
        } else if (cmd instanceof AppCmd.Exit) {
            System.exit(0);
        } else if (cmd instanceof AppCmd.PlaySound playSound) {
            audioExecutor.submit(() -> {
                try (InputStream is = getClass().getResourceAsStream(playSound.resourcePath())) {
                    if (is != null) {
                        Player player = new Player(is);
                        player.play();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to play sound: " + playSound.resourcePath());
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void close() {
        commandExecutor.shutdownNow();
        audioExecutor.shutdownNow();
    }
}
