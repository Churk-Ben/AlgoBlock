package com.algoblock;

import java.util.regex.Pattern;

/**
 * 封装单条指令的定义，包括结构ID、指令ID、正则表达式以及使用限制状态。
 */
public class InstructionDefinition {
    private final String structId;
    private final String instId;
    private final Pattern regex;
    private int usedCount;
    private int maxUses;

    public InstructionDefinition(String structId, String instId, String regexStr) {
        this.structId = structId;
        this.instId = instId;
        this.regex = Pattern.compile(regexStr);
        this.usedCount = 0;
        this.maxUses = 0; // 默认0，代表不受玩家限制或尚未初始化
    }

    public String getStructId() { return structId; }
    public String getInstId() { return instId; }
    public Pattern getRegex() { return regex; }
    
    public int getUsedCount() { return usedCount; }
    public void incrementUsedCount() { this.usedCount++; }
    
    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }

    public void resetUses() { this.usedCount = 0; }
}