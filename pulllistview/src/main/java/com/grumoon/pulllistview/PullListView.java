package com.grumoon.pulllistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PullListView extends ListView implements OnScrollListener {

	// 下拉刷新接口
	public interface OnRefreshListener {
		public void onRefresh();
	}

	// 加载更多接口
	public interface OnGetMoreListener {
		public void onGetMore();
	}

	private static final String TAG = PullListView.class.getSimpleName();

	// 完成状态（初始状态）
	private final static int NONE = 1;

	// 下拉刷新状态
	private final static int PULL_TO_REFRESH = 2;

	// 松开刷新状态
	private final static int RELEASE_TO_REFRESH = 3;

	// 正在刷新状态
	private final static int REFRESHING = 4;

	// 实际的padding的距离与界面上偏移距离的比例（迟滞比例）
	private final static int RATIO = 2;

	private LayoutInflater inflater;

	// 下拉刷新视图（头部视图）
	private ViewGroup headView;

	// 下拉刷新文字
	private TextView tvHeadTitle;

	// 下拉图标
	private ImageView ivHeadArrow;

	// 刷新中忙碌框
	private ProgressBar pbHeadRefreshing;

	// 加载更多视图（底部视图）
	private View footView;

	// 加载更多文字
	private TextView tvFootTitle;

	// 加载更多忙碌框
	private ProgressBar pbFootRefreshing;

	// 旋转动画
	private RotateAnimation animation;
	// 反向旋转动画
	private RotateAnimation reverseAnimation;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;

	// 头部高度
	private int headViewHeight;

	// 用于记录滑动开始时候的Y值
	private int startY;

	// ListView中第一个可见item的序号
	private int firstVisiableItemIndex;

	// 状态
	private int state;

	/**
	 * 判断下拉刷新状态是由done状态转变而来，还是由松开刷新状态转变而来<br/>
	 * true 表示由松开刷新状态转变而来
	 */
	private boolean isFromReleaseToRefresh;

	private OnRefreshListener refreshListener;
	private OnGetMoreListener getMoreListener;

	private boolean canRefresh;
	private boolean isGetMoreing = false;

	public PullListView(Context context) {
		super(context);
		init(context);
	}

	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {

		initAnimation();

		setOnScrollListener(this);


		inflater = LayoutInflater.from(context);

		/**
		 * 头部
		 */
		headView = (LinearLayout) inflater.inflate(R.layout.pull_list_view_head, null);
		ivHeadArrow = (ImageView) headView.findViewById(R.id.iv_head_arrow);
		pbHeadRefreshing = (ProgressBar) headView.findViewById(R.id.pb_head_refreshing);
		tvHeadTitle = (TextView) headView.findViewById(R.id.tv_head_title);
		measureView(headView);
		headViewHeight = headView.getMeasuredHeight();
		headView.setPadding(0, -headViewHeight, 0, 0);
		headView.invalidate();
		addHeaderView(headView, null, false);

		/**
		 * 底部
		 */
		footView = inflater.inflate(R.layout.pull_list_view_foot, null);
		tvFootTitle = (TextView) footView.findViewById(R.id.tv_foot_title);
		pbFootRefreshing = (ProgressBar) footView.findViewById(R.id.pb_foot_refreshing);
		footView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMore();
			}
		});

		state = NONE;
		canRefresh = false;
	}

	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		// 旋转
		animation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(300);
		reverseAnimation.setFillAfter(true);
	}

	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
		firstVisiableItemIndex = firstVisiableItem;
	}

	public void onScrollStateChanged(AbsListView arg0, int arg1) {

	}

	public boolean onTouchEvent(MotionEvent event) {
		// 不可下拉刷新
		if (!canRefresh) {
			return super.onTouchEvent(event);
		}

		int action = event.getAction();
		int tempY = (int) event.getRawY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 只有在滑动到第一个item才处理
			if (firstVisiableItemIndex == 0 && !isRecored) {
				isRecored = true;
				startY = tempY;
				Log.v(TAG, "在按下时候记录初始位置");
			}
			break;

		case MotionEvent.ACTION_UP:
			if (state == PULL_TO_REFRESH) {
				state = NONE;
				changeHeaderViewByState();
			} else if (state == RELEASE_TO_REFRESH) {
				state = REFRESHING;
				changeHeaderViewByState();
				refresh();
			}
			isRecored = false;
			isFromReleaseToRefresh = false;
			break;

		case MotionEvent.ACTION_MOVE:
			// 在滑动时候记下初始位置
			if (!isRecored && firstVisiableItemIndex == 0) {
				isRecored = true;
				startY = tempY;
				Log.v(TAG, "在滑动时候记录初始位置");
			}

			if (isRecored && state != REFRESHING) {
				int deltaY = tempY - startY;
				// 初始状态下
				if (state == NONE) {
					if (deltaY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();
						Log.v(TAG, "由初始状态转变为下拉刷新状态");
					}
				}
				// 还没有到达显示松开刷新的时候PULL_TO_REFRESH状态
				else if (state == PULL_TO_REFRESH) {

					setSelection(0);

					// 下拉到可以进入RELEASE_TO_REFRESH的状态
					if (deltaY / RATIO >= headViewHeight) {
						state = RELEASE_TO_REFRESH;
						isFromReleaseToRefresh = true;
						changeHeaderViewByState();
						Log.v(TAG, "下拉刷新转变到松开刷新状态");
					}
					// 上推到顶了
					else if (deltaY <= 0) {
						state = NONE;
						changeHeaderViewByState();
						Log.v(TAG, "下拉刷新转变到初始状态");
					} else {
						headView.setPadding(0, -headViewHeight + deltaY / RATIO, 0, 0);
					}
				}
				// 可以松手去刷新了
				else if (state == RELEASE_TO_REFRESH) {

					setSelection(0);

					// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
					if ((deltaY / RATIO < headViewHeight) && deltaY > 0) {
						state = PULL_TO_REFRESH;
						changeHeaderViewByState();

						Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
					}
					// 一下子推到顶了
					else if (deltaY <= 0) {
						state = NONE;
						changeHeaderViewByState();

						Log.v(TAG, "由松开刷新状态转变到初始状态");
					} else {
						headView.setPadding(0, -headViewHeight + deltaY / RATIO, 0, 0);
					}
				}
			}

			break;
		}

		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case NONE:
			headView.setPadding(0, -1 * headViewHeight, 0, 0);
			pbHeadRefreshing.setVisibility(View.GONE);
			ivHeadArrow.clearAnimation();
			ivHeadArrow.setImageResource(R.drawable.pull_list_view_progressbar_bg);
			tvHeadTitle.setText("下拉刷新");
			break;

		case PULL_TO_REFRESH:
			pbHeadRefreshing.setVisibility(View.GONE);
			tvHeadTitle.setVisibility(View.VISIBLE);
			ivHeadArrow.clearAnimation();
			ivHeadArrow.setVisibility(View.VISIBLE);
			tvHeadTitle.setText("下拉刷新");
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isFromReleaseToRefresh) {
				isFromReleaseToRefresh = false;
				ivHeadArrow.clearAnimation();
				ivHeadArrow.startAnimation(reverseAnimation);
			}
			break;

		case RELEASE_TO_REFRESH:
			ivHeadArrow.setVisibility(View.VISIBLE);
			pbHeadRefreshing.setVisibility(View.GONE);
			tvHeadTitle.setVisibility(View.VISIBLE);

			ivHeadArrow.clearAnimation();
			ivHeadArrow.startAnimation(animation);

			tvHeadTitle.setText("松开刷新");

			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			pbHeadRefreshing.setVisibility(View.VISIBLE);
			ivHeadArrow.clearAnimation();
			ivHeadArrow.setVisibility(View.GONE);
			tvHeadTitle.setText("正在刷新...");

			break;

		}
	}

	// 刷新
	private void refresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}

		if (footView != null) {
			footView.setVisibility(View.VISIBLE);
			pbFootRefreshing.setVisibility(View.GONE);
			tvFootTitle.setText("加载更多");
			isGetMoreing = false;
		}
	}

	// 加载更多
	private void getMore() {
		if (isGetMoreing) {
			return;
		}

		if (getMoreListener != null) {
			pbFootRefreshing.setVisibility(View.VISIBLE);
			tvFootTitle.setText("正在加载...");
			getMoreListener.onGetMore();
			isGetMoreing = true;
		}
	}

	// 测量视图
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setCanRefresh(boolean canRefresh) {
		this.canRefresh = canRefresh;
	}

	/**
	 * 代码触发下拉刷新操作<br/>
	 * 多用于首次进入页面的加载
	 */
	public void performRefresh() {
		state = REFRESHING;
		changeHeaderViewByState();
		refresh();
	}

	/**
	 * 设置下拉刷新监听器
	 * 
	 * @param refreshListener
	 */
	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		canRefresh = true;
	}

	/**
	 * 设置加载更多监听器
	 * 
	 * @param getMoreListener
	 */
	public void setOnGetMoreListener(OnGetMoreListener getMoreListener) {
		this.getMoreListener = getMoreListener;
		this.addFooterView(footView);
	}

	/**
	 * 下拉刷新完成
	 */
	public void refreshComplete() {
		state = NONE;
		changeHeaderViewByState();
	}

	/**
	 * 加载更多完成
	 */
	public void getMoreComplete() {
		pbFootRefreshing.setVisibility(View.GONE);
		tvFootTitle.setText("加载更多");
		isGetMoreing = false;
	}

	/**
	 * 设置没有更多的数据了<br/>
	 * 不再显示加载更多按钮
	 */
	public void setNoMore() {
		if (footView != null) {
			footView.setVisibility(View.GONE);
		}
	}

	/**
	 * 显示加载更多按钮
	 */
	public void setHasMore() {
		if (footView != null) {
			footView.setVisibility(View.VISIBLE);
		}
	}

}
