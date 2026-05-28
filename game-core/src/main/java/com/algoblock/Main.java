package com.algoblock;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World! Engine Starting...");
        Core core = new Core();
        
        // 传入需要加载的关卡配置文件路径 (本代码中由 loadLevelConfig 内部写死演示数据)
        // 进入关卡主循环
        core.run();
    }
}