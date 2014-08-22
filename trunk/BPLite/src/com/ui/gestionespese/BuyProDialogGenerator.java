package com.ui.gestionespese;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.arandroid.bplite.R;

public class BuyProDialogGenerator {

	public static Dialog create(final Context context) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.buy_pro_dialog, null);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(context.getString(R.string.acquista_ora));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(context.getString(R.string.acquista_ora));
		}

		builder.setPositiveButton(context.getString(R.string.acquista),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent myIntent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse("https://play.google.com/store/apps/details?id=com.arandroid.bilanciopersonale&hl=it"));
						context.startActivity(myIntent);
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(context.getString(R.string.chiudi),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.setView(v);
		dialog = builder.create();
		return dialog;
	}

}
