package com.gabrielo.backend.btree;

public class NodeLayout {

  // Common header
  public static final int NODE_TYPE_SIZE = 1;
  public static final int IS_ROOT_SIZE = 1;
  public static final int PARENT_PAGE_ID_SIZE = 4;

  public static final int COMMON_HEADER_SIZE = NODE_TYPE_SIZE + IS_ROOT_SIZE + PARENT_PAGE_ID_SIZE;

  // Leaf node header
  public static final int NUM_CELLS_SIZE = 4;
  public static final int NEXT_LEAF_PAGE_ID_SIZE = 4;

  public static final int LEAF_HEADER_SIZE = COMMON_HEADER_SIZE + NUM_CELLS_SIZE + NEXT_LEAF_PAGE_ID_SIZE;

  // Leaf cell layout
  public static final int CELL_KEY_SIZE = 4;
  public static final int RECORD_SIZE = 68;
  public static final int CELL_SIZE = CELL_KEY_SIZE + RECORD_SIZE;

  // Leaf capacity
  public static final int PAGE_SIZE = 4096;
  public static final int LEAF_MAX_CELLS = (PAGE_SIZE - LEAF_HEADER_SIZE) / CELL_SIZE;

  // Internal node header
  public static final int NUM_KEYS_SIZE = 4;
  public static final int RIGHT_CHILD_PAGE_ID_SIZE = 4;

  public static final int INTERNAL_HEADER_SIZE = COMMON_HEADER_SIZE + NUM_KEYS_SIZE + RIGHT_CHILD_PAGE_ID_SIZE;

  // Internal node child entry: [childPageId (4 bytes)] [key (4 bytes)]
  public static final int CHILD_PAGE_ID_SIZE = 4;
  public static final int INTERNAL_CHILD_SIZE = CHILD_PAGE_ID_SIZE + CELL_KEY_SIZE;

  // Internal node capacity
  public static final int INTERNAL_MAX_KEYS = (PAGE_SIZE - INTERNAL_HEADER_SIZE) / INTERNAL_CHILD_SIZE;

  // Node type constants
  public static final byte NODE_TYPE_LEAF = 0;
  public static final byte NODE_TYPE_INTERNAL = 1;

  private NodeLayout() {
  }
}
