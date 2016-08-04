package zhj.notetaking.listener;


import java.util.Map;

public interface ItemClickListener {
  /**
   * 某个笔记被点击
   * @param info 笔记信息
   * @param stringStringMap
   */
  void onItemClick(Map<String, String> stringStringMap);
}
