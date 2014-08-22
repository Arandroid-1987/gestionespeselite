package com.arandroid.bilanciopersonale.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.FileChooser;
import com.arandroid.bplite.R;
import com.db.DatabaseHandler;

public class DBManagerFragment extends Fragment {

	private final static int FILE_CHOOSER_ACTIVITY = 0;

	private View rootView;
	private Activity context;
	private AlertDialog customDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		rootView = inflater
				.inflate(R.layout.dbmanager_layout, container, false);

		View backup = rootView.findViewById(R.id.backup);
		backup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DatabaseHandler dbHandler = DatabaseHandler
						.getInstance(context);
				SQLiteDatabase db = dbHandler.getWritableDatabase();
				dbHandler.backup(db);
				db.close();
			}
		});

		View restore = rootView.findViewById(R.id.restore);
		restore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FileChooser.class);
				startActivityForResult(intent, FILE_CHOOSER_ACTIVITY);
			}
		});

		View reset = rootView.findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createAndShowAreYouSureDialog();
			}
		});

		return rootView;
	}

	protected void createAndShowAreYouSureDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				resetDB();
				customDialog.dismiss();
			}
		});

		builder.setNegativeButton(getString(R.string.annulla),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						customDialog.dismiss();
					}
				});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			LayoutInflater inflater = getLayoutInflater(null);
			View title = inflater.inflate(R.layout.dialog_title,
					(ViewGroup) rootView.findViewById(R.id.titleLayout));
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getResources().getString(
					R.string.sei_sicuro));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getResources().getString(
					R.string.sei_sicuro));
		}

		customDialog = builder.create();
		customDialog.show();
	}

	protected void resetDB() {
		DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = dbHandler.getWritableDatabase();
		dbHandler.clear(db);
		db.close();
		Toast.makeText(context, getString(R.string.database_resettato),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CHOOSER_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				String file_path = data.getStringExtra("file_path");
				DatabaseHandler dbHandler = DatabaseHandler
						.getInstance(context);
				SQLiteDatabase db = dbHandler.getWritableDatabase();
				dbHandler.restore(db, file_path);
				onResume();
			} else {
				Toast.makeText(context,
						getString(R.string.attenzione_nessun_file_selezionato),
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
