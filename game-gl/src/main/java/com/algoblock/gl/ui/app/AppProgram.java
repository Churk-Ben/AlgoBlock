package com.algoblock.gl.ui.app;

import com.algoblock.gl.renderer.core.RenderFrame;
import com.algoblock.gl.renderer.core.TerminalBuffer;
import com.algoblock.gl.ui.pages.GamePage;
import com.algoblock.gl.ui.pages.StartPage;
import com.algoblock.gl.ui.pages.diagnostics.DiagnosticsPage;
import com.algoblock.gl.ui.tea.Program;
import com.algoblock.gl.ui.tea.UpdateResult;
import com.algoblock.core.levels.Level;

import java.util.ArrayList;
import java.util.List;

public class AppProgram implements Program<AppModel, AppMsg, AppCmd> {

    private final StartPage startPage;
    private final GamePage gamePage;
    private final DiagnosticsPage diagnosticsPage;
    private final Level initialLevel;

    public AppProgram(StartPage startPage, GamePage gamePage, DiagnosticsPage diagnosticsPage, Level initialLevel) {
        this.startPage = startPage;
        this.gamePage = gamePage;
        this.diagnosticsPage = diagnosticsPage;
        this.initialLevel = initialLevel;
    }

    @Override
    public AppModel init() {
        return AppModel.init(initialLevel, System.currentTimeMillis() / 1000);
    }

    @Override
    public UpdateResult<AppModel, AppCmd> update(AppModel model, AppMsg msg) {
        if (model.screen() == AppModel.Screen.START) {
            StartPage.Msg startMsg = null;
            if (msg instanceof AppMsg.Intent im) {
                startMsg = new StartPage.Msg.Intent(im.intent());
            }

            if (startMsg != null) {
                UpdateResult<StartPage.Model, StartPage.Cmd> result = startPage.update(model.startModel(), startMsg);
                List<AppCmd> commands = new ArrayList<>();
                AppModel.Screen nextScreen = model.screen();
                GamePage.Model nextGameModel = model.gameModel();

                if (result.commands() != null) {
                    for (StartPage.Cmd cmd : result.commands()) {
                        if (cmd instanceof StartPage.Cmd.StartGame) {
                            nextScreen = AppModel.Screen.GAME;
                            // reset game model start time
                            nextGameModel = GamePage.Model.init(model.currentLevel(),
                                    System.currentTimeMillis() / 1000);
                        } else if (cmd instanceof StartPage.Cmd.OpenDiagnostics) {
                            nextScreen = AppModel.Screen.DIAGNOSTICS;
                        }
                    }
                }
                AppModel nextModel = new AppModel(nextScreen, result.model(), nextGameModel, model.diagnosticsModel(),
                        model.currentLevel());
                return new UpdateResult<>(nextModel, commands);
            }
            return new UpdateResult<>(model, List.of());
        }

        if (model.screen() == AppModel.Screen.GAME) {
            GamePage.Msg gameMsg = null;
            if (msg instanceof AppMsg.Intent im) {
                gameMsg = new GamePage.Msg.Intent(im.intent());
            } else if (msg instanceof AppMsg.SubmitFinished sf) {
                gameMsg = new GamePage.Msg.SubmitFinished(sf.result());
            }

            if (gameMsg != null) {
                UpdateResult<GamePage.Model, GamePage.Cmd> result = gamePage.update(model.gameModel(), gameMsg);
                List<AppCmd> commands = new ArrayList<>();
                if (result.commands() != null) {
                    for (GamePage.Cmd cmd : result.commands()) {
                        if (cmd instanceof GamePage.Cmd.Submit submit) {
                            commands.add(new AppCmd.Submit(submit.level(), submit.source(), submit.elapsedSeconds()));
                        } else if (cmd instanceof GamePage.Cmd.PlaySound playSound) {
                            commands.add(new AppCmd.PlaySound(playSound.resourcePath()));
                        }
                    }
                }
                AppModel nextModel = new AppModel(model.screen(), model.startModel(), result.model(),
                        model.diagnosticsModel(), model.currentLevel());
                return new UpdateResult<>(nextModel, commands);
            }
            return new UpdateResult<>(model, List.of());
        }

        if (model.screen() == AppModel.Screen.DIAGNOSTICS) {
            DiagnosticsPage.Msg diagMsg = null;
            if (msg instanceof AppMsg.Intent im) {
                diagMsg = new DiagnosticsPage.Msg.Intent(im.intent());
            }

            if (diagMsg != null) {
                UpdateResult<DiagnosticsPage.Model, DiagnosticsPage.Cmd> result = diagnosticsPage
                        .update(model.diagnosticsModel(), diagMsg);
                List<AppCmd> commands = new ArrayList<>();
                AppModel.Screen nextScreen = model.screen();

                if (result.commands() != null) {
                    for (DiagnosticsPage.Cmd cmd : result.commands()) {
                        if (cmd instanceof DiagnosticsPage.Cmd.ReturnToStart) {
                            nextScreen = AppModel.Screen.START;
                        }
                    }
                }
                AppModel nextModel = new AppModel(nextScreen, model.startModel(), model.gameModel(), result.model(),
                        model.currentLevel());
                return new UpdateResult<>(nextModel, commands);
            }
            return new UpdateResult<>(model, List.of());
        }

        return new UpdateResult<>(model, List.of());
    }

    @Override
    public RenderFrame view(AppModel model, TerminalBuffer buffer, long nowMillis) {
        buffer.clear();
        if (model.screen() == AppModel.Screen.START) {
            return startPage.view(model.startModel(), buffer, nowMillis);
        } else if (model.screen() == AppModel.Screen.GAME) {
            return gamePage.view(model.gameModel(), buffer, nowMillis);
        } else {
            return diagnosticsPage.view(model.diagnosticsModel(), buffer, nowMillis);
        }
    }
}
