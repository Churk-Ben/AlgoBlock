package com.algoblock.core.engine;

import com.algoblock.api.Block;
import com.algoblock.api.BlockMeta;
import com.algoblock.api.UnaryBlock;
import com.algoblock.api.BinaryBlock;
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

    public boolean containsForcedBlocks(Block<?> root, Level level) {
        Set<String> found = new HashSet<>();
        collectBlockNames(root, found);
        for (String forced : level.forcedBlocks()) {
            if (!found.contains(forced)) {
                return false;
            }
        }
        return true;
    }

    private void collectBlockNames(Block<?> block, Set<String> found) {
        if (block == null) return;
        
        BlockMeta meta = block.getClass().getAnnotation(BlockMeta.class);
        if (meta != null) {
            found.add(meta.name());
        }
        
        if (block instanceof UnaryBlock<?, ?> unary) {
            collectBlockNames(unary.child(), found);
        } else if (block instanceof BinaryBlock<?, ?, ?> binary) {
            collectBlockNames(binary.left(), found);
            collectBlockNames(binary.right(), found);
        }
    }
}
