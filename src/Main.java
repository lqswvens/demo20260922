/**
 * Demo 项目入口：先打印欢迎语，再按顺序输出 1~{@value #MAX_COUNT} 的计数。
 */
public class Main {

    /** 计数上限。 */
    private static final int MAX_COUNT = 5;

    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        for (int i = 1; i <= MAX_COUNT; i++) {
            System.out.println("i = " + i);
        }
    }
}
