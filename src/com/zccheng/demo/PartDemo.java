package com.zccheng.demo;

import java.util.ArrayList;
import java.util.List;

public class PartDemo {

    public static List<List<Item>> groupItems(List<Item> items, int maxGroupSize) {
        List<List<Item>> result = new ArrayList<>();
        List<Item> currentGroup = new ArrayList<>();
        int currentSum = 0;

        // 先处理所有Item，将大的Item拆分成小的
        List<Item> processedItems = new ArrayList<>();
        for (Item item : items) {
            int itemSize = item.getValues().size();
            if (itemSize <= maxGroupSize) {
                processedItems.add(item);
            } else {
                // 拆分大的Item
                List<String> values = item.getValues();
                for (int i = 0; i < values.size(); i += maxGroupSize) {
                    int end = Math.min(i + maxGroupSize, values.size());
                    List<String> subValues = values.subList(i, end);
                    processedItems.add(new Item(new ArrayList<>(subValues)));
                }
            }
        }

        // 现在所有Item的size都不超过maxGroupSize，进行分组
        for (int i = 0; i < processedItems.size(); i++) {
            Item item = processedItems.get(i);
            int itemSize = item.getValues().size();

            // 如果当前item能放入当前组
            if (currentSum + itemSize <= maxGroupSize) {
                currentGroup.add(item);
                currentSum += itemSize;
            } else {
                // 当前组已满，看看是否能从后面找小的item来补满当前组
                boolean foundReplacement = false;

                // 从后面查找能放入当前组的小item
                for (int j = i + 1; j < processedItems.size(); j++) {
                    Item candidate = processedItems.get(j);
                    int candidateSize = candidate.getValues().size();

                    if (currentSum + candidateSize <= maxGroupSize) {
                        // 找到能放入的小item，交换位置
                        currentGroup.add(candidate);
                        currentSum += candidateSize;
                        processedItems.remove(j);
                        processedItems.add(i, item); // 把当前item放回原位置
                        foundReplacement = true;
                        break;
                    }
                }

                if (foundReplacement) {
                    // 继续处理当前item（它被放回了原位置）
                    i--;
                } else {
                    // 没找到能补满的小item，结束当前组
                    result.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                    currentGroup.add(item);
                    currentSum = itemSize;
                }
            }
        }

        // 添加最后一组
        if (!currentGroup.isEmpty()) {
            result.add(currentGroup);
        }

        return result;
    }

    // 更简单的版本：先拆分大Item，然后贪心分组
    public static List<List<Item>> groupItemsSimple(List<Item> items, int maxGroupSize) {
        List<List<Item>> result = new ArrayList<>();
        List<Item> processedItems = new ArrayList<>();

        // 1. 拆分大的Item
        for (Item item : items) {
            int itemSize = item.getValues().size();
            if (itemSize <= maxGroupSize) {
                processedItems.add(item);
            } else {
                List<String> values = item.getValues();
                for (int i = 0; i < values.size(); i += maxGroupSize) {
                    int end = Math.min(i + maxGroupSize, values.size());
                    List<String> subValues = values.subList(i, end);
                    processedItems.add(new Item(item.getItemCode(),new ArrayList<>(subValues)));
                }
            }
        }

        // 2. 贪心分组
        List<Item> currentGroup = new ArrayList<>();
        int currentSum = 0;

        for (Item item : processedItems) {
            int itemSize = item.getValues().size();

            if (currentSum + itemSize <= maxGroupSize) {
                currentGroup.add(item);
                currentSum += itemSize;
            } else {
                result.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(item);
                currentSum = itemSize;
            }
        }

        if (!currentGroup.isEmpty()) {
            result.add(currentGroup);
        }

        return result;
    }

    // 测试代码
    public static void main(String[] args) {
        List<Item> items = new ArrayList<>();

        items.add(buildItem("aa",95));
        items.add(buildItem("bb",20));
        items.add(buildItem("cc",28));

        List<List<Item>> lists = groupItemsSimple(items, 30);
        System.out.println();
    }

    private static Item buildItem(String itemCode, int size) {
        Item item = new Item();
        item.setItemCode(itemCode);
        List<String> itemValues = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            itemValues.add(item.getItemCode() + i);
        }
        item.setValues(itemValues);

        return item;
    }

    static class Item {
        private String itemCode;
        private List<String> values;

        public Item() {
        }

        public Item(String itemCode, List<String> values) {
            this.itemCode = itemCode;
            this.values = values;
        }

        public String getItemCode() {
            return itemCode;
        }

        public void setItemCode(String itemCode) {
            this.itemCode = itemCode;
        }

        public Item(List<String> values) {
            this.values = values;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        @Override
        public String toString() {
            return "Item{values.size=" + values.size() + "}";
        }
    }
}

