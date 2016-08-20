package zhj.notetaking.listener;


import java.util.Map;

import zhj.notetaking.data.NoteInfo;

public interface ItemClickListener {
  /**
   * 某个笔记被点击
   * @param info 笔记信息
   */
  void onItemClick(NoteInfo info);
}
