package zhj.notetaking.listener;


import java.util.List;

import zhj.notetaking.data.NoteInfo;

public interface ISearchAdapter {
  /**
   * 获取笔记信息
   * @return
   */
  List<NoteInfo> get();
}
