package utils;

import com.arandroid.bplite.R;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
	private Activity context;
	private String okText;
	private String noText;
	
	public CustomToast(Activity context, String okText, String noText) {
		this.context = context;
		this.okText = okText;
		this.noText = noText;
	}

	public Toast getCorrectToast() {
		LayoutInflater inflater = context.getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) context.findViewById(R.id.custom_toast_layout_id));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.correct);

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(this.okText);
		Toast toast = new Toast(context.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		return toast;
	}

	public Toast getErrorToast() {
		LayoutInflater inflater = context.getLayoutInflater();
		View layout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) context.findViewById(R.id.custom_toast_layout_id));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.error);

		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(noText);
		Toast toast = new Toast(context.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		return toast;
	}

}
