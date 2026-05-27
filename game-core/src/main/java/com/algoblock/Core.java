package com.algoblock;

import com.algoblock.Structure.Abstract;
import java.util.*;
import java.util.regex.Matcher;

public class Core {
    private final GameObjectStack stack = new GameObjectStack();
    private final List<InstructionDefinition> registeredInstructions = new ArrayList<>();
    private final Map<String, Abstract> structureTemplates = new HashMap<>();
    
    // 关卡数据（模拟JSON解析结果）
    private List<String> structUsed;
    private Map<String, Integer> instsAllowed;
    private List<String> initInsts;
    private List<String> judgeInsts;
    private int stepsLimit;
    
    public void loadLevelConfig() {
        // [硬编码模拟JSON解析]: 001.json
        structUsed = Arrays.asList("Queue", "Stack");
        
        instsAllowed = new HashMap<>();
        instsAllowed.put("Queue_pop", 6);
        instsAllowed.put("Queue_add", 6);
        
        initInsts = Arrays.asList("Queue(A,(1,2,3,4))", "Stack(B)");
        judgeInsts = Arrays.asList(
            "Stack(B).copy(ABCDEFG_ABCDEFG_A)",
            "Stack(ABCDEFG_ABCDEFG_B,(1,3,4,2))",
            "Stack.equal(ABCDEFG_ABCDEFG_A,ABCDEFG_ABCDEFG_B)",
            "Stack(ABCDEFG_ABCDEFG_A).delete",
            "Stack(ABCDEFG_ABCDEFG_B).delete"
        );
        
        stack.setBufferConfig("Stack", "B", "Stack(B).push", "Stack(B).pop");
        stepsLimit = 6;
        
        System.out.println("[加载] 关卡JSON解析完成，准备注册结构。");
    }

    public void registerStructures() {
        // [硬编码模拟registry.json映射关系] -> 使用反射机制实例化
        Map<String, String> registry = new HashMap<>();
        registry.put("Queue", "com.algoblock.Structure.Queue.FakeQueue");
        registry.put("Stack", "com.algoblock.Structure.Stack.FakeStack");
        
        for (String struct : structUsed) {
            String fqcn = registry.get(struct);
            try {
                // 核心解耦：根据FQCN动态加载具体的结构类
                Abstract structInstance = (Abstract) Class.forName(fqcn).getDeclaredConstructor().newInstance();
                structureTemplates.put(struct, structInstance);
                
                // 收集每个结构的正则表达式，注册到Core
                Map<String, String> regexMap = structInstance.getRegexPatterns();
                for (Map.Entry<String, String> entry : regexMap.entrySet()) {
                    registeredInstructions.add(new InstructionDefinition(struct, entry.getKey(), entry.getValue()));
                }
                System.out.println("[注册] 成功加载结构: " + struct);
            } catch (Exception e) {
                System.err.println("[错误] 反射加载失败: " + fqcn);
            }
        }
    }

    public void initAllowedLimits() {
        for (InstructionDefinition def : registeredInstructions) {
            String key = def.getStructId() + "_" + def.getInstId();
            if (instsAllowed.containsKey(key)) {
                def.setMaxUses(instsAllowed.get(key));
            }
        }
    }

    private boolean executeStatement(String statement, boolean isPlayerAction) {
        for (InstructionDefinition def : registeredInstructions) {
            Matcher m = def.getRegex().matcher(statement);
            if (m.matches()) {
                if (isPlayerAction) {
                    // 第3步：检查该语句是否可执行
                    if (def.getMaxUses() > 0 && def.getUsedCount() >= def.getMaxUses()) {
                        System.out.println("[拦截] 结构" + def.getStructId() + "的指令" + def.getInstId() + "已达到使用上限");
                        return false;
                    }
                    def.incrementUsedCount();
                }
                
                // 第4步：执行这个语句，路由分发到具体的Abstract子类
                Abstract template = structureTemplates.get(def.getStructId());
                template.executeInstruction(def.getInstId(), statement, stack, this);
                
                System.out.println(String.format("[执行] %s-%s-语句:%s-说明:完成%s操作", 
                    def.getStructId(), def.getInstId(), statement, def.getInstId()));
                return true;
            }
        }
        return false;
    }

    // 供子类在需要触发默认输入/输出时回调执行命令
    public void triggerEngineCommand(String statement) {
        executeStatement(statement, false); // 内部触发不消耗玩家次数
    }

    public void run() {
        loadLevelConfig();
        registerStructures();
        initAllowedLimits();

        System.out.println("\n--- 初始化阶段 ---");
        for (String initInst : initInsts) {
            executeStatement(initInst, false);
        }

        System.out.println("\n--- 进入关卡主循环 ---");
        Scanner scanner = new Scanner(System.in);
        int currentStep = 0;

        while (currentStep < stepsLimit) {
            System.out.println("\n[步骤1] 归零游戏对象栈的两个变量");
            stack.resetCheckCounts();

            System.out.println("[步骤2] 等待一个语句输入 (请输入指令，或输入 'exit' 退出测试):");
            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) break;

            System.out.println("[步骤3] 检查该语句是否可执行");
            boolean executed = executeStatement(input, true);
            if (!executed) continue; 
            
            System.out.println("[步骤5] 执行完后对judge_insts里的语句顺序逐一执行");
            for (String judge : judgeInsts) {
                executeStatement(judge, false);
            }

            System.out.println("[步骤6] 清空游戏对象栈的缓冲区");
            stack.clearBuffer();

            System.out.println("[步骤7] 判断是否退出大循环");
            if (stack.isWinConditionMet()) {
                System.out.println("[过关] 判定条件通过！游戏胜利！");
                break;
            } else {
                System.out.println("[循环] 判定未通过，继续下一轮。当前消耗步数: " + (currentStep + 1));
            }
            currentStep++;
        }
        
        if (currentStep >= stepsLimit) {
            System.out.println("[失败] 达到最大步数限制，游戏结束。");
        }
        scanner.close();
    }
}