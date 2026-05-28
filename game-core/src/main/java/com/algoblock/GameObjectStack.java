package com.algoblock;

import com.algoblock.Structure.Abstract;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class GameObjectStack {
    // 游戏对象栈：(结构类型, 对象名称) -> 具体对象实例
    private final Map<String, Abstract> objects = new HashMap<>();
    
    // 缓冲区，独立于继承体系，只存int
    private final Queue<Integer> buffer = new LinkedList<>();
    
    // 缓冲指针与默认指令
    private String bufferStruct;
    private String bufferName;
    private String bufferInstIn;
    private String bufferInstOut;

    // 循环打破条件变量（执行范式6的次数，检测通过的次数）
    private int runCheckCount = 0;
    private int passedCheckCount = 0;

    // 生成联合主键
    private String generateKey(String struct, String name) {
        return struct + "_" + name;
    }

    public void putObject(String struct, String name, Abstract obj) {
        objects.put(generateKey(struct, name), obj);
    }

    public Abstract getObject(String struct, String name) {
        return objects.get(generateKey(struct, name));
    }

    public void removeObject(String struct, String name) {
        objects.remove(generateKey(struct, name));
    }

    public void setBufferConfig(String struct, String name, String instIn, String instOut) {
        this.bufferStruct = struct;
        this.bufferName = name;
        this.bufferInstIn = instIn;
        this.bufferInstOut = instOut;
    }

    public boolean isBufferTarget(String struct, String name) {
        return struct.equals(bufferStruct) && name.equals(bufferName);
    }

    public void pushToBuffer(int value) {
        buffer.offer(value);
    }

    public Integer popFromBuffer() {
        return buffer.poll();
    }

    public void clearBuffer() {
        buffer.clear();
    }

    // 暴露默认指令字符串，供Core解析执行
    public String getBufferInstIn() { return bufferInstIn; }
    public String getBufferInstOut() { return bufferInstOut; }

    // 重置与修改校验变量
    public void resetCheckCounts() {
        this.runCheckCount = 0;
        this.passedCheckCount = 0;
    }

    public void incrementRunCheck() { this.runCheckCount++; }
    public void incrementPassedCheck() { this.passedCheckCount++; }
    
    public boolean isWinConditionMet() {
        return runCheckCount > 0 && runCheckCount == passedCheckCount;
    }
}