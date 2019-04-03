package com.github.tsingjyujing.lofka.algorithm.cluster;

import com.github.tsingjyujing.lofka.algorithm.cluster.common.DivisibleGenerator;
import com.github.tsingjyujing.lofka.algorithm.cluster.common.IDivisible;
import com.github.tsingjyujing.lofka.algorithm.cluster.common.SingleString;
import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * 增量式文本泛化器（用纯Java7写算法真的是生不如死啊）
 *
 * @author yuanyifan
 */
@Beta
public class IncrementalTextGeneralizer implements Serializable {

    /**
     * 最小公共长度
     */
    private final int minCommonSize;
    private final int maxUnfetchCount;

    /**
     * 当前的结果集合
     */
    private final List<String> pattern;

    /**
     * 根据泛化结果生成
     *
     * @param generalizeResult 泛化结果
     */
    public IncrementalTextGeneralizer(int minCommonSize, int maxUnfetchCount, Iterable<String> generalizeResult) {
        this.minCommonSize = minCommonSize;
        this.maxUnfetchCount = maxUnfetchCount;
        this.pattern = Lists.newArrayList(generalizeResult);
    }

    /**
     * 根据字符串生成
     *
     * @param minCommonSize 最小连续串大小（推荐值：1）
     * @param textArray     文本序列
     */
    public IncrementalTextGeneralizer(int minCommonSize, int maxUnfetchCount, String... textArray) {
        this(
                minCommonSize,
                maxUnfetchCount,
                generatePatternByTextArray(minCommonSize, textArray)
        );
    }

    /**
     * 根据字符串生成
     *
     * @param textArray 文文本集合
     * @return
     */
    private static Iterable<String> generatePatternByTextArray(int minCommonSize, String[] textArray) {
        final List<IDivisible<String>> patterns = new DivisibleGenerator<String>().analysisCommonStrings(
                minCommonSize,
                DivisibleGenerator.generateSequenceByComma(
                        textArray
                )
        );
        final List<String> result = Lists.newArrayList();
        for (IDivisible<String> patternDetail : patterns) {
            if (patternDetail instanceof SingleString) {
                result.add(patternDetail.toString());
            }
        }
        return result;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public String getPattern(int index) {
        return pattern.get(index);
    }

    /**
     * 向当前的模型追加训练数据
     *
     * @param appendData 追加的数据
     * @throws Exception 追加失败
     */
    public void append(String appendData) {
        synchronized (pattern) {
            final List<Integer> fetchResult = fuzzyFetch(getPattern(), appendData);
            int unfetchCount = Iterables.size(
                    Iterables.filter(fetchResult, new Predicate<Integer>() {
                        @Override
                        public boolean apply(@Nullable Integer input) {
                            return input == null || input < 0;
                        }
                    })
            );
            if (!resultCanFuzzyFetch(fetchResult, maxUnfetchCount)) {
                throw new RuntimeException(
                        "Can't fuzzy fetch append data"
                );
            }
            // 分裂不匹配项，重构Pattern
            if (unfetchCount > 0) {
                final List<String> newPattern = Lists.newArrayList();
                for (int i = 0; i < fetchResult.size(); i++) {
                    final int result = fetchResult.get(i);
                    final String fetchPattern = getPattern(i);
                    if (result >= 0) {
                        newPattern.add(fetchPattern);
                    } else {
                        final int startIndex;
                        final int endIndex;
                        if (i == 0) {
                            startIndex = 0;
                        } else {
                            startIndex = fetchResult.get(i - 1) + getPattern(i - 1).length();
                        }
                        if (i == (fetchResult.size() - 1)) {
                            endIndex = appendData.length();
                        } else {
                            endIndex = fetchResult.get(i + 1);
                        }
                        final String subDataString = appendData.substring(
                                startIndex, endIndex
                        );

                        final List<IDivisible<String>> middleRes = new DivisibleGenerator<String>().analysisCommonStrings(
                                minCommonSize,
                                DivisibleGenerator.generateSequenceByComma(
                                        getPattern(i),
                                        subDataString
                                )
                        );

                        for (IDivisible<String> patternInfo : middleRes) {
                            if (patternInfo instanceof SingleString) {
                                newPattern.add(patternInfo.toString());
                            }
                        }
                    }
                }
                if (canFetch(newPattern, appendData)) {
                    getPattern().clear();
                    getPattern().addAll(newPattern);
                } else {
                    // 不能构造新的模式，抛出异常
                    throw new RuntimeException(
                            "Append failed caused by can't construct new pattern"
                    );
                }
            }
        }
    }

    /**
     * 顺序匹配数据
     *
     * @param pattern 需要匹配的模式
     * @param data    待匹配的数据
     * @return 匹配结果 是否能正确匹配
     */
    public static boolean canFetch(final List<String> pattern, final String data) {
        int startIndex = 0;
        for (String searchInfo : pattern) {
            final int currentSearchResult = data.indexOf(searchInfo, startIndex);
            if (currentSearchResult >= 0) {
                startIndex = currentSearchResult + searchInfo.length();
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean canFuzzyFetch(final List<String> pattern, final String data, int maxUnfetchCount) {
        return resultCanFuzzyFetch(
                fuzzyFetch(pattern, data),
                maxUnfetchCount
        );
    }

    private static boolean resultCanFuzzyFetch(List<Integer> fetchResult, int maxUnfetchCount) {
        // 检查不匹配项是否太多
        int unfetchCount = 0;
        int lastFetch = 0;
        for (Integer fetchIndex : fetchResult) {
            if (fetchIndex < 0) {
                if (lastFetch < 0) {
                    // fixme 算法暂时不支持两个连续的不匹配pattern出现，以后期望支持
                    return false;
                } else {
                    unfetchCount++;
                }
            }
            lastFetch = fetchIndex;
        }
        return unfetchCount <= maxUnfetchCount;
    }


    /**
     * 顺序模糊匹配数据
     *
     * @param pattern 需要匹配的模式
     * @param data    待匹配的数据
     * @return 匹配结果，即开始的Index
     */
    public static List<Integer> fuzzyFetch(final List<String> pattern, final String data) {
        int startIndex = 0;
        final List<Integer> result = Lists.newArrayList();
        for (String searchInfo : pattern) {
            final int currentSearchResult = data.indexOf(searchInfo, startIndex);
            if (currentSearchResult >= 0) {
                startIndex = currentSearchResult + searchInfo.length();
            }
            result.add(currentSearchResult);
        }
        return result;
    }

    @Override
    public String toString() {
        return Joiner.on('*').join(getPattern());
    }
}
