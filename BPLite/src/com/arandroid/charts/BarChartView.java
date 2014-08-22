package com.arandroid.charts;

import java.text.DecimalFormat;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.arandroid.bplite.R;
import com.dao.SettingsDao;
import com.db.DatabaseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.DisplayMetrics;
import android.view.View;

public class BarChartView {

	public static View getView(final Context context,
			final CategorySeries mSeries, final XYMultipleSeriesDataset dataset, String [] xLabels) {
		Resources resources = context.getResources();
		int[] colors = new int[] { resources.getColor(R.color.pie_red),
				resources.getColor(R.color.background_main_item), Color.YELLOW,
				resources.getColor(R.color.pie_grey) };
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(dataset.getSeriesAt(0).getItemCount() + 0.5);
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(max(dataset) + 5);
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		renderer.setLabelsTextSize(7 * metrics.scaledDensity);
		for (int i = 0; i < dataset.getSeriesAt(0).getItemCount(); i++) {
			renderer.addXTextLabel(i+1, xLabels[i]);
		}
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setBarSpacing(0.5);
		renderer.setXTitle(context.getString(R.string.periodi));
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		String[] currency = SettingsDao.getCurrency(db);
		db.close();
		renderer.setYTitle(currency[1]);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.GRAY);
		renderer.setXLabels(0);
		int count = mSeries.getItemCount();
		int i = 0;
		while (i < count) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setChartValuesFormat(new DecimalFormat("0.00"));
			r.setColor(colors[i % colors.length]);
			renderer.addSeriesRenderer(r);
			i++;
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(context,
				dataset, renderer, Type.DEFAULT);
		return mChartView;
	}

	private static double max(final XYMultipleSeriesDataset dataset) {
		double max = 0;
		for (int i = 0; i < dataset.getSeriesCount(); i++) {
			XYSeries serie = dataset.getSeriesAt(i);
			for (int j = 0; j < serie.getItemCount(); j++) {
				double value = serie.getY(j);
				if (value > max) {
					max = value;
				}
			}
		}
		return max;
	}

}
