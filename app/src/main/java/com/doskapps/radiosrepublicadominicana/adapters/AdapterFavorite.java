package com.doskapps.radiosrepublicadominicana.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.doskapps.radiosrepublicadominicana.Config;
import com.doskapps.radiosrepublicadominicana.R;
import com.doskapps.radiosrepublicadominicana.models.Radio;
import com.doskapps.radiosrepublicadominicana.utilities.Constant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdapterFavorite extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private List<Radio> items = new ArrayList<>();
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemClickListener mOnItemOverflowClickListener;
    private Radio pos;
    private CharSequence charSequence = null;

    public interface OnItemClickListener {
        void onItemClick(View view, Radio obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterFavorite(Context context, RecyclerView view, List<Radio> items) {
        this.items = items;
        this.context = context;
        lastItemViewDetector(view);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView radio_image;
        public TextView radio_name, category_name, genere_name;
        public ImageView overflow;
        RelativeLayout relativeLayout;

        public OriginalViewHolder(View v) {
            super(v);

            radio_image = v.findViewById(R.id.row_logo);
            radio_name = v.findViewById(R.id.row_label);
            category_name = v.findViewById(R.id.row_category);
            genere_name = v.findViewById(R.id.row_genere);
            overflow = v.findViewById(R.id.overflow);
            relativeLayout = v.findViewById(R.id.relativeLayout);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_radio, parent, false);
            vh = new OriginalViewHolder(v);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof OriginalViewHolder) {

            final Radio p = items.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.radio_name.setText(p.radio_name);
            vItem.category_name.setText(p.category_name);
            vItem.genere_name.setText(p.getGenere_name().concat("          "));
            vItem.genere_name.setSelected(true);

            Picasso
                    .with(context)
                    .load(Config.ADMIN_PANEL_URL + "/upload/" + Constant.LOCALE + "/" + p.radio_image)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(vItem.radio_image);

            vItem.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, p, position);
                    }
                }
            });

            vItem.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemOverflowClickListener != null) {
                        mOnItemOverflowClickListener.onItemClick(view, p, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
            return VIEW_ITEM;
    }

    public void insertData(List<Radio> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / Config.LOAD_MORE;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}