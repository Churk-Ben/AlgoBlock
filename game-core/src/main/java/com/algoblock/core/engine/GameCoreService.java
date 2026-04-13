package com.algoblock.core.engine;

import com.algoblock.api.Block;
import com.algoblock.api.EvalContext;
import com.algoblock.core.levels.Level;
import java.util.List;

public class GameCoreService {
    private final Parser parser;
    private final Judge judge;
    private final Scorer scorer;
    private final LevelRules levelRules;

    public GameCoreService(BlockRegistry registry) {
        this.parser = new Parser(registry);
        this.judge = new Judge();
        this.scorer = new Scorer();
        this.levelRules = new LevelRules();
    }

    public SubmissionResult submit(Level level, String expr, long elapsedSeconds) {
        if (!levelRules.usesOnlyAvailableBlocks(expr, level)) {
            return new SubmissionResult(false, new ScoreResult(false, false, false, 0), List.of(), null, "使用了未开放积木");
        }
        
        Block<?> root;
        try {
            root = parser.parse(expr);
        } catch (Exception e) {
            return new SubmissionResult(false, new ScoreResult(false, false, false, 0), List.of(), null, "解析失败: " + e.getMessage());
        }

        if (!levelRules.containsForcedBlocks(root, level)) {
            return new SubmissionResult(false, new ScoreResult(false, false, false, 0), List.of(), null, "缺少必须积木");
        }
        
        EvalContext ctx = new EvalContext(level.input(), level.stepBudget());
        Object result;
        try {
            result = root.evaluate(ctx);
        } catch (Exception e) {
            return new SubmissionResult(false, new ScoreResult(false, false, false, 0), ctx.trace(), null, "运行错误: " + e.getMessage());
        }
        
        boolean correct = judge.check(result, level.output());
        ScoreResult score = scorer.score(correct, root, elapsedSeconds, level);
        return new SubmissionResult(correct, score, ctx.trace(), result, correct ? "AC" : "WA");
    }
}
