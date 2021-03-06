package com.yibao.music.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yibao.music.R;
import com.yibao.music.base.BaseRvAdapter;
import com.yibao.music.base.listener.OnMusicItemClickListener;
import com.yibao.music.model.MusicBean;
import com.yibao.music.util.Constants;
import com.yibao.music.util.ImageUitl;
import com.yibao.music.util.StringUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @项目名： BigGirl
 * @包名： ${PACKAGE_NAME}
 * @文件名: ${NAME}
 * @author: Stran
 * @Email: www.strangermy@outlook.com / www.stranger98@gmail.com
 * @创建时间: 2016/11/5 15:53
 * @描述： {TODO}
 */

public class SongAdapter
        extends BaseRvAdapter<MusicBean>


{
    private Context mContext;
    private int mIsShowStickyView = 0;
    private OnOpenItemMoerMenuListener mListener;

    /**
     * @param context
     * @param list
     * @param isShowStickyView 控制列表的StickyView是否显示，0 显示 ，1 ：不显示
     *                         parm isArtistList     用来控制音乐列表和艺术家列表的显示
     */
    public SongAdapter(Context context, List<MusicBean> list, int isShowStickyView) {
        super(list);
        this.mContext = context;
        this.mIsShowStickyView = isShowStickyView;
    }


    @NonNull
    @Override
    public SongListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music_list, parent, false);
        return new SongListViewHolder(view);
    }

    @Override
    protected String getLastItemDes() {
        return " 首歌";
    }

    @Override
    protected void bindView(RecyclerView.ViewHolder holder, MusicBean info) {
        if (holder instanceof SongListViewHolder) {

            SongListViewHolder songListViewHolder = (SongListViewHolder) holder;
            int position = holder.getAdapterPosition();
            songListViewHolder.mSongArtistName.setText(info.getArtist());
            ImageUitl.customLoadPic(mContext, StringUtil.getAlbulm(info.getAlbumId()), R.drawable.noalbumcover_120, songListViewHolder.mSongAlbum);
            songListViewHolder.mSongName.setText(info.getTitle());
            if (mIsShowStickyView == Constants.NUMBER_ZOER) {
                String firstTv = info.getFirstChar();
                songListViewHolder.mTvStickyView.setText(firstTv);
                if (position == 0) {
                    songListViewHolder.mTvStickyView.setVisibility(View.VISIBLE);
                } else if (firstTv.equals(mList.get(position - 1)
                        .getFirstChar())) {
                    songListViewHolder.mTvStickyView.setVisibility(View.GONE);

                } else {
                    songListViewHolder.mTvStickyView.setVisibility(View.VISIBLE);
                }
            } else {

                songListViewHolder.mTvStickyView.setVisibility(View.GONE);
            }

            songListViewHolder.mIvSongItemMenu.setOnClickListener(view -> {
                if (mListener != null) {
                    mListener.openClickMoerMenu();
                }
            });

            //  Item点击监听
            songListViewHolder.mLlMusicItem.setOnClickListener(view -> {
                if (mContext instanceof OnMusicItemClickListener) {
                    ((OnMusicItemClickListener) mContext).startMusicService(position);
                }
            });

        }

    }

    @Override
    protected RecyclerView.ViewHolder getViewHolder(View view) {

        return new SongListViewHolder(view);
    }

    @Override
    protected int getLayoutId() {

        return R.layout.item_music_list;
    }

    @Override
    public int getItemCount() {

        return mList == null ? Constants.NUMBER_ZOER : mList.size();
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }


    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = mList.get(i).getFirstChar().toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {

        return mList.get(position).getFirstChar().toUpperCase().charAt(0);
    }


    static class SongListViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.item_sticky_view)
        TextView mTvStickyView;
        @BindView(R.id.song_album)
        ImageView mSongAlbum;
        @BindView(R.id.song_item_play_flag)
        ImageView mSongPlayFlag;
        @BindView(R.id.song_name)
        TextView mSongName;
        @BindView(R.id.song_artist_name)
        TextView mSongArtistName;
        @BindView(R.id.ll_music_item)
        LinearLayout mLlMusicItem;
        @BindView(R.id.iv_song_item_menu)
        ImageView mIvSongItemMenu;

        SongListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }

    }


    public void setOnItemMenuListener(OnOpenItemMoerMenuListener listener) {
        mListener = listener;
    }

    public interface OnOpenItemMoerMenuListener {
        /**
         * 更多菜单
         */
        void openClickMoerMenu();
    }

}
