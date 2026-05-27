package com.algoblock.Structure.Queue;

import com.algoblock.GameObjectStack;
import com.algoblock.Core;
import com.algoblock.Structure.Abstract;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeQueue extends Abstract {
    // 强制使用原生数组模拟队列（环形数组结构）
    private int[] array;
    private int head;
    private int tail;
    private int size;
    private static final int INITIAL_CAPACITY = 16;
    
    // 对外暴露的结构标识符依然是Queue，保持与JSON兼容
    private static final String TYPE_ID = "Queue";

    public FakeQueue() {
        this.array = new int[INITIAL_CAPACITY];
        this.head = 0;
        this.tail = 0;
        this.size = 0;
    }

    // 内部方法：动态扩容
    private void ensureCapacity() {
        if (size == array.length) {
            int[] newArray = new int[array.length * 2];
            // 将环形数组展开为线性数组
            for (int i = 0; i < size; i++) {
                newArray[i] = array[(head + i) % array.length];
            }
            array = newArray;
            head = 0;
            tail = size;
        }
    }

    // 内部方法：入队
    private void enqueue(int val) {
        ensureCapacity();
        array[tail] = val;
        tail = (tail + 1) % array.length;
        size++;
    }

    // 内部方法：出队
    private int dequeue() {
        if (size == 0) throw new IllegalStateException("Queue is empty");
        int val = array[head];
        head = (head + 1) % array.length;
        size--;
        return val;
    }

    @Override
    public Map<String, String> getRegexPatterns() {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("init_full", "^Queue\\(([a-zA-Z0-9_]+),\\(([\\d,]*)\\)\\)$");
        patterns.put("init_empty", "^Queue\\(([a-zA-Z0-9_]+)\\)$");
        patterns.put("copy", "^Queue\\(([a-zA-Z0-9_]+)\\)\\.copy\\(([a-zA-Z0-9_]+)\\)$");
        patterns.put("delete", "^Queue\\(([a-zA-Z0-9_]+)\\)\\.delete$");
        patterns.put("pop", "^Queue\\(([a-zA-Z0-9_]+)\\)\\.pop$");
        patterns.put("add", "^Queue\\(([a-zA-Z0-9_]+)\\)\\.add$");
        // 补充关于equal的指令id和注册
        patterns.put("equal", "^Queue\\.equal\\(([a-zA-Z0-9_]+),([a-zA-Z0-9_]+)\\)$");
        return patterns;
    }

    @Override
    public void executeInstruction(String instId, String fullCommand, GameObjectStack gameObjectStack, Core core) {
        Pattern p = Pattern.compile(getRegexPatterns().get(instId));
        Matcher m = p.matcher(fullCommand);
        if (!m.matches()) return;

        switch (instId) {
            case "init_full": {
                String objName = m.group(1);
                String values = m.group(2);
                FakeQueue newObj = new FakeQueue();
                newObj.name = objName;
                if (!values.isEmpty()) {
                    for (String v : values.split(",")) {
                        newObj.enqueue(Integer.parseInt(v));
                    }
                }
                gameObjectStack.putObject(TYPE_ID, objName, newObj);
                break;
            }
            case "init_empty": {
                String objName = m.group(1);
                FakeQueue newObj = new FakeQueue();
                newObj.name = objName;
                gameObjectStack.putObject(TYPE_ID, objName, newObj);
                break;
            }
            case "copy": {
                String srcName = m.group(1);
                String destName = m.group(2);
                FakeQueue srcObj = (FakeQueue) gameObjectStack.getObject(TYPE_ID, srcName);
                if (srcObj != null) {
                    FakeQueue newObj = new FakeQueue();
                    newObj.name = destName;
                    // 数组结构的深拷贝
                    newObj.array = new int[srcObj.array.length];
                    System.arraycopy(srcObj.array, 0, newObj.array, 0, srcObj.array.length);
                    newObj.head = srcObj.head;
                    newObj.tail = srcObj.tail;
                    newObj.size = srcObj.size;
                    gameObjectStack.putObject(TYPE_ID, destName, newObj);
                }
                break;
            }
            case "delete": {
                String objName = m.group(1);
                gameObjectStack.removeObject(TYPE_ID, objName);
                break;
            }
            case "pop": {
                String objName = m.group(1);
                FakeQueue obj = (FakeQueue) gameObjectStack.getObject(TYPE_ID, objName);
                if (obj != null) {
                    if (gameObjectStack.isBufferTarget(TYPE_ID, objName)) return; 
                    if (obj.size > 0) {
                        gameObjectStack.pushToBuffer(obj.dequeue());
                        core.triggerEngineCommand(gameObjectStack.getBufferInstIn()); 
                    }
                }
                break;
            }
            case "add": {
                String objName = m.group(1);
                FakeQueue obj = (FakeQueue) gameObjectStack.getObject(TYPE_ID, objName);
                if (obj != null) {
                    if (gameObjectStack.isBufferTarget(TYPE_ID, objName)) return; 
                    core.triggerEngineCommand(gameObjectStack.getBufferInstOut()); 
                    Integer val = gameObjectStack.popFromBuffer();
                    if (val != null) {
                        obj.enqueue(val);
                    }
                }
                break;
            }
            case "equal": {
                String nameA = m.group(1);
                String nameB = m.group(2);
                FakeQueue objA = (FakeQueue) gameObjectStack.getObject(TYPE_ID, nameA);
                FakeQueue objB = (FakeQueue) gameObjectStack.getObject(TYPE_ID, nameB);
                
                gameObjectStack.incrementRunCheck();
                if (objA != null && objB != null && objA.size == objB.size) {
                    boolean isEqual = true;
                    // 按队列顺序逐个对比数组元素
                    for (int i = 0; i < objA.size; i++) {
                        int indexA = (objA.head + i) % objA.array.length;
                        int indexB = (objB.head + i) % objB.array.length;
                        if (objA.array[indexA] != objB.array[indexB]) {
                            isEqual = false;
                            break;
                        }
                    }
                    if (isEqual) {
                        gameObjectStack.incrementPassedCheck();
                    }
                }
                break;
            }
        }
    }
}