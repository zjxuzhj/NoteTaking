package zhj.notetaking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import zhj.notetaking.data.NoteInfo;
import zhj.notetaking.listener.ISearchAdapter;
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
    private List<NoteInfo> data_list;
    /** 适配器类型 */
    private AdapterType type = AdapterType.NOTE_TYPE;
    ISearchAdapter data;
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
     * 设置数据和数据类型
     * @param type 类型
     * @param data 数据
     */
    public void setDataAndType(AdapterType type, ISearchAdapter data) {
        this.type = type;
        this.data = data;
    }


    /**
     * 设置长按监听器
     *
     * @param mItemLongClickListener 监听器
     */
    public void setItemLongClickListener(ItemLongClickListener mItemLongClickListener) {
        this.mItemLongClickListener = mItemLongClickListener;
    }

    public NoteAdapter(Context context, List<NoteInfo> data_list) {
        this.mContext = context;

        // 默认
        setDataAndType(AdapterType.NOTE_TYPE, null);
        getNoteInfos( data_list);

    }

    /**
     * 获取笔记
     */
    public void getNoteInfos(List<NoteInfo> data_list) {

        switch (type) {
            case NOTE_TYPE:
                this.data_list = data_list;
                break;
            case SEARCH_TYPE:
                this.data_list = data.get();
                break;
        }
    }
    @Override
    public NoteAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        NoteInfo info = data_list.get(position);
        holder.noteText.setText(info.getNote());
        holder.noteTime.setText(info.getTime());


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
