package com.arandroid.charts;

import java.text.DecimalFormat;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import com.arandroid.bplite.R;

import utils.NumberUtils;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class PieChartView {
	private static GraphicalView mChartView;
	private static DefaultRenderer renderer;

	public static View getView(final Context context,
			final CategorySeries mSeries) {
		Resources resources = context.getResources();
		int[] colors = new int[] { resources.getColor(R.color.pie_red), resources.getColor(R.color.background_main_item), Color.YELLOW,
				resources.getColor(R.color.pie_grey) };
		renderer = new DefaultRenderer();
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		renderer.setChartTitleTextSize(7*metrics.scaledDensity);
		renderer.setDisplayValues(true);
		renderer.setShowLabels(false);
		renderer.setLabelsTextSize(11*metrics.scaledDensity);
		renderer.setLegendTextSize(11*metrics.scaledDensity);
		renderer.setInScroll(true);
		renderer.setClickEnabled(true);
		renderer.setLabelsColor(Color.BLACK);
		int count = mSeries.getItemCount();
		int i = 0;
		while (i < count) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setChartValuesFormat(new DecimalFormat("0.00"));
			r.setColor(colors[i % colors.length]);
			renderer.addSeriesRenderer(r);
			i++;
		}
		mChartView = ChartFactory.getPieChartView(context, mSeries, renderer);
		mChartView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView
						.getCurrentSeriesAndPoint();
				if (seriesSelection == null) {
				} else {
					int pointIndex = seriesSelection.getPointIndex();
					String label = mSeries.getCategory(pointIndex);
					Toast.makeText(context,
							label + " = " + NumberUtils.getString(seriesSelection.getValue()) + "€",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		return mChartView;
	}

}
