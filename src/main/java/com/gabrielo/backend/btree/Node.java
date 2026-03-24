package com.gabrielo.backend.btree;

import com.gabrielo.backend.pager.Page;

public sealed interface Node permits LeafNode, InternalNode {

  int getParentPageId();

  void setParentPageId(int pageId);

  static Node from(Page page) {
    byte nodeType = page.getBuffer().get(NodeLayout.NODE_TYPE_OFFSET);
    return switch (nodeType) {
      case NodeLayout.NODE_TYPE_LEAF -> new LeafNode(page);
      case NodeLayout.NODE_TYPE_INTERNAL -> new InternalNode(page);
      default -> throw new IllegalStateException("Unknown node type: " + nodeType);
    };
  }
}
