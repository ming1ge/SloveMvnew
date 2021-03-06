package com.ming.slove.mvnew.shop.shoptab1.books;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bumptech.glide.Glide;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.melnykov.fab.FloatingActionButton;
import com.ming.slove.mvnew.R;
import com.ming.slove.mvnew.api.other.OtherApi;
import com.ming.slove.mvnew.app.APPS;
import com.ming.slove.mvnew.app.ThemeHelper;
import com.ming.slove.mvnew.common.base.BaseRecyclerViewAdapter;
import com.ming.slove.mvnew.common.base.LazyLoadFragment;
import com.ming.slove.mvnew.common.utils.MyItemDecoration;
import com.ming.slove.mvnew.model.bean.BookList;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 图书列表
 */
public class BooksTab1Fragment extends LazyLoadFragment {

    @Bind(R.id.m_x_recyclerview)
    XRecyclerView mXRecyclerView;
    @Bind(R.id.m_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.content_empty)
    TextView contentEmpty;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    private BooksAdapter mAdapter;
    private List<BookList.DataBean.ListBean> mList = new ArrayList<>();

    final private static int PAGE_SIZE = 10;
    private int page = 1;
    private final int REQUEST_CODE = 12331;

    @Override
    public int getLayout() {
        return R.layout.fragment_books;
    }

    @Override
    public void initViews(View view) {
        config();
        // 刷新时，指示器旋转后变化的颜色
        int themeColor = ThemeUtils.getColorById(getContext(), R.color.theme_color_primary);
        mRefreshLayout.setColorSchemeColors(themeColor);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.setItem(null);
                mList.clear();
                page = 1;
                initData(page);
            }
        });
    }

    @Override
    public void loadData() {
        initData(page);
    }

    private void config() {
        //设置fab
        fab.attachToRecyclerView(mXRecyclerView);//fab随recyclerView的滚动，隐藏和出现
        int themeColor = ThemeUtils.getColorById(getContext(), R.color.theme_color_primary);
        int themeColor2 = ThemeUtils.getColorById(getContext(), R.color.theme_color_primary_dark);
        fab.setColorNormal(themeColor);//fab背景颜色
        fab.setColorPressed(themeColor2);//fab点击后背景颜色
        fab.setColorRipple(themeColor2);//fab点击后涟漪颜色
        //设置recyclerview布局
        mXRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mXRecyclerView.addItemDecoration(new MyItemDecoration(getContext()));//添加分割线
        mXRecyclerView.setHasFixedSize(true);//保持固定的大小,这样会提高RecyclerView的性能
        mXRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置Item增加、移除动画


        //设置adapter
        mAdapter = new BooksAdapter();
        mXRecyclerView.setAdapter(mAdapter);

        mXRecyclerView.setPullRefreshEnabled(false);
        mXRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallRotate);
        mXRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
            }

            @Override
            public void onLoadMore() {
                initData(++page);
                mXRecyclerView.loadMoreComplete();
            }
        });
    }

    private void initData(int page) {
        String vid = Hawk.get(APPS.MANAGER_VID);
        OtherApi.getService()
                .get_BookListTab1(vid, page, PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BookList>() {
                    @Override
                    public void onCompleted() {
                        mRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BookList bookList) {
                        mList.addAll(bookList.getData().getList());
                        if (mList.isEmpty()) {
                            contentEmpty.setVisibility(View.VISIBLE);
                            contentEmpty.setText(R.string.empty_book_tab1);
                        } else {
                            contentEmpty.setVisibility(View.GONE);
                        }
                        mAdapter.setItem(mList);
                    }
                });
    }

    @OnClick(R.id.fab)
    public void onClick() {
//        Toast.makeText(mActivity, "添加图书", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext(), AddBookActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mAdapter.setItem(null);
                mList.clear();
                page = 1;
                initData(page);
            }
        }
    }

    static class BooksAdapter extends BaseRecyclerViewAdapter<BookList.DataBean.ListBean, BooksAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_tab1_books, parent, false);
            return new ViewHolder(mView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Context mContext = holder.itemView.getContext();
            BookList.DataBean.ListBean data = mList.get(position);
            //书籍封面图片
            String imageUrl = APPS.BASE_URL + data.getPic_s();
            Glide.with(mContext).load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.shape_picture_background)
                    .into(holder.imgBook);
            //显示书号
            holder.tvNum.setText("书号：  " + data.getBnum());
            //显示书名
            holder.tvName.setText("书名：《" + data.getBname() + "》");
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            @Bind(R.id.img_book)
            ImageView imgBook;
            @Bind(R.id.tv_num)
            TextView tvNum;
            @Bind(R.id.tv_name)
            TextView tvName;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
