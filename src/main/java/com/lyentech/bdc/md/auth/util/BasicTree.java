package com.lyentech.bdc.md.auth.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicTree<T extends BasicTree<T>> {
    private Long id;
    private Long pid;
    private List<T> subNode;

    public static <T extends BasicTree<T>> List<T> listToTree(List<T> nodeList) {
        if (nodeList == null || nodeList.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 构造 id->节点 的映射
        Map<Long, T> idNodeMap = nodeList.stream()
                .collect(Collectors.toMap(BasicTree::getId, node -> node));

        List<T> rootNodes = new ArrayList<>();

        // 2. 遍历所有节点，组装子节点到父节点
        for (T node : nodeList) {
            Long pid = node.getPid();
            if (pid == null || !idNodeMap.containsKey(pid)) {
                // pid为空或没有对应的父节点，说明是根节点
                rootNodes.add(node);
            } else {
                T parent = idNodeMap.get(pid);
                if (parent.getSubNode() == null) {
                    parent.setSubNode(new ArrayList<>());
                }
                parent.getSubNode().add(node);
            }
        }

        return rootNodes;
    }

    // 省略 getter/setter 方法（保持与之前相同）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPid() { return pid; }
    public void setPid(Long pid) { this.pid = pid; }
    public List<T> getSubNode() { return subNode; }
    public void setSubNode(List<T> subNode) { this.subNode = subNode; }
}