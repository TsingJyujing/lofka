package com.github.tsingjyujing.lofka.algorithm.cluster.tree;

import java.util.List;

/**
 * 树形结构的节点
 *
 * @param <T>
 */
public class Node<T> {
    private T dataValue;

    private List<Node<T>> subNodes;

    /**
     * 初始化节点
     * @param dataValue 数据内容
     * @param subNodes 子节点
     */
    public Node(T dataValue, List<Node<T>> subNodes) {
        this.dataValue = dataValue;
        this.subNodes = subNodes;
    }

    public T getDataValue() {
        return dataValue;
    }

    public void setDataValue(T dataValue) {
        this.dataValue = dataValue;
    }

    public List<Node<T>> getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(List<Node<T>> subNodes) {
        this.subNodes = subNodes;
    }


}
