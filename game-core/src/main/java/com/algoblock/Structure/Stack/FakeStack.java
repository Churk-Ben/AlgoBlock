package com.algoblock.Structure.Stack;

import com.algoblock.GameObjectStack;
import com.algoblock.Core;
import com.algoblock.Structure.Abstract;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakeStack extends Abstract {
    // 强制使用原生数组模拟栈结构
    private int[] array;
    private int top;
    private static final int INITIAL_CAPACITY = 16;
    
    private static final String TYPE_ID = "Stack";

    public FakeStack() {
        this.array = new int[INITIAL_CAPACITY];
        this.top = -1; // -1表示空栈
    }

    private void ensureCapacity() {
        if (top == array.length - 1) {
            int[] newArray = new int[array.length * 2];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }
    }

    private void pushElement(int val) {
        ensureCapacity();
        array[++top] = val;
    }

    private int popElement() {
        if (top == -1) throw new IllegalStateException("Stack is empty");
        return array[top--];
    }

    @Override
    public Map<String, String> getRegexPatterns() {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("init_full", "^Stack\\(([a-zA-Z0-9_]+),\\(([\\d,]*)\\)\\)$");
        patterns.put("init_empty", "^Stack\\(([a-zA-Z0-9_]+)\\)$");
        patterns.put("copy", "^Stack\\(([a-zA-Z0-9_]+)\\)\\.copy\\(([a-zA-Z0-9_]+)\\)$");
        patterns.put("delete", "^Stack\\(([a-zA-Z0-9_]+)\\)\\.delete$");
        patterns.put("pop", "^Stack\\(([a-zA-Z0-9_]+)\\)\\.pop$");
        patterns.put("push", "^Stack\\(([a-zA-Z0-9_]+)\\)\\.push$");
        patterns.put("equal", "^Stack\\.equal\\(([a-zA-Z0-9_]+),([a-zA-Z0-9_]+)\\)$"); 
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
                FakeStack newObj = new FakeStack();
                newObj.name = objName;
                if (!values.isEmpty()) {
                    for (String v : values.split(",")) {
                        newObj.pushElement(Integer.parseInt(v));
                    }
                }
                gameObjectStack.putObject(TYPE_ID, objName, newObj);
                break;
            }
            case "init_empty": {
                String objName = m.group(1);
                FakeStack newObj = new FakeStack();
                newObj.name = objName;
                gameObjectStack.putObject(TYPE_ID, objName, newObj);
                break;
            }
            case "copy": {
                String srcName = m.group(1);
                String destName = m.group(2);
                FakeStack srcObj = (FakeStack) gameObjectStack.getObject(TYPE_ID, srcName);
                if (srcObj != null) {
                    FakeStack newObj = new FakeStack();
                    newObj.name = destName;
                    newObj.array = new int[srcObj.array.length];
                    System.arraycopy(srcObj.array, 0, newObj.array, 0, srcObj.array.length);
                    newObj.top = srcObj.top;
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
                FakeStack obj = (FakeStack) gameObjectStack.getObject(TYPE_ID, objName);
                if (obj != null && obj.top > -1) {
                    int val = obj.popElement();
                    gameObjectStack.pushToBuffer(val);
                    if (!gameObjectStack.isBufferTarget(TYPE_ID, objName)) {
                        core.triggerEngineCommand(gameObjectStack.getBufferInstIn());
                    }
                }
                break;
            }
            case "push": {
                String objName = m.group(1);
                FakeStack obj = (FakeStack) gameObjectStack.getObject(TYPE_ID, objName);
                if (obj != null) {
                    if (!gameObjectStack.isBufferTarget(TYPE_ID, objName)) {
                        core.triggerEngineCommand(gameObjectStack.getBufferInstOut());
                    }
                    Integer val = gameObjectStack.popFromBuffer();
                    if (val != null) {
                        obj.pushElement(val);
                    }
                }
                break;
            }
            case "equal": {
                String nameA = m.group(1);
                String nameB = m.group(2);
                FakeStack objA = (FakeStack) gameObjectStack.getObject(TYPE_ID, nameA);
                FakeStack objB = (FakeStack) gameObjectStack.getObject(TYPE_ID, nameB);
                
                gameObjectStack.incrementRunCheck();
                if (objA != null && objB != null && objA.top == objB.top) {
                    boolean isEqual = true;
                    // 从栈底到栈顶逐一校验
                    for (int i = 0; i <= objA.top; i++) {
                        if (objA.array[i] != objB.array[i]) {
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