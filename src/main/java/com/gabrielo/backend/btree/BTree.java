package com.gabrielo.backend.btree;

import java.io.IOException;

import com.gabrielo.backend.pager.Page;
import com.gabrielo.backend.pager.Pager;

public class BTree {

  private final Pager pager;

  private int rootPageId = -1;

  public BTree(Pager pager) {
    this.pager = pager;
  }

  public void insert(int key, byte[] record) throws IOException {
    if (rootPageId == -1) {
      Page root = pager.allocateNewPage();
      rootPageId = root.getId();
      LeafNode leafNode = new LeafNode(root);
      leafNode.initialize();
    }

    Page leafPage = findLeafPage(key);
    LeafNode leafNode = new LeafNode(leafPage);
    leafNode.insert(key, record);
    leafPage.markDirty();

    if (leafNode.isFull()) {
      splitLeaf(leafNode, leafPage);
    }
  }

  private Page findLeafPage(int key) throws IOException {
    Page current = pager.getPage(rootPageId);
    while (true) {
      byte nodeType = current.getBuffer().get(0);
      if (nodeType == NodeLayout.NODE_TYPE_LEAF) {
        return current;
      }
      InternalNode internalNode = new InternalNode(current);
      int childPageId = internalNode.findChild(key);
      current = pager.getPage(childPageId);
    }
  }

  private void splitLeaf(LeafNode leafNode, Page leafPage) throws IOException {
    Page newPage = pager.allocateNewPage();
    int splitKey = leafNode.split(newPage);
    int leftPageId = leafPage.getId();
    int rightPageId = newPage.getId();

    if (leftPageId == rootPageId) {
      // The leaf being split is the root — create a new internal root
      Page newRootPage = pager.allocateNewPage();
      InternalNode newRoot = new InternalNode(newRootPage);
      newRoot.initialize();
      newRoot.insert(splitKey, leftPageId, rightPageId);
      rootPageId = newRootPage.getId();

      leafNode.setParentPageId(rootPageId);
      new LeafNode(newPage).setParentPageId(rootPageId);

      newRootPage.markDirty();
    } else {
      // Insert the split key into the existing parent internal node
      int parentPageId = leafNode.getParentPageId();
      Page parentPage = pager.getPage(parentPageId);
      InternalNode parentNode = new InternalNode(parentPage);

      new LeafNode(newPage).setParentPageId(parentPageId);
      parentNode.insert(splitKey, leftPageId, rightPageId);
      parentPage.markDirty();

      if (parentNode.isFull()) {
        splitInternal(parentNode, parentPage);
      }
    }
  }

  private void splitInternal(InternalNode node, Page nodePage) throws IOException {
    Page newPage = pager.allocateNewPage();
    InternalNode newRight = new InternalNode(newPage);
    newRight.initialize();

    int totalKeys = node.getNumKeys();
    int midIndex = totalKeys / 2;
    int splitKey = node.getKey(midIndex);

    int originalRightChild = node.getRightChildPageId();

    for (int i = midIndex + 1; i < totalKeys; i++) {
      int childPageId = node.getChildPageId(i);
      int k = node.getKey(i);
      newRight.insertEntry(i - midIndex - 1, k, childPageId);
    }
    newRight.setRightChildPageId(originalRightChild);
    newRight.setNumKeysDirectly(totalKeys - midIndex - 1);

    // Update parent pointers for children that moved to the right node
    for (int i = midIndex + 1; i < totalKeys; i++) {
      updateChildParent(node.getChildPageId(i), newPage.getId());
    }
    updateChildParent(originalRightChild, newPage.getId());

    node.setRightChildPageId(node.getChildPageId(midIndex));
    node.setNumKeysDirectly(midIndex);

    int leftPageId = nodePage.getId();
    int rightPageId = newPage.getId();

    newPage.markDirty();
    nodePage.markDirty();

    if (leftPageId == rootPageId) {
      Page newRootPage = pager.allocateNewPage();
      InternalNode newRoot = new InternalNode(newRootPage);
      newRoot.initialize();
      newRoot.insert(splitKey, leftPageId, rightPageId);
      rootPageId = newRootPage.getId();

      node.setParentPageId(rootPageId);
      newRight.setParentPageId(rootPageId);
      newRootPage.markDirty();
      return;
    }

    int parentPageId = node.getParentPageId();
    Page parentPage = pager.getPage(parentPageId);
    InternalNode parentNode = new InternalNode(parentPage);

    newRight.setParentPageId(parentPageId);
    parentNode.insert(splitKey, leftPageId, rightPageId);
    parentPage.markDirty();

    if (parentNode.isFull()) {
      splitInternal(parentNode, parentPage);
    }
  }

  private void updateChildParent(int childPageId, int newParentPageId) throws IOException {
    Page childPage = pager.getPage(childPageId);
    byte nodeType = childPage.getBuffer().get(0);
    if (nodeType == NodeLayout.NODE_TYPE_LEAF) {
      new LeafNode(childPage).setParentPageId(newParentPageId);
    } else {
      new InternalNode(childPage).setParentPageId(newParentPageId);
    }
    childPage.markDirty();
  }

  public int getRootPageId() {
    return rootPageId;
  }

  public void setRootPageId(int rootPageId) {
    this.rootPageId = rootPageId;
  }

  public int getLeftMostLeafPageId() throws IOException {
    if (rootPageId == -1) {
      return -1;
    }
    Page current = pager.getPage(rootPageId);
    while (true) {
      byte nodeType = current.getBuffer().get(0);
      if (nodeType == NodeLayout.NODE_TYPE_LEAF) {
        return current.getId();
      }
      InternalNode internalNode = new InternalNode(current);
      int childPageId = internalNode.getChildPageId(0);
      current = pager.getPage(childPageId);
    }
  }

  public byte[] search(int key) throws IOException {
    if (rootPageId == -1) {
      return null;
    }
    Page leafPage = findLeafPage(key);
    LeafNode leafNode = new LeafNode(leafPage);
    int cellIndex = leafNode.findCell(key);
    if (cellIndex == -1) {
      return null;
    }
    return leafNode.getCell(cellIndex);
  }
}
