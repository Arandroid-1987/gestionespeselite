/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arandroid.bilanciopersonale.qr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.arandroid.bplite.R;
import com.nineoldandroids.animation.ObjectAnimator;

public class QuickReturnFragment extends Fragment implements
		ObservableScrollView.Callbacks {

	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;
	
	private final static String TAB1 = "TAB 1";
	private final static String TAB2 = "TAB 2";
	private final static String TAB3 = "TAB 3";
	private final static String TAB11 = "TAB 11";
	private final static String TAB12 = "TAB 12";

	private View mQuickReturnView;
	private View mPlaceholderView;
	private ObservableScrollView mObservableScrollView;
	private ScrollSettleHandler mScrollSettleHandler = new ScrollSettleHandler();
	private int mMinRawY = 0;
	private int mState = STATE_ONSCREEN;
	private int mQuickReturnHeight;
	private int mMaxScrollY;
	private ObjectAnimator anim;
	private int translationY;
	
	private TabHost tabHost;
	private TabHost dataTabHost;
	
	private Activity context;

	public QuickReturnFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.list_spese_layout, container, false);
		
		context = getActivity();

		mObservableScrollView = (ObservableScrollView) rootView
				.findViewById(R.id.scroll_view);
		mObservableScrollView.setCallbacks(this);

		mQuickReturnView = rootView.findViewById(R.id.sticky);
		mPlaceholderView = rootView.findViewById(R.id.placeholder);

		mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						onScrollChanged(mObservableScrollView.getScrollY());
						mMaxScrollY = mObservableScrollView
								.computeVerticalScrollRange()
								- mObservableScrollView.getHeight();
						mQuickReturnHeight = mQuickReturnView.getHeight();
					}
				});
		
		tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		TabSpec spec1 = tabHost.newTabSpec(TAB1);
		spec1.setContent(R.id.tab1);
		spec1.setIndicator(context.getString(R.string.filtra_per_data));
		
		TabSpec spec2 = tabHost.newTabSpec(TAB2);
		spec2.setContent(R.id.tab2);
		spec2.setIndicator(context.getString(R.string.filtra_per_importo));
		
		TabSpec spec3 = tabHost.newTabSpec(TAB3);
		spec3.setContent(R.id.tab3);
		spec3.setIndicator(context.getString(R.string.filtra_per_tag));
		
		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);
		
		dataTabHost = (TabHost) rootView.findViewById(R.id.dataTabHost);
		dataTabHost.setup();
		
		TabSpec spec11 = tabHost.newTabSpec(TAB11);
		spec11.setContent(R.id.classico);
		spec11.setIndicator(context.getString(R.string.classico));
		
		TabSpec spec12 = tabHost.newTabSpec(TAB12);
		spec12.setContent(R.id.personalizzato);
		spec12.setIndicator(context.getString(R.string.personalizzato));
		
		dataTabHost.addTab(spec11);
		dataTabHost.addTab(spec12);

		return rootView;
	}

	@Override
	public void onScrollChanged(int scrollY) {
		scrollY = Math.min(mMaxScrollY, scrollY);

		mScrollSettleHandler.onScroll(scrollY);

		int rawY = mPlaceholderView.getTop() - scrollY;
		translationY = 0;

		switch (mState) {
		case STATE_OFFSCREEN:
			if (rawY <= mMinRawY) {
				mMinRawY = rawY;
			} else {
				mState = STATE_RETURNING;
			}
			translationY = rawY;
			break;

		case STATE_ONSCREEN:
			if (rawY < -mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			translationY = rawY;
			break;

		case STATE_RETURNING:
			translationY = (rawY - mMinRawY) - mQuickReturnHeight;
			if (translationY > 0) {
				translationY = 0;
				mMinRawY = rawY - mQuickReturnHeight;
			}

			if (rawY > 0) {
				mState = STATE_ONSCREEN;
				translationY = rawY;
			}

			if (translationY < -mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			break;
		}
		 anim = ObjectAnimator.ofFloat(mQuickReturnView, "translationY",
		 scrollY + translationY);
		 anim.setDuration(0);
		 anim.start();
	}

	@Override
	public void onDownMotionEvent() {
		mScrollSettleHandler.setSettleEnabled(false);
	}

	@Override
	public void onUpOrCancelMotionEvent() {
		mScrollSettleHandler.setSettleEnabled(true);
		mScrollSettleHandler.onScroll(mObservableScrollView.getScrollY());
	}

	private class ScrollSettleHandler extends Handler {
		private static final int SETTLE_DELAY_MILLIS = 100;

		private int mSettledScrollY = Integer.MIN_VALUE;
		private boolean mSettleEnabled;

		public void onScroll(int scrollY) {
			if (mSettledScrollY != scrollY) {
				// Clear any pending messages and post delayed
				removeMessages(0);
				sendEmptyMessageDelayed(0, SETTLE_DELAY_MILLIS);
				mSettledScrollY = scrollY;
			}
		}

		public void setSettleEnabled(boolean settleEnabled) {
			mSettleEnabled = settleEnabled;
		}

		@Override
		public void handleMessage(Message msg) {
			// Handle the scroll settling.
			if (STATE_RETURNING == mState && mSettleEnabled) {
				int mDestTranslationY;
				float amount1 = (Float) anim.getAnimatedValue("translationY");
				if (mSettledScrollY - amount1 > mQuickReturnHeight / 2) {
					mState = STATE_OFFSCREEN;
					mDestTranslationY = Math.max(mSettledScrollY
							- mQuickReturnHeight, mPlaceholderView.getTop());
				} else {
					mDestTranslationY = mSettledScrollY;
				}

				mMinRawY = mPlaceholderView.getTop() - mQuickReturnHeight
						- mDestTranslationY;
			}
			mSettledScrollY = Integer.MIN_VALUE; // reset
		}
	}
}
