/**
 * Demo 项目入口：先打印欢迎语，再按顺序输出 1~{@value #MAX_COUNT} 的计数。
 *
 * <p>Codex 桌面端改动标记：2026-09-22，用于验证 IDE 能否同步外部改动（已验证可删除）。
 */
public class Main {

    /** 计数上限。 */
    private static final int MAX_COUNT = 5;

    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
        printCount();
    }

    /** 按顺序打印 1~{@value #MAX_COUNT} 的计数。 */
    private static void printCount() {
        for (int i = 1; i <= MAX_COUNT; i++) {
            System.out.println("i = " + i);
        }
    }
}
