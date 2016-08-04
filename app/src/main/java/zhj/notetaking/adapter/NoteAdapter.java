package zhj.notetaking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import zhj.notetaking.R;
import zhj.notetaking.listener.ItemClickListener;
import zhj.notetaking.listener.ItemLongClickListener;

/**
 * Created by HongJay on 2016/8/4.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 笔记集合
     */
    private List<Map<String, String>> data_list;

    /**
     * 条目点击事件
     */
    private ItemClickListener mItemClickListener;
    /**
     * 长按点击事件
     */
    private ItemLongClickListener mItemLongClickListener;


    /**
     * 设置点击监听器
     *
     * @param mItemClickListener 监听器
     */
    public void setItemClickListener(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    /**
     * 设置长按监听器
     *
     * @param mItemLongClickListener 监听器
     */
    public void setItemLongClickListener(ItemLongClickListener mItemLongClickListener) {
        this.mItemLongClickListener = mItemLongClickListener;
    }

    public NoteAdapter(Context context, List<Map<String, String>> data_list) {
        this.mContext = context;

        this.data_list = data_list;

    }
    /**
     * 获取笔记
     */
    public void getNoteInfos(List<Map<String, String>> data_list) {
        this.data_list = data_list;
    }

    @Override
    public NoteAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Map<String, String> data = data_list.get(position);
        Set<String> set = data.keySet();
        Iterator<String> it = set.iterator();
        if (it.hasNext()) {
            String text = it.next();
            holder.noteText.setText(data.get(it.next()));
            holder.noteTime.setText(data.get(text));
        }


    }


    @Override
    public int getItemCount() {
        return data_list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.note_text)
        TextView noteText;
        @BindView(R.id.note_time)
        TextView noteTime;

        // 卡片
        View card;

        public MyViewHolder(View itemView) {
            super(itemView);
            card = itemView;
            ButterKnife.bind(this, itemView);

            card.setOnLongClickListener(this);
            card.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(data_list.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            mItemLongClickListener.onItemLongClick(data_list.get(getAdapterPosition()));
            return true;
        }
    }
}
