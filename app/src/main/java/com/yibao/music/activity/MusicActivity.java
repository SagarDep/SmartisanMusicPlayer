package com.yibao.music.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.yibao.music.R;
import com.yibao.music.adapter.MusicPagerAdapter;
import com.yibao.music.artisanlist.MusicPagerListener;
import com.yibao.music.base.BaseActivity;
import com.yibao.music.base.listener.OnMusicItemClickListener;
import com.yibao.music.base.listener.UpdataTitleListener;
import com.yibao.music.model.DetailsFlagBean;
import com.yibao.music.model.MusicBean;
import com.yibao.music.model.MusicLyricBean;
import com.yibao.music.model.MusicStatusBean;
import com.yibao.music.model.QqBarUpdataBean;
import com.yibao.music.service.AudioPlayService;
import com.yibao.music.util.Constants;
import com.yibao.music.util.LogUtil;
import com.yibao.music.util.LyricsUtil;
import com.yibao.music.util.QueryMusicFlagListUtil;
import com.yibao.music.util.SharePrefrencesUtil;
import com.yibao.music.util.StringUtil;
import com.yibao.music.util.ToastUtil;
import com.yibao.music.view.MainViewPager;
import com.yibao.music.view.music.MusicNavigationBar;
import com.yibao.music.view.music.QqControlBar;
import com.yibao.music.view.music.SmartisanControlBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Stran
 * Des：${音乐列表界面}
 * Time:2017/5/30 13:27
 */
public class MusicActivity
        extends BaseActivity
        implements OnMusicItemClickListener, UpdataTitleListener {

    @BindView(R.id.tv_music_toolbar_title)
    TextView mTvMusicToolbarTitle;

    @BindView(R.id.music_navigation_bar)
    MusicNavigationBar mMusicNavigationBar;

    @BindView(R.id.music_viewpager)
    MainViewPager mMusicViewPager;

    @BindView(R.id.smartisan_control_bar)
    SmartisanControlBar mSmartisanControlBar;

    @BindView(R.id.qq_control_bar)
    QqControlBar mQqControlBar;


    private List<MusicBean> mMusicItems;

    private static AudioPlayService.AudioBinder audioBinder;
    private AudioServiceConnection mConnection;
    private MusicBean mCurrentMusicBean;
    private int mCurrentPosition;
    private boolean mMusicConfig;
    private boolean isShowQqBar;
    private int mPlayState;

    private int lyricsFlag = 0;
    private ArrayList<MusicLyricBean> mLyricList;
    private int mTitleResourceId;
    // 切换Tab时更改TiTle的标记,打开详情页面时正确显示Title
    private boolean mIsShowDetail;
    private String mDetailViewTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mBind = ButterKnife.bind(this);
        initView();
        initData();
        initRxBusData();
        initMusicConfig();
        initListener();
    }


    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar_music);
        toolbar.setNavigationIcon(R.drawable.music_titlebar_back_selector);
        toolbar.setNavigationOnClickListener(v -> MusicActivity.this.onBackPressed());

    }


    private void initData() {
        List<MusicBean> initMusicList = QueryMusicFlagListUtil.getDataList(getSpMusicFlag(), mMusicDao);
        mCurrentPosition = SharePrefrencesUtil.getMusicPosition(this);
        mCurrentMusicBean = initMusicList.get(mCurrentPosition);
        // 初始化 MusicPagerAdapter 主页面
        MusicPagerAdapter musicPagerAdapter = new MusicPagerAdapter(getSupportFragmentManager());
        mMusicViewPager.setAdapter(musicPagerAdapter);
        mMusicViewPager.setCurrentItem(Constants.NUMBER_TWO);
        mMusicViewPager.setOffscreenPageLimit(5);
    }


    private void initMusicConfig() {
        mMusicConfig = SharePrefrencesUtil.getMusicConfig(this, false);
        if (mMusicConfig) {
            mPlayState = SharePrefrencesUtil.getMusicPlayState(this);
            LogUtil.d("======= mPlayStae  " + mPlayState);
            if (mPlayState == Constants.NUMBER_ONE) {
                // 读取用户的播放记录，设置UI显示，做好播放的准备。(暂停和播放两种状态)
                perpareItem(mCurrentMusicBean);
            } else if (mPlayState == Constants.NUMBER_TWO) {
                executStartServiceAndInitAnimation();
            }
        } else {
            LogUtil.d("用户 ++++  nothing ");
        }


    }

    private void executStartServiceAndInitAnimation() {
        startMusicService(mCurrentPosition);
        mSmartisanControlBar.setPlayButtonState(R.drawable.btn_playing_pause_selector);
        mQqControlBar.setPlayButtonState(R.mipmap.notifycation_pause);
        mPlayState = Constants.NUMBER_THRRE;
    }

    private void initListener() {
        mMusicNavigationBar.setOnNavigationbarListener((currentSelecteFlag, titleResourceId) -> {
//            if (mIsShowDetail) {
//                mTvMusicToolbarTitle.setText(mDetailViewTitle);
//            } else {
//            }
            mTitleResourceId = titleResourceId;
            mTvMusicToolbarTitle.setText(titleResourceId);

            mMusicViewPager.setCurrentItem(currentSelecteFlag, false);
        });
        mSmartisanControlBar.setClickListener(clickFlag -> {
            if (mMusicConfig) {
                switch (clickFlag) {
                    case Constants.NUMBER_ONE:
                        setSongfavoriteState(mCurrentMusicBean, mQqControlBar, mSmartisanControlBar);
                        break;
                    case Constants.NUMBER_TWO:
                        audioBinder.playPre();
                        break;
                    case Constants.NUMBER_THRRE:
                        switchPlayState();
                        break;
                    case Constants.NUMBER_FOUR:
                        audioBinder.playNext();
                        break;
                    default:
                        break;
                }
            } else {
                ToastUtil.showNoMusic(MusicActivity.this);
            }
        });
        mQqControlBar.setOnButtonClickListener(clickFlag -> {
            if (mMusicConfig) {
                switch (clickFlag) {
                    case Constants.NUMBER_ONE:
                        switchPlayState();
                        break;
                    case Constants.NUMBER_TWO:
                        setSongfavoriteState(mCurrentMusicBean, mQqControlBar, mSmartisanControlBar);
                        break;
                    default:
                        break;
                }
            } else {
                ToastUtil.showNoMusic(MusicActivity.this);
            }
        });
        openMusicPlayDialogFag();
        mTvMusicToolbarTitle.setOnClickListener(view -> switchMusicControlBar());
        mQqControlBar.setOnPagerSelecteListener(this::startMusicService);
    }

    /**
     * 切换音乐控制面板的样式
     */
    private void switchMusicControlBar() {
        if (isShowQqBar) {
            mQqControlBar.setVisibility(View.INVISIBLE);
            mSmartisanControlBar.setVisibility(View.VISIBLE);
            disposableQqLyric();
        } else {
            mQqControlBar.setVisibility(View.VISIBLE);
            mSmartisanControlBar.setVisibility(View.INVISIBLE);
            //TODO 这里做更新歌词的操作
            setQqPagerLyric();
        }
        isShowQqBar = !isShowQqBar;
    }

    /**
     * QQbar时时更新歌词
     */
    //TODO
    private void setQqPagerLyric() {
        if (mQqLyricsDisposable == null) {
            mQqLyricsDisposable = Observable.interval(0, 2800, TimeUnit.MICROSECONDS)
//                .onBackpressureBuffer()
                    .subscribeOn(Schedulers.io())
//                .map(new Function<Long, List<MusicBean>>() {
//                    @Override
//                    public List<MusicBean> apply(Long aLong) {
//                        SparseArray<String> lyricArry = new SparseArray<>();
//                        HashMap<String, Integer> flagMap = new HashMap<>();
////                        for (int i = 0; i < mLyricList.size() - 1; i++) {
////                            MusicLyricBean lyricBean = mLyricList.get(i);
////                            lyricArry.put(i, lyricBean.getContent());
////                            flagMap.put(lyricBean.getContent(), i);
////                        }
//
//
//
//                        return mMusicItems;
//                    }
//                })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(musicBeanList -> {
                        if (mLyricList != null && mLyricList.size() > 1 && lyricsFlag < mLyricList.size()) {
                            //通过集合，播放过的歌词就从集合中删除
                            MusicLyricBean lyrBean = mLyricList.get(lyricsFlag);
                            String content = lyrBean.getContent();
                            int progress = audioBinder.getProgress();
                            int startTime = lyrBean.getStartTime();
//                            String s = lyricArry.get(startTime);
//                            Integer integer = flagMap.get(lyrBean.getContent());
                            if (progress > startTime) {
                                MusicBean musicBean = new MusicBean();
                                if (mCurrentPosition < mMusicItems.size()) {
                                    musicBean.setTitle(mMusicItems.get(mCurrentPosition).getTitle());
                                }
                                musicBean.setArtist(content);
                                if (mCurrentPosition < mMusicItems.size()) {
                                    mMusicItems.set(mCurrentPosition, musicBean);
                                }
                                LogUtil.d("当前的位置 ===  " + mCurrentPosition);
                                LogUtil.d("当前的时间和歌词 ===  " + startTime + " ==  " + content);

                                mQqControlBar.updaPagerData(mMusicItems, mCurrentPosition);
                                lyricsFlag++;
                            }


                        }


                    });

        }


    }


    /**
     * 在主列表播放音乐
     * 开启服务，播放音乐并且将数据标记传送过去
     *
     * @param position 当前点击的曲目
     */
    @Override
    public void startMusicService(int position) {
        int spMusicFlag = getSpMusicFlag();
        if (spMusicFlag != Constants.NUMBER_TEN) {
            mCurrentPosition = position;
            Intent musicIntent = new Intent(this, AudioPlayService.class);
            musicIntent.putExtra("sortFlag", spMusicFlag);
            musicIntent.putExtra("position", mCurrentPosition);
            mConnection = new AudioServiceConnection();
            bindService(musicIntent, mConnection, Context.BIND_AUTO_CREATE);
            startService(musicIntent);
        }
    }

    /**
     * 在详情页面播放音乐回调
     *
     * @param position  播放位置
     * @param dataFlag  数据列表的标识
     * @param queryFlag 具体查询的条 ( 按 歌手 或 专辑查询 )
     */
    @Override
    public void startMusicServiceFlag(int position, int dataFlag, String queryFlag) {
        mCurrentPosition = position;
        Intent intent = new Intent(this, AudioPlayService.class);
        intent.putExtra("sortFlag", Constants.NUMBER_TEN);
        intent.putExtra("dataFlag", dataFlag);
        intent.putExtra("queryFlag", queryFlag);
        intent.putExtra("position", mCurrentPosition);
        AudioServiceConnection serviceConnection = new AudioServiceConnection();
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

    }

    private int getSpMusicFlag() {

        return SharePrefrencesUtil.getMusicDataListFlag(this);
    }

    /**
     * PagerAdapter回调
     */
    @Override
    public void onOpenMusicPlayDialogFag() {
        readyMusic();
    }


    private void openMusicPlayDialogFag() {
        mCompositeDisposable.add(RxView.clicks(mSmartisanControlBar)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(o -> readyMusic()));

    }

    private void readyMusic() {
        if (mMusicConfig) {
            Intent intent = new Intent(this, PlayActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("info", mCurrentMusicBean);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
            overridePendingTransition(R.anim.dialog_push_in, 0);
        } else {
            ToastUtil.showNoMusic(MusicActivity.this);
        }


    }

    private void initRxBusData() {
        //接收service发出的数据，时时更新播放歌曲 进度 歌名 歌手信息
        mCompositeDisposable.add(mBus.toObserverable(MusicBean.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(musicItem -> {
                    //将QQBar的释放掉
                    disposableQqLyric();
                    // 将MusicConfig设置为ture
                    SharePrefrencesUtil.setMusicConfig(MusicActivity.this);
                    mMusicConfig = true;
                    // 更新歌曲的信息
                    MusicActivity.this.perpareItem(musicItem);
                    //更新播放状态按钮
                    MusicActivity.this.updatePlayBtnStatus();
                    //初始化动画
                    mSmartisanControlBar.initAnimation();
                    //更新歌曲的进度
                    MusicActivity.this.updataProgress();

                }));
        /*
         position = bean.getPosition() 用来判断触发消息的源头，
         < 0 >表示是通知栏播放和暂停按钮发出，
         同时MusicPlayDialogFag在播放和暂停的时候也会发出通知并且type也是< 0 >，
         MuiscListActivity会接收到通知栏发出的播放状态的消息,用于控制播放按钮的显示状态
         < 1 >表示从通知栏打开音列表，即整个通知栏布局的监听。
         < 2 >表示在通知栏关闭通知栏
         < 3 > 切换列表数据
         < 4 >
         */
        mCompositeDisposable.add(mBus.toObserverable(MusicStatusBean.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MusicActivity.this::refreshBtnAndNotify));
        // 同步两个Bar上的数据
        mCompositeDisposable.add(mBus.toObserverable(QqBarUpdataBean.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qqBarBean -> mMusicItems = QueryMusicFlagListUtil.getMusicDataList(mMusicDao, qqBarBean.getMusicBean(), qqBarBean.getSortListFlag(), qqBarBean.getDataFlag(), qqBarBean.getQueryFlag())));
        mQqControlBar.setPagerData(mMusicItems);

    }


    private void refreshBtnAndNotify(MusicStatusBean bean) {
        switch (bean.getType()) {
            case 0:
                if (bean.isPlay()) {
                    audioBinder.pause();
                } else {
                    audioBinder.start();
                }
                mSmartisanControlBar.setAnimaorState(bean.isPlay);
                updatePlayBtnStatus();
                break;
            case 1:
                startActivity(new Intent(this, MusicActivity.class));
                break;
            case 2:
                finish();
                break;

            default:
                break;
        }
    }


    /**
     * 设置歌曲名和歌手名
     *
     * @param musicItem g
     */
    private void perpareItem(MusicBean musicItem) {
        mCurrentMusicBean = musicItem;
        checkCurrentSongIsFavorite(mCurrentMusicBean, mQqControlBar, mSmartisanControlBar);

        //更新音乐标题
        String songName = mCurrentMusicBean.getTitle();
        mSmartisanControlBar.setSongName(songName);
        //更新歌手名称
        String artistName = mCurrentMusicBean.getArtist();
        mSmartisanControlBar.setSingerName(artistName);
        //设置专辑
        String albumUri = StringUtil.getAlbulm(mCurrentMusicBean.getAlbumId()).toString();
        mSmartisanControlBar.setAlbulmUrl(albumUri);
        mQqControlBar.setPagerData(mMusicItems);
        mQqControlBar.setPagerCurrentItem(mCurrentMusicBean.getCureetPosition());
//         加载歌词的List，
        mLyricList = LyricsUtil.getLyricList(mCurrentMusicBean.getTitle(), mCurrentMusicBean.getArtist());
        if (isShowQqBar) {
            setQqPagerLyric();
        }
    }

    private void updataProgress() {
        if (audioBinder != null && audioBinder.isPlaying()) {
            if (mDisposableProgresse == null) {
                int duration = audioBinder.getDuration();
                mSmartisanControlBar.setMaxProgress(duration);
                mQqControlBar.setMaxProgress(duration);
                mDisposableProgresse = Observable.interval(0, 2800, TimeUnit.MICROSECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            mSmartisanControlBar.setSongProgress(audioBinder.getProgress());
                            mQqControlBar.setProgress(audioBinder.getProgress());
                        });
            }

        }
    }


    private void updatePlayBtnStatus() {
        //根据当前播放状态设置图片
        mSmartisanControlBar.updatePlayBtnStatus(audioBinder.isPlaying());
        mQqControlBar.updatePlayButtonState(audioBinder.isPlaying());

//        更新通知栏的按钮状态
//        MusicNoification.updatePlayBtn(audioBinder.isPlaying());
    }


    /**
     * 切换当前播放状态
     * mPlayState  将音乐的播放状态记录到本地，方便用户下次打开时进行UI初始化操作。
     * <p>
     * mPlayState = 1 ：表示用户点击暂停后，并退出音乐播放器。下次打开播放器的界面时，
     * 不会自动播放上一次记录的歌曲，需要点击播放按钮，才能播放上一次记录的歌曲。
     * <p>
     * mPlayState = 2 ：表示在播放时退出音乐播放器的界面，只是短暂的离开，但并没有退出程序(程序并没有被后台杀死)，
     * 下次打开播放器的界面时，继续自动播放当前的歌曲。
     */
    private void switchPlayState() {
        if (mPlayState == Constants.NUMBER_ONE) {
            LogUtil.d(" PlayState == 1 ==================");
            executStartServiceAndInitAnimation();
        } else if (mPlayState == Constants.NUMBER_TWO) {
            mPlayState = Constants.NUMBER_THRRE;
        } else {
            if (audioBinder == null) {
                ToastUtil.showNoMusic(this);
            } else if (audioBinder.isPlaying()) {
                // 当前播放  暂停
                audioBinder.pause();
                if (mDisposableProgresse != null) {
                    mDisposableProgresse.dispose();
                    mDisposableProgresse = null;
                }
            } else if (!audioBinder.isPlaying()) {
                // 当前暂停  播放
                audioBinder.start();
                updataProgress();
            }
            mSmartisanControlBar.setAnimaorState(audioBinder.isPlaying());
            //更新播放状态按钮
            updatePlayBtnStatus();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_title_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_titlebar_search:
                LogUtil.d("==================search");
//                startActivity(new Intent(this, SearchActivity.class));

//                setQqPagerLyric();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public static AudioPlayService.AudioBinder getAudioBinder() {
        return audioBinder;
    }

    private class AudioServiceConnection
            implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audioBinder = (AudioPlayService.AudioBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioBinder = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSmartisanControlBar.animatorOnPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioBinder != null) {
            mSmartisanControlBar.animatorOnResume(audioBinder.isPlaying());
            checkCurrentSongIsFavorite(mCurrentMusicBean, mQqControlBar, mSmartisanControlBar);
            updataProgress();
            if (isShowQqBar) {
                setQqPagerLyric();
            }
        }
    }

    @Override
    protected void headsetPullOut() {
        super.headsetPullOut();
        if (audioBinder != null && audioBinder.isPlaying()) {
            switchPlayState();
        }
    }

    @Override
    public void updataTitle(String toolbarTitle, boolean isShowDetail) {
        mDetailViewTitle = toolbarTitle;
        mTvMusicToolbarTitle.setText(toolbarTitle);
        mIsShowDetail = isShowDetail;
    }


    @Override
    public void onBackPressed() {
        int detailFlag = SharePrefrencesUtil.getDetailFlag(this);
        if (detailFlag > Constants.NUMBER_ZOER) {
            mBus.post(new DetailsFlagBean(detailFlag));
            mTvMusicToolbarTitle.setText(mTitleResourceId);
            mIsShowDetail = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindAudioService();
        handleAftermath();

    }

    public void unbindAudioService() {
        if (mConnection != null) {
            unbindService(mConnection);
            mConnection = null;
        }

    }

    private void handleAftermath() {
        if (mSmartisanControlBar != null) {
            mSmartisanControlBar.animatorStop();
        }
        if (audioBinder != null && !audioBinder.isPlaying()) {
            audioBinder.closeNotificaction();
        }
        if (audioBinder != null) {
            mPlayState = audioBinder.isPlaying() ? Constants.NUMBER_TWO : Constants.NUMBER_ONE;
            SharePrefrencesUtil.setMusicPlayState(this, mPlayState);
        }
//        stopService(new Intent(this, AudioPlayService.class));
    }


}
