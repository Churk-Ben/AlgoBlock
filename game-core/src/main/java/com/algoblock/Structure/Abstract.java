package com.algoblock.Structure;

import com.algoblock.GameObjectStack;
import com.algoblock.Core;
import java.util.Map;

public abstract class Abstract {
    protected String name;
    
    // 供Core反射实例化及复制使用
    public Abstract() {}

    /**
     * 引擎要求每个结构提供自己的正则注册表 (Instruction ID -> Regex)
     */
    public abstract Map<String, String> getRegexPatterns();

    /**
     * 结构端分发器：执行对应的操作，并修改GameObjectStack。
     * @param instId 命令ID
     * @param fullCommand 完整正则匹配的字符串
     * @param stack 游戏对象栈引用
     * @param core 引擎Core引用，用于回调Buffer指令
     */
    public abstract void executeInstruction(String instId, String fullCommand, GameObjectStack stack, Core core);
}