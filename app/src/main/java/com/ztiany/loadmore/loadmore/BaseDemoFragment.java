package com.ztiany.loadmore.loadmore;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ztiany.adapter.WrapperAdapter;
import com.ztiany.loadmore.BaseAdapter;
import com.ztiany.loadmore.BaseLayoutFragment;
import com.ztiany.loadmore.DensityUtils;
import com.ztiany.loadmore.ILoadMore;
import com.ztiany.loadmore.OnLoadMoreListener;
import com.ztiany.loadmore.R;
import com.ztiany.loadmore.StateViewFactory;
import com.ztiany.loadmore.ViewHolder;
import com.ztiany.state.IState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * @author Ztiany
 *         email 1169654504@qq.com & ztiany3@gmail.com
 *         date 2015-12-18 11:18
 *         description
 *         vsersion
 */
public abstract class BaseDemoFragment extends BaseLayoutFragment {

    private BaseAdapter<String, ViewHolder<String>> mRecyclerAdapter;
    private static final String TAG = BaseDemoFragment.class.getSimpleName();


    private boolean mHasMore = true;
    private boolean mIsFail;
    private boolean mIsPause = false;

    private int count = 20;

    @Bind(R.id.fragment_recycler_rv)
    protected RecyclerView mRecyclerView;
    @Bind(R.id.fragment_recycler_ptr)
    protected PtrClassicFrameLayout mPtrClassicFrameLayout;

    private List<String> mData;
    protected ILoadMore mLoaderManager;
    private IState mStateManager;


    @OnClick(value = {R.id.frag_show_option})
    public void onButtonClick(View v) {

        PopupMenu pop = new PopupMenu(getContext(), v);
        Menu menu = pop.getMenu();

        String pauseString = mIsPause ? "no puase" : "pause";

        menu.add(Menu.NONE, 1, 0, "next time fail");
        menu.add(Menu.NONE, 2, 1, "next time no more");
        menu.add(Menu.NONE, 3, 2, pauseString);
        menu.add(Menu.NONE, 4, 3, "next time normal");
        menu.add(Menu.NONE, 5, 4, "Content");
        menu.add(Menu.NONE, 6, 5, "Loading");
        menu.add(Menu.NONE, 7, 6, "Fail");
        menu.add(Menu.NONE, 8, 7, "Empty");

        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                Log.d(TAG, "itemId:" + itemId);
                switch (itemId) {
                    case 1: {
                        mIsFail = true;
                        break;
                    }
                    case 2: {
                        mHasMore = false;
                        break;
                    }
                    case 3: {
                        mIsPause = !mIsPause;
                        mLoaderManager.pause(mIsPause);
                        break;
                    }
                    case 4: {
                        mIsFail = false;
                        mHasMore = true;
                        mLoaderManager.loadCompleted(true);
                        break;
                    }
                    case 5: {
                        mStateManager.content();
                        break;
                    }
                    case 6: {
                        mStateManager.loading();
                        break;
                    }
                    case 7: {
                        mStateManager.fail();
                        break;
                    }
                    case 8: {
                        mStateManager.empty();
                        break;
                    }
                }
                return true;
            }
        });
        pop.setGravity(Gravity.CENTER);
        pop.show();
    }



    @Override
    protected int provideLayoutRes() {
        return R.layout.fragment_recycler_load;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initAdapter();

        RecyclerView.LayoutManager linearLayoutManager = getLayoutManager();
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int px = DensityUtils.dip2px(getContext(), 10);
                outRect.bottom = px;
                super.getItemOffsets(outRect, view, parent, state);
            }
        });
        WrapperAdapter wrapperAdapter = new WrapperAdapter(mRecyclerAdapter);
        mRecyclerView.setAdapter(wrapperAdapter);


        mLoaderManager = wrapperAdapter.getLoadMoreManager();
        onCreateLoaderManager(mLoaderManager);
        mStateManager = wrapperAdapter.getStateManager();

        setOnLoadMoreListener();
        setStateView();

        mPtrClassicFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                if (mLoaderManager.isLoadingMore()) {
                    frame.refreshComplete();
                    return;
                }

                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                        Toast.makeText(getContext(), "刷新完毕", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return super.checkCanDoRefresh(frame, mRecyclerView, header);
            }
        });

    }

    protected void onCreateLoaderManager(ILoadMore loaderManager) {

    }

    protected void setStateView() {
        mStateManager.setStateViewFactory(new StateViewFactory() {
            @Override
            public View onCreateEmptyView(ViewGroup parent) {
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_empty, parent, false);
            }

            @Override
            public View onCreateLoadingView(ViewGroup parent) {
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loading, parent, false);

            }

            @Override
            public View onCreateFailView(ViewGroup parent) {
                return LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_error, parent, false);

            }
        });
    }

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    private void setOnLoadMoreListener() {

        mLoaderManager.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {


                mPtrClassicFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsFail) {
                            mLoaderManager.loadFail();
                            return;
                        }

                        if (!mHasMore) {
                            mLoaderManager.loadCompleted(false);
                            return;
                        }

                        mRecyclerAdapter.addAll(Arrays.asList(
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++,
                                "新来的Item" + count++
                        ));

                        mLoaderManager.loadCompleted(true);
                    }
                }, 1000);
            }
        });

    }

    private void initAdapter() {

        mRecyclerAdapter = new BaseAdapter<String, ViewHolder<String>>(getContext(), mData) {
            @Override
            public ViewHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
                View inflate = mLayoutInflater.inflate(R.layout.item, parent, false);
                return new ViewHolder<String>(inflate) {
                    private TextView mTextView;

                    @Override
                    protected void findViews() {
                        mTextView = findView(R.id.item_tv);
                    }

                    @Override
                    public void bindData(final String data) {
                        mTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
                                ;
                            }
                        });
                        onBindData(mTextView, data);
                    }


                };
            }

            @Override
            public void onBindViewHolder(ViewHolder<String> viewHolder, final int position) {
                viewHolder.bindData(mData.get(position));
            }
        };
    }


    private void initData() {
        mData = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            mData.add("我是Item " + i);
        }
    }


    protected void onBindData(TextView textView, String data) {
        textView.setText(data);
    }
}