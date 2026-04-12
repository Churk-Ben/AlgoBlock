package com.algoblock.core.engine;

import com.algoblock.core.levels.Level;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelRules {
    private static final Pattern IDENT = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public boolean usesOnlyAvailableBlocks(String expr, Level level) {
        Set<String> allowed = new HashSet<>(level.availableBlocks());
        Matcher matcher = IDENT.matcher(expr);
        while (matcher.find()) {
            String token = matcher.group();
            if (Character.isDigit(token.charAt(0))) {
                continue;
            }
            if (!allowed.contains(token)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsForcedBlocks(String expr, Level level) {
        for (String forced : level.forcedBlocks()) {
            if (!expr.contains(forced)) {
                return false;
            }
        }
        return true;
    }
}
