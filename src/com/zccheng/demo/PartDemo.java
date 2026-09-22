package com.zccheng.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 把一批 {@link Item} 按容量（{@code maxGroupSize}）重新打包成若干组。
 *
 * <p>打包分两步，每步都是一个独立的小方法：
 * <ol>
 *   <li>{@link #splitOversizedItems(List, int)}：单元数超过容量的 Item 先按容量切片，保证每个片段都放得下；</li>
 *   <li>{@link #groupItems(List, int)} / {@link #groupItemsSimple(List, int)}：把片段顺序装进分组，保证每组单元数不超过容量。</li>
 * </ol>
 *
 * <p>两种装箱策略的区别：
 * <ul>
 *   <li>{@code groupItems}：顺序装箱 + 回填，当前组装不下时用后面能放下的小片段补满，减少碎片；</li>
 *   <li>{@code groupItemsSimple}：纯顺序贪心装箱，实现最简单，但容易出现装不满的组。</li>
 * </ul>
 */
public final class PartDemo {

    private PartDemo() {
    }

    // ------------------------------------------------------------------
    // 对外方法
    // ------------------------------------------------------------------

    /**
     * 顺序装箱 + 回填：每个分组先放入第一个还没被使用过的片段，再用后面的小片段尽量补满。
     * 组与组之间保持原有先后顺序，同一个片段只会出现一次。
     */
    public static List<List<Item>> groupItems(List<Item> items, int maxGroupSize) {
        List<Item> parts = splitOversizedItems(items, maxGroupSize);
        boolean[] used = new boolean[parts.size()];
        List<List<Item>> groups = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            if (used[i]) {
                continue;
            }

            used[i] = true;
            List<Item> group = new ArrayList<>();
            group.add(parts.get(i));
            int freeCapacity = maxGroupSize - parts.get(i).size();

            for (int j = i + 1; j < parts.size() && freeCapacity > 0; j++) {
                Item candidate = parts.get(j);
                if (!used[j] && candidate.size() <= freeCapacity) {
                    used[j] = true;
                    group.add(candidate);
                    freeCapacity -= candidate.size();
                }
            }

            groups.add(group);
        }

        return groups;
    }

    /**
     * 纯顺序贪心装箱：按顺序把片段放进当前组，放不下就开新组。
     */
    public static List<List<Item>> groupItemsSimple(List<Item> items, int maxGroupSize) {
        List<Item> parts = splitOversizedItems(items, maxGroupSize);
        List<List<Item>> groups = new ArrayList<>();
        List<Item> currentGroup = new ArrayList<>();
        int currentSize = 0;

        for (Item part : parts) {
            if (currentSize + part.size() > maxGroupSize) {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentSize = 0;
            }
            currentGroup.add(part);
            currentSize += part.size();
        }

        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }

        return groups;
    }

    /**
     * 拆分超大 Item：单元数不超过容量的保持原样，超过的按容量切片。
     *
     * @return 拆分后的片段列表，每个片段的单元数都不超过 {@code maxGroupSize}
     */
    public static List<Item> splitOversizedItems(List<Item> items, int maxGroupSize) {
        requirePositiveMaxGroupSize(maxGroupSize);
        Objects.requireNonNull(items, "items 不能为 null");

        List<Item> parts = new ArrayList<>();
        for (Item item : items) {
            Objects.requireNonNull(item, "items 中不能出现 null 元素");
            split(item, maxGroupSize, parts);
        }
        return parts;
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    private static void split(Item item, int maxGroupSize, List<Item> target) {
        int itemSize = item.size();
        if (itemSize <= maxGroupSize) {
            target.add(item);
            return;
        }

        List<String> values = item.getValues();
        for (int from = 0; from < itemSize; from += maxGroupSize) {
            int to = Math.min(from + maxGroupSize, itemSize);
            // 切片时保留 itemCode，便于回溯数据来自哪个 Item
            target.add(new Item(item.getItemCode(), new ArrayList<>(values.subList(from, to))));
        }
    }

    private static void requirePositiveMaxGroupSize(int maxGroupSize) {
        if (maxGroupSize <= 0) {
            throw new IllegalArgumentException("maxGroupSize 必须大于 0，当前值：" + maxGroupSize);
        }
    }

    private static int totalSize(List<Item> group) {
        int total = 0;
        for (Item item : group) {
            total += item.size();
        }
        return total;
    }

    // ------------------------------------------------------------------
    // 示例 / 自测入口
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        List<Item> items = new ArrayList<>();
        items.add(buildItem("aa", 95));
        items.add(buildItem("bb", 20));
        items.add(buildItem("cc", 28));

        int maxGroupSize = 30;
        printGroups("groupItems（顺序装箱 + 回填）", groupItems(items, maxGroupSize), maxGroupSize);
        printGroups("groupItemsSimple（纯顺序贪心）", groupItemsSimple(items, maxGroupSize), maxGroupSize);
    }

    private static void printGroups(String title, List<List<Item>> groups, int maxGroupSize) {
        System.out.println(title + "，maxGroupSize=" + maxGroupSize);
        for (int i = 0; i < groups.size(); i++) {
            List<Item> group = groups.get(i);
            System.out.println("  第 " + (i + 1) + " 组（" + totalSize(group) + "/" + maxGroupSize + "）：" + group);
        }
        System.out.println("  共 " + groups.size() + " 组");
    }

    private static Item buildItem(String itemCode, int size) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            values.add(itemCode + i);
        }
        return new Item(itemCode, values);
    }

    /**
     * 待分组的元素集合：{@code values} 是真正参与容量计算的单元，{@code itemCode} 只用于标识来源。
     */
    public static class Item {

        private String itemCode;
        private List<String> values = new ArrayList<>();

        public Item() {
        }

        public Item(List<String> values) {
            this(null, values);
        }

        public Item(String itemCode, List<String> values) {
            this.itemCode = itemCode;
            setValues(values);
        }

        /** 该 Item 占用的容量，即单元个数。 */
        public int size() {
            return values == null ? 0 : values.size();
        }

        public String getItemCode() {
            return itemCode;
        }

        public void setItemCode(String itemCode) {
            this.itemCode = itemCode;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        @Override
        public String toString() {
            return "Item{" + itemCode + ", size=" + size() + "}";
        }
    }
}
