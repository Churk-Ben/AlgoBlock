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
