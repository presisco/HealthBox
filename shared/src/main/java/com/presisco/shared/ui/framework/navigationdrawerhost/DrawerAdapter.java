package com.presisco.shared.ui.framework.navigationdrawerhost;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by presisco on 2017/6/6.
 */
public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEWTYPE_ITEM = 2;
    private static final int VIEWTYPE_HEADER = 1;
    private static final int VIEWTYPE_FOOTER = 3;

    private boolean has_header = false;
    private boolean has_footer = false;

    private LayoutInflater li;
    private int mItemLayoutId;
    private int mHeaderLayoutId;
    private int mFooterLayoutId;

    private FooterListener mOnFooter;
    private ItemListener mOnItem;
    private HeaderListener mOnHeader;

    private ItemHolder mSelectedItem;
    private int mSelectedPosition;

    private int mItemCount;

    public DrawerAdapter(Context context, int item_layout_id, ItemListener listener) {
        li = LayoutInflater.from(context);
        mItemLayoutId = item_layout_id;
        mOnItem = listener;
    }

    public void setHeader(int id, HeaderListener listener) {
        mHeaderLayoutId = id;
        has_header = true;
        mOnHeader = listener;
    }

    public void setFooter(int id, FooterListener listener) {
        mFooterLayoutId = id;
        has_footer = true;
        mOnFooter = listener;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWTYPE_ITEM:
                return new ItemHolder(li.inflate(mItemLayoutId, null, false), mOnItem);
            case VIEWTYPE_HEADER:
                return new HeaderHolder(li.inflate(mHeaderLayoutId, null, false));
            case VIEWTYPE_FOOTER:
                return new FooterHolder(li.inflate(mFooterLayoutId, null, false));
            default:
                return null;
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEWTYPE_ITEM:
                mOnItem.onBindItem((ItemHolder) holder, getItemPosition(position));
                break;
            case VIEWTYPE_FOOTER:
                mOnFooter.onBindFooter((FooterHolder) holder);
                break;
            case VIEWTYPE_HEADER:
                mOnHeader.onBindHeader((HeaderHolder) holder);
        }
    }

    /**
     * get the real item position
     *
     * @param position input raw position
     * @return position in item array
     */
    public int getItemPosition(int position) {
        if (has_header)
            return position - 1;
        else
            return position;
    }

    public int getItemCount() {
        int total = mItemCount;
        if (has_header)
            total++;
        if (has_footer)
            total++;
        return total;
    }

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if (has_header) {
                return VIEWTYPE_HEADER;
            } else if (has_footer && getItemCount() == 1) {
                return VIEWTYPE_FOOTER;
            } else {
                return VIEWTYPE_ITEM;
            }
        } else if (position == getItemCount() - 1) {
            if (has_footer) {
                return VIEWTYPE_FOOTER;
            } else if (has_header && position == 0) {
                return VIEWTYPE_HEADER;
            } else {
                return VIEWTYPE_ITEM;
            }
        } else {
            return VIEWTYPE_ITEM;
        }
    }

    public interface FooterListener {
        void onBindFooter(FooterHolder footerHolder);
    }

    public interface ItemListener {
        void onBindItem(ItemHolder itemHolder, int position);

        void onClickItem(ItemHolder itemHolder, int position);

        void onRestoreItem(ItemHolder itemHolder, int position);
    }

    public interface HeaderListener {
        void onBindHeader(HeaderHolder headerHolder);
    }

    public class RootHolder extends RecyclerView.ViewHolder {
        protected final View rootView;

        public RootHolder(View itemView) {
            super(itemView);
            rootView = itemView;
        }

        public View getRootView() {
            return rootView;
        }

        public View findViewById(int id) {
            return rootView.findViewById(id);
        }
    }

    public class ItemHolder extends RootHolder {
        public ItemHolder(View itemView, final ItemListener listener) {
            super(itemView);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClickItem(ItemHolder.this, getItemPosition(getAdapterPosition()));
                    if (mSelectedItem != null) {
                        listener.onRestoreItem(mSelectedItem, mSelectedPosition);
                    }
                    mSelectedItem = ItemHolder.this;
                    mSelectedPosition = getItemPosition(getAdapterPosition());
                }
            });
        }
    }

    public class HeaderHolder extends RootHolder {
        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    public class FooterHolder extends RootHolder {
        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}
