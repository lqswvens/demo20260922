package com.zccheng.demo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 集合工具类：判空、null 归一、安全取值、去重过滤、分组转换、切分与集合运算。
 *
 * <p>设计取舍：
 * <ul>
 *   <li>final + 私有构造 + 全静态方法，与 {@link StringUtils}、{@link DateUtils} 保持一致，
 *       不持有任何状态，天然线程安全；</li>
 *   <li>只有 {@code isEmpty} / {@code isNotEmpty} 判断系列允许传 null，其余方法一律要求集合非 null
 *       （需要兼容 null 时先用 {@link #emptyIfNull(Collection)} 归一），
 *       这样「空集合」与「null 集合」不会被静默当成同一件事；</li>
 *   <li>所有方法都不修改调用方传入的集合，返回的永远是新的集合对象，
 *       调用方可以放心继续持有并使用原集合；</li>
 *   <li>只封装「Stream 写法啰嗦」或「{@code Collectors} 默认行为容易踩坑」的操作，
 *       {@code java.util.Collections} 已经提供的能力（sort、binarySearch、shuffle 等）不重复造。</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>{@code
 * if (CollectionUtils.isEmpty(orderIds)) {
 *     return Collections.emptyList();
 * }
 * List<String> names = CollectionUtils.map(users, User::getName);
 * Map<String, List<User>> byCity = CollectionUtils.groupBy(users, User::getCity);
 * List<List<Integer>> batches = CollectionUtils.partition(ids, 500);
 * }</pre>
 *
 * <p>本类为工具类，不可实例化。
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    // ------------------------------------------------------------------
    // 空值判断
    // ------------------------------------------------------------------

    /**
     * 判断集合是否为 {@code null} 或没有任何元素。
     *
     * <p>相比直接调用 {@code collection.isEmpty()}，这里顺带处理了 null，
     * 省掉调用方到处写的 {@code collection != null &&} 前缀；
     * 反过来，只有本系列方法允许传 null，其它方法收到 null 会立刻抛异常，让问题留在调用现场。
     *
     * <p>注意：传 {@code null} 字面量时编译器无法从重载里挑出唯一一个方法，
     * 需要显式指定类型，例如 {@code isEmpty((Collection<?>) null)}。
     *
     * @param collection 待判断的集合，允许为 null
     * @return 为 null 或没有任何元素时返回 true
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否非空（非 null 且至少有一个元素）。
     *
     * @param collection 待判断的集合，允许为 null
     * @return 非 null 且至少有一个元素时返回 true
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断 Map 是否为 {@code null} 或没有任何键值对。
     *
     * <p>为什么不用 {@code !map.isEmpty()}：Web 接口里「查不到」与「字段没传」都表现为 null，
     * 用这个方法可以把两种情况的判断收敛成一行。
     *
     * @param map 待判断的 Map，允许为 null
     * @return 为 null 或没有任何键值对时返回 true
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断 Map 是否非空（非 null 且至少有一个键值对）。
     *
     * @param map 待判断的 Map，允许为 null
     * @return 非 null 且至少有一个键值对时返回 true
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断数组是否为 {@code null} 或长度为 0。
     *
     * <p>只接收 {@code Object[]}：基本类型数组（{@code int[]} 等）不能强转成它，
     * 那类数组请直接写 {@code array == null || array.length == 0}，
     * 或者用 {@link java.lang.reflect.Array#getLength(Object)} 做通用处理。
     *
     * @param array 待判断的数组，允许为 null
     * @return 为 null 或长度为 0 时返回 true
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 判断数组是否非空（非 null 且长度大于 0）。
     *
     * @param array 待判断的数组，允许为 null
     * @return 非 null 且长度大于 0 时返回 true
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    // ------------------------------------------------------------------
    // null 归一与默认值
    // ------------------------------------------------------------------

    /**
     * 把可能为 {@code null} 的集合归一成「空集合」，便于后续无脑遍历。
     *
     * <p>返回的是 {@link Collections#emptyList()} 这个全局共享的不可变实例：
     * 不额外分配对象，但也不能往里 {@code add}（会抛 {@link UnsupportedOperationException}）。
     * 需要可写副本时写成 {@code new ArrayList<>(emptyIfNull(collection))}。
     *
     * @param collection 待归一的集合，允许为 null
     * @param <T>        元素类型
     * @return 原集合；为 null 时返回共享的不可变空集合
     */
    public static <T> Collection<T> emptyIfNull(Collection<T> collection) {
        return collection == null ? Collections.<T>emptyList() : collection;
    }

    /**
     * 把可能为 {@code null} 的 Map 归一成「空 Map」，语义与 {@link #emptyIfNull(Collection)} 相同。
     *
     * @param map 待归一的 Map，允许为 null
     * @param <K> key 类型
     * @param <V> value 类型
     * @return 原 Map；为 null 时返回共享的不可变空 Map
     */
    public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
        return map == null ? Collections.<K, V>emptyMap() : map;
    }

    /**
     * 列表为 {@code null} 或没有元素时返回默认列表，否则原样返回传入的列表（不做拷贝）。
     *
     * <p>「原样返回」是有意为之：调用方拿回的往往就是自己传进去的那个对象，省一次拷贝；
     * 代价是「命中默认值」与「命中原值」两条分支返回的对象不同，
     * 若调用方打算修改返回值，请自己先包一层 {@code new ArrayList<>}。
     *
     * @param list        待检查的列表，允许为 null
     * @param defaultList 兜底列表，不能为 null
     * @param <T>         元素类型
     * @return 非空的 list，或 defaultList
     * @throws NullPointerException 默认列表为 null 时抛出
     */
    public static <T> List<T> defaultIfEmpty(List<T> list, List<T> defaultList) {
        Objects.requireNonNull(defaultList, "参数 defaultList 不能为 null");
        return isEmpty(list) ? defaultList : list;
    }

    // ------------------------------------------------------------------
    // 安全取值
    // ------------------------------------------------------------------

    /**
     * 取集合的第一个元素；集合为空时返回 {@code null}。
     *
     * <p>返回 null 有两种可能：集合本来就是空的，或第一个元素本身就是 null。
     * 需要区分时先用 {@link #isEmpty(Collection)} 判断集合是否为空。
     *
     * @param collection 来源集合，不能为 null
     * @param <T>        元素类型
     * @return 第一个元素；集合为空时返回 null
     * @throws NullPointerException 集合为 null 时抛出
     */
    public static <T> T firstElement(Collection<T> collection) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 取列表的最后一个元素；列表为空时返回 {@code null}。
     *
     * <p>为什么这里的入参是 {@link List} 而不是 {@link Collection}：
     * {@code Collection} 只能从头往后遍历，想取末尾元素就得把整个集合走一遍；
     * 而 {@code List} 支持随机访问，{@code get(size - 1)} 是常数时间，
     * 签名上就明确告诉调用方「只有列表才能高效地取尾巴」。
     *
     * @param list 来源列表，不能为 null
     * @param <T>  元素类型
     * @return 最后一个元素；列表为空时返回 null
     * @throws NullPointerException 列表为 null 时抛出
     */
    public static <T> T lastElement(List<T> list) {
        Objects.requireNonNull(list, "参数 list 不能为 null");
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * 按下标安全取值：下标越界时返回默认值，而不是抛 {@link IndexOutOfBoundsException}。
     *
     * <p>适合「取第 N 条，没有就算了」的场景，例如取历史记录里最近一次登录；
     * 把越界交给调用方去 try-catch，代码反而更啰嗦。
     *
     * @param list         来源列表，不能为 null
     * @param index        下标，允许为负或越界
     * @param defaultValue 越界时返回的默认值，可以为 null
     * @param <T>          元素类型
     * @return 下标对应的元素，或 defaultValue
     * @throws NullPointerException 列表为 null 时抛出
     */
    public static <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        Objects.requireNonNull(list, "参数 list 不能为 null");
        if (index < 0 || index >= list.size()) {
            return defaultValue;
        }
        return list.get(index);
    }

    // ------------------------------------------------------------------
    // 创建集合
    // ------------------------------------------------------------------

    /**
     * 创建一个可变的 {@link ArrayList}，用于替代 {@code Arrays.asList(...)}。
     *
     * <p>为什么不用 {@code Arrays.asList}：它返回的是固定长度的视图，
     * 调用 {@code add} / {@code remove} 会抛 {@link UnsupportedOperationException}；
     * 而这里返回的是货真价实的 {@code ArrayList}，增删随便来。
     *
     * <p>方法上的 {@link SafeVarargs} 是对编译器的承诺：「本方法不会把 elements 数组存进集合，
     * 也不会把它外泄给调用方」——泛型数组不可具体化，没有这个注解，每次调用都会收到 unchecked 警告。
     *
     * @param elements 初始元素，允许包含 null，也可以一个都不传
     * @param <T>      元素类型
     * @return 可变的列表
     */
    @SafeVarargs
    public static <T> List<T> newArrayList(T... elements) {
        List<T> list = new ArrayList<T>(elements.length);
        // 这里刻意不用 Collections.addAll(list, elements)：把泛型数组当 varargs 实参转发出去时，
        // 编译器会再报一条 varargs 堆污染警告，逐个 add 反而更直白，也没有任何警告
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    // ------------------------------------------------------------------
    // 去重、过滤、映射
    // ------------------------------------------------------------------

    /**
     * 保序去重：返回新列表，相等的元素（按 {@link Object#equals} 判断）只保留第一次出现的那一个。
     *
     * <p>底层用 {@link LinkedHashSet}：去重靠哈希，顺序靠链表，于是结果是「原顺序 + 已去重」；
     * 换成 {@code HashSet} 顺序就不可预期了。
     *
     * @param collection 来源集合，不能为 null
     * @param <T>        元素类型
     * @return 去重后的新列表，顺序与元素首次出现的顺序一致
     * @throws NullPointerException 集合为 null 时抛出
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        return new ArrayList<T>(new LinkedHashSet<T>(collection));
    }

    /**
     * 过滤：返回满足条件的元素组成的新列表，顺序与原集合一致。
     *
     * <p>等价写法是 {@code collection.stream().filter(predicate).collect(Collectors.toList())}，
     * 两者的差别在求值时机：Stream 是惰性的，没有终止操作就什么都不会执行；
     * 本方法则是立即求值，返回时结果已经算好，之后再修改原集合也不会影响结果。
     *
     * @param collection 来源集合，不能为 null
     * @param predicate  过滤条件，不能为 null
     * @param <T>        元素类型
     * @return 满足条件的元素组成的新列表
     * @throws NullPointerException 集合或条件为 null 时抛出
     */
    public static <T> List<T> filter(Collection<? extends T> collection, Predicate<? super T> predicate) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        Objects.requireNonNull(predicate, "参数 predicate 不能为 null");
        List<T> result = new ArrayList<T>();
        for (T element : collection) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * 映射：把每个元素转换成另一种类型，返回新列表，元素个数与顺序都不变。
     *
     * <p>这里刻意不做「映射结果为 null 就丢掉」之类的隐式处理：元素个数与输入严格一致，
     * 调用方一眼就能看出结果对不对；需要丢掉某些元素时用 {@link #filter}，
     * 需要「先过滤再映射」就先调 filter，再调 map。
     *
     * @param collection 来源集合，不能为 null
     * @param mapper     转换函数，不能为 null
     * @param <T>        源元素类型
     * @param <R>        目标元素类型
     * @return 转换后的新列表
     * @throws NullPointerException 集合或转换函数为 null 时抛出
     */
    public static <T, R> List<R> map(Collection<? extends T> collection, Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        Objects.requireNonNull(mapper, "参数 mapper 不能为 null");
        List<R> result = new ArrayList<R>(collection.size());
        for (T element : collection) {
            result.add(mapper.apply(element));
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 分组与转换
    // ------------------------------------------------------------------

    /**
     * 按 key 分组：返回保序的 {@link LinkedHashMap}，value 是组内元素组成的新列表，
     * 组内元素顺序与原集合一致。
     *
     * <p>为什么不直接写 {@code Collectors.groupingBy}：
     * <ul>
     *   <li>{@code groupingBy} 默认用 {@code HashMap} 装分组，key 的顺序不可预期，
     *       想保序还得额外指定 {@code LinkedHashMap::new}，写成三参重载，读起来很别扭；</li>
     *   <li>{@code groupingBy} 内部用 {@code Map#merge} 写入，
     *       某个元素的 key 算出 null 时会直接抛 {@link NullPointerException}
     *       （{@code HashMap} 本身允许 null key，是 {@code merge} 不允许），
     *       而这里允许 null key 并把它当成一个普通分组，
     *       于是「某条数据字段没填」不会让整个分组操作失败。</li>
     * </ul>
     *
     * @param collection 来源集合，不能为 null
     * @param keyMapper  分组依据，不能为 null，允许某些元素算出 null key
     * @param <T>        元素类型
     * @param <K>        key 类型
     * @return 保序的分组结果，key 按首次出现的顺序排列
     * @throws NullPointerException 集合或分组依据为 null 时抛出
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<? extends T> collection,
                                                 Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        Objects.requireNonNull(keyMapper, "参数 keyMapper 不能为 null");
        Map<K, List<T>> result = new LinkedHashMap<K, List<T>>();
        for (T element : collection) {
            K key = keyMapper.apply(element);
            List<T> group = result.get(key);
            if (group == null) {
                group = new ArrayList<T>();
                result.put(key, group);
            }
            group.add(element);
        }
        return result;
    }

    /**
     * 把集合转成 Map：key 与 value 都由元素计算得出，结果按 key 首次出现的顺序排列。
     *
     * <p>两个容易踩的坑，这里都显式处理：
     * <ul>
     *   <li>重复 key：抛 {@link IllegalArgumentException} 并带上冲突的 key。
     *       {@code Collectors.toMap} 遇到重复 key 同样会抛异常，但它另外提供的合并重载
     *       要求调用方自己写合并策略，策略一旦写错就会静默丢数据，
     *       不如让重复直接失败，问题在写入侧就暴露出来；</li>
     *   <li>key 为 null：抛 {@link NullPointerException}。{@code HashMap} 允许 null key，
     *       但能当业务主键的值通常不该为 null，早点报错比事后在业务逻辑里排查划算。</li>
     * </ul>
     *
     * @param collection  来源集合，不能为 null
     * @param keyMapper   key 计算函数，不能为 null，结果不允许为 null
     * @param valueMapper value 计算函数，不能为 null
     * @param <T>         元素类型
     * @param <K>         key 类型
     * @param <V>         value 类型
     * @return 保序的 Map
     * @throws NullPointerException     集合、两个函数为 null，或某个元素的 key 算出 null 时抛出
     * @throws IllegalArgumentException 出现重复 key 时抛出
     */
    public static <T, K, V> Map<K, V> toMap(Collection<? extends T> collection,
                                            Function<? super T, ? extends K> keyMapper,
                                            Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(collection, "参数 collection 不能为 null");
        Objects.requireNonNull(keyMapper, "参数 keyMapper 不能为 null");
        Objects.requireNonNull(valueMapper, "参数 valueMapper 不能为 null");
        Map<K, V> result = new LinkedHashMap<K, V>();
        for (T element : collection) {
            K key = keyMapper.apply(element);
            Objects.requireNonNull(key, "元素的 key 不能为 null：" + element);
            if (result.containsKey(key)) {
                throw new IllegalArgumentException("集合中存在重复的 key：" + key);
            }
            result.put(key, valueMapper.apply(element));
        }
        return result;
    }

    // ------------------------------------------------------------------
    // 切分与分页
    // ------------------------------------------------------------------

    /**
     * 把列表按固定容量切成若干段，每段都是独立的拷贝（不是 {@code subList} 视图）。
     *
     * <p>为什么不用 {@code list.subList(from, to)}：
     * <ul>
     *   <li>它返回的是原列表的视图，原列表一改视图跟着变，
     *       而且原列表被结构性修改后，访问视图会抛 {@link java.util.ConcurrentModificationException}；</li>
     *   <li>视图内部持有原列表的引用，只用到一小段也会拖住整个大列表，无法被回收。</li>
     * </ul>
     *
     * <p>典型场景是分批处理：
     * {@code for (List<Integer> batch : partition(ids, 500)) { dao.batchInsert(batch); }}。
     *
     * @param list 来源列表，不能为 null，可以为空
     * @param size 每段容量，必须大于 0
     * @param <T>  元素类型
     * @return 分段结果，每段都是新列表；来源为空时返回空的段列表
     * @throws NullPointerException     列表为 null 时抛出
     * @throws IllegalArgumentException 每段容量小于等于 0 时抛出
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        Objects.requireNonNull(list, "参数 list 不能为 null");
        if (size <= 0) {
            throw new IllegalArgumentException("每段容量必须大于 0，实际为：" + size);
        }
        // 段数是 (size 向上取整)，先按这个容量预分配，避免 ArrayList 反复扩容
        List<List<T>> result = new ArrayList<List<T>>((list.size() + size - 1) / size);
        for (int from = 0; from < list.size(); from += size) {
            int to = Math.min(from + size, list.size());
            result.add(new ArrayList<T>(list.subList(from, to)));
        }
        return result;
    }

    /**
     * 分页：页码从 1 开始，返回该页元素组成的独立列表。
     *
     * <p>页码越界（小于 1，或超过总页数）不抛异常而是返回空列表，
     * 方便直接接到「暂无数据」的分支上；总数为 0 时任何页码都返回空列表。
     *
     * @param list     来源列表，不能为 null
     * @param pageNo   页码，从 1 开始
     * @param pageSize 每页条数，必须大于 0
     * @param <T>      元素类型
     * @return 当页元素组成的新列表；页码越界时为空列表
     * @throws NullPointerException     列表为 null 时抛出
     * @throws IllegalArgumentException 每页条数小于等于 0 时抛出
     */
    public static <T> List<T> page(List<T> list, int pageNo, int pageSize) {
        Objects.requireNonNull(list, "参数 list 不能为 null");
        if (pageSize <= 0) {
            throw new IllegalArgumentException("每页条数必须大于 0，实际为：" + pageSize);
        }
        if (pageNo < 1) {
            return new ArrayList<T>();
        }
        // 用 long 计算起点：pageNo 很大时 pageNo * pageSize 会溢出 int，让越界判断失效
        long from = ((long) pageNo - 1) * pageSize;
        if (from >= list.size()) {
            return new ArrayList<T>();
        }
        // 终点同样用 long 兜住 start + pageSize 的溢出，最后才安全地收窄回 int
        long to = Math.min(from + pageSize, list.size());
        return new ArrayList<T>(list.subList((int) from, (int) to));
    }

    // ------------------------------------------------------------------
    // 集合运算
    // ------------------------------------------------------------------

    /**
     * 并集：返回去重且保序的新列表，顺序是「先 first 的全部元素，再 second 中 first 没有的元素」。
     *
     * <p>结果去重是有意为之：数学意义上的并集本身就是集合语义，
     * 用 {@link LinkedHashSet} 承载既能去重，又保住了元素首次出现的顺序。
     *
     * @param first  第一个集合，不能为 null
     * @param second 第二个集合，不能为 null
     * @param <T>    元素类型
     * @return 并集（已去重、保序）
     * @throws NullPointerException 任一集合为 null 时抛出
     */
    public static <T> List<T> union(Collection<? extends T> first, Collection<? extends T> second) {
        Objects.requireNonNull(first, "参数 first 不能为 null");
        Objects.requireNonNull(second, "参数 second 不能为 null");
        Set<T> result = new LinkedHashSet<T>(first);
        result.addAll(second);
        return new ArrayList<T>(result);
    }

    /**
     * 交集：返回同时存在于两个集合中的元素，顺序以 {@code first} 为准，结果已去重。
     *
     * @param first  第一个集合，不能为 null
     * @param second 第二个集合，不能为 null
     * @param <T>    元素类型
     * @return 交集（已去重、按 first 的顺序）
     * @throws NullPointerException 任一集合为 null 时抛出
     */
    public static <T> List<T> intersection(Collection<? extends T> first, Collection<? extends T> second) {
        Objects.requireNonNull(first, "参数 first 不能为 null");
        Objects.requireNonNull(second, "参数 second 不能为 null");
        Set<T> result = new LinkedHashSet<T>(first);
        result.retainAll(second);
        return new ArrayList<T>(result);
    }

    /**
     * 差集：返回存在于 {@code first} 但不在 {@code second} 中的元素，
     * 顺序以 {@code first} 为准，结果已去重。
     *
     * <p>注意差集不满足交换律：{@code difference(a, b)} 通常不等于 {@code difference(b, a)}，
     * 写代码时先想清楚「要谁减谁」。
     *
     * @param first  第一个集合，不能为 null
     * @param second 第二个集合，不能为 null
     * @param <T>    元素类型
     * @return 差集（已去重、按 first 的顺序）
     * @throws NullPointerException 任一集合为 null 时抛出
     */
    public static <T> List<T> difference(Collection<? extends T> first, Collection<? extends T> second) {
        Objects.requireNonNull(first, "参数 first 不能为 null");
        Objects.requireNonNull(second, "参数 second 不能为 null");
        Set<T> result = new LinkedHashSet<T>(first);
        result.removeAll(second);
        return new ArrayList<T>(result);
    }

    // ------------------------------------------------------------------
    // 内部辅助方法
    // ------------------------------------------------------------------

    /** 断言两个对象相等（含 null），失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " 失败：期望 [" + expected + "]，实际 [" + actual + "]");
        }
    }

    /** 断言两个整数相等，失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " 失败：期望 [" + expected + "]，实际 [" + actual + "]");
        }
    }

    /** 断言条件成立，失败时抛出 {@link AssertionError}。 */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " 失败");
        }
    }

    /**
     * 断言执行体抛出指定类型的异常。
     *
     * <p>不引入测试框架时的轻量替代：抛对了直接通过，抛错类型或没抛都给出中文说明。
     */
    private static void assertThrows(Class<? extends Exception> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Exception e) {
            if (expectedType.isInstance(e)) {
                return;
            }
            throw new AssertionError(message + " 失败：期望抛出 " + expectedType.getSimpleName()
                    + "，实际抛出 " + e.getClass().getName(), e);
        }
        throw new AssertionError(message + " 失败：期望抛出 " + expectedType.getSimpleName() + "，但没有抛出任何异常");
    }

    // ------------------------------------------------------------------
    // main 自测入口：先打印示例建立直觉，再跑标准断言
    // ------------------------------------------------------------------

    /**
     * 自测入口：先打印各方法的典型输出建立直觉，再执行一组断言，全部通过后输出「通过」字样。
     *
     * <p>所有用例都使用固定数据，不依赖系统时间、随机数与环境变量，任何机器上跑结果都一样。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        // ------------------ 示例演示 ------------------
        List<Integer> numbers = newArrayList(3, 1, 3, 2);
        System.out.println("isEmpty((Collection<?>) null) = " + isEmpty((Collection<?>) null));
        System.out.println("emptyIfNull(null).size() = " + emptyIfNull((Collection<Integer>) null).size());
        System.out.println("firstElement([3, 1, 3, 2]) = " + firstElement(numbers));
        System.out.println("lastElement([3, 1, 3, 2]) = " + lastElement(numbers));
        System.out.println("getOrDefault([3, 1, 3, 2], 9, -1) = " + getOrDefault(numbers, 9, -1));
        System.out.println("distinct([3, 1, 3, 2]) = " + distinct(numbers));
        System.out.println("filter(n > 1) = " + filter(numbers, n -> n > 1));
        System.out.println("map(n * 2) = " + map(numbers, n -> n * 2));
        System.out.println("groupBy(奇偶) = " + groupBy(numbers, n -> n % 2 == 0 ? "偶数" : "奇数"));
        System.out.println("toMap(数字 -> 平方) = " + toMap(newArrayList(1, 2, 3), n -> n, n -> n * n));
        System.out.println("partition([3, 1, 3, 2], 3) = " + partition(numbers, 3));
        System.out.println("page([3, 1, 3, 2], 2, 3) = " + page(numbers, 2, 3));
        System.out.println("union([1, 2, 2], [2, 3]) = " + union(newArrayList(1, 2, 2), newArrayList(2, 3)));
        System.out.println("intersection([1, 2, 3], [2, 3, 4]) = "
                + intersection(newArrayList(1, 2, 3), newArrayList(2, 3, 4)));
        System.out.println("difference([1, 2, 3], [2]) = "
                + difference(newArrayList(1, 2, 3), newArrayList(2)));

        // ------------------ 断言自测 ------------------
        // 空值判断：null 与「空壳」（空集合 / 空 Map / 空数组）都算空
        assertTrue(isEmpty((Collection<String>) null), "null 集合应判为空");
        assertTrue(isEmpty(Collections.<String>emptyList()), "空集合应判为空");
        assertTrue(isNotEmpty(newArrayList("a")), "有元素的集合应判为非空");
        assertTrue(isEmpty((Map<String, String>) null), "null Map 应判为空");
        assertTrue(isNotEmpty(Collections.singletonMap("k", "v")), "有键值对的 Map 应判为非空");
        assertTrue(isEmpty(new String[0]), "空数组应判为空");
        assertTrue(isNotEmpty(new String[] {"a"}), "有元素的数组应判为非空");

        // null 归一与默认值
        assertEquals(0, emptyIfNull((Collection<String>) null).size(), "null 集合应归一为空集合");
        assertEquals(1, emptyIfNull(Collections.singletonList("a")).size(), "非 null 集合应原样返回");
        assertEquals(0, emptyIfNull((Map<String, String>) null).size(), "null Map 应归一为空 Map");
        assertEquals(newArrayList("默认"), defaultIfEmpty(null, newArrayList("默认")), "null 列表应返回默认值");
        assertEquals(newArrayList("默认"), defaultIfEmpty(new ArrayList<String>(), newArrayList("默认")),
                "空列表应返回默认值");
        List<String> kept = newArrayList("原值");
        assertTrue(kept == defaultIfEmpty(kept, newArrayList("默认")), "非空列表应原样返回同一个引用");
        assertThrows(UnsupportedOperationException.class,
                () -> emptyIfNull((Collection<String>) null).add("x"), "归一得到的是共享空集合，不可写入");
        assertThrows(NullPointerException.class,
                () -> defaultIfEmpty(newArrayList("a"), null), "默认列表为 null 应拒绝");

        // 安全取值
        assertEquals("a", firstElement(newArrayList("a", "b")), "取第一个元素");
        assertEquals(null, firstElement(Collections.<String>emptyList()), "空集合取第一个元素应返回 null");
        assertEquals("b", lastElement(newArrayList("a", "b")), "取最后一个元素");
        assertEquals(null, lastElement(Collections.<String>emptyList()), "空列表取最后一个元素应返回 null");
        assertEquals("b", getOrDefault(newArrayList("a", "b"), 1, "缺省"), "下标合法时应取到对应元素");
        assertEquals("缺省", getOrDefault(newArrayList("a", "b"), 5, "缺省"), "下标过大应返回默认值");
        assertEquals("缺省", getOrDefault(newArrayList("a", "b"), -1, "缺省"), "负下标应返回默认值");

        // 创建集合
        List<String> created = newArrayList("a", "b");
        created.add("c");
        assertEquals(3, created.size(), "newArrayList 返回的列表必须可写");
        assertEquals(0, newArrayList().size(), "newArrayList 允许一个元素都不传");
        assertEquals(1, newArrayList((String) null).size(), "newArrayList 允许 null 元素");

        // 去重、过滤、映射
        assertEquals(newArrayList(3, 1, 2), distinct(newArrayList(3, 1, 3, 2)), "去重应保留元素首次出现的顺序");
        assertEquals(newArrayList(3, 3), filter(newArrayList(3, 1, 3, 2), n -> n > 2), "过滤应只保留满足条件的元素");
        assertEquals(newArrayList(6, 2, 6, 4), map(newArrayList(3, 1, 3, 2), n -> n * 2), "映射应逐个转换且保持顺序");
        assertEquals(0, filter(newArrayList(1, 2), n -> n > 9).size(), "没有元素满足条件时应返回空列表");

        // 分组与转换
        Map<String, List<Integer>> byParity = groupBy(newArrayList(3, 1, 2), n -> n % 2 == 0 ? "偶数" : "奇数");
        assertEquals(newArrayList(3, 1), byParity.get("奇数"), "奇数分组的内容与顺序");
        assertEquals(newArrayList(2), byParity.get("偶数"), "偶数分组的内容与顺序");
        assertEquals(newArrayList("奇数", "偶数"), new ArrayList<String>(byParity.keySet()),
                "分组 key 应按首次出现的顺序排列");
        Map<String, List<Integer>> nullKeyGrouped = groupBy(newArrayList(1, 2), n -> (String) null);
        assertTrue(nullKeyGrouped.containsKey(null), "key 算成 null 时也应正常分组");

        Map<String, Integer> lengths = toMap(newArrayList("a", "bb", "ccc"), s -> s, String::length);
        assertEquals(3, lengths.get("ccc").intValue(), "转 Map 的 value 应逐个计算");
        assertEquals(newArrayList("a", "bb", "ccc"), new ArrayList<String>(lengths.keySet()), "转 Map 的结果应保序");
        assertThrows(IllegalArgumentException.class,
                () -> toMap(newArrayList("a", "a"), s -> s, String::length), "重复 key 应直接拒绝");
        assertThrows(NullPointerException.class,
                () -> toMap(newArrayList("a"), s -> (String) null, String::length), "key 为 null 应直接拒绝");

        // 切分与分页
        assertEquals(newArrayList(newArrayList(1, 2, 3), newArrayList(4, 5)),
                partition(newArrayList(1, 2, 3, 4, 5), 3), "按容量切分，最后一段允许不满");
        assertEquals(0, partition(Collections.<Integer>emptyList(), 3).size(), "空列表切分后没有任何段");
        List<Integer> source = newArrayList(1, 2, 3);
        List<List<Integer>> parts = partition(source, 2);
        parts.get(0).add(99);
        assertEquals(newArrayList(1, 2, 3), source, "切分返回的是拷贝，改某一段不会影响原列表");

        assertEquals(newArrayList(4, 5), page(newArrayList(1, 2, 3, 4, 5), 2, 3), "第 2 页（每页 3 条）应取第 4、5 条");
        assertEquals(newArrayList(1, 2, 3), page(newArrayList(1, 2, 3), 1, 3), "最后一页不足整页时只返回剩余元素");
        assertEquals(0, page(newArrayList(1, 2, 3), 9, 3).size(), "页码超过总页数应返回空列表");
        assertEquals(0, page(newArrayList(1, 2, 3), 0, 3).size(), "页码小于 1 应返回空列表");
        assertThrows(IllegalArgumentException.class, () -> page(newArrayList(1), 1, 0), "每页条数不能为 0");
        assertThrows(IllegalArgumentException.class, () -> partition(newArrayList(1), -1), "每段容量不能为负");

        // 集合运算
        assertEquals(newArrayList(1, 2, 3), union(newArrayList(1, 2, 2), newArrayList(2, 3)), "并集应去重且保序");
        assertEquals(newArrayList(2, 3), intersection(newArrayList(1, 2, 3), newArrayList(2, 3, 4)), "交集应保序");
        assertEquals(newArrayList(1), difference(newArrayList(1, 2, 3), newArrayList(2, 3)), "差集应保序");
        assertEquals(0, intersection(newArrayList(1), newArrayList(2)).size(), "没有公共元素时交集为空");
        assertEquals(newArrayList(3), difference(newArrayList(3), Collections.<Integer>emptyList()),
                "与空集合求差集应得到原集合");

        // 参数校验：不允许 null 的方法必须尽早报错
        assertThrows(NullPointerException.class, () -> firstElement((Collection<String>) null),
                "firstElement 收到 null 集合应拒绝");
        assertThrows(NullPointerException.class, () -> distinct((Collection<String>) null),
                "distinct 收到 null 集合应拒绝");
        assertThrows(NullPointerException.class, () -> filter(newArrayList(1), null), "过滤条件为 null 应拒绝");
        assertThrows(NullPointerException.class, () -> groupBy(newArrayList(1), null), "分组依据为 null 应拒绝");

        System.out.println("CollectionUtils 自测通过");
    }
}
