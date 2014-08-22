package utils;

import com.arandroid.bplite.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class KeyButton extends LinearLayout {
	private Button button;
	private ImageButton imageButton;
	private int value;
	private boolean useText;
	private String text;
	private Drawable drawable;
	private Context context;
	private int backgroundResourceInt;
	private int textColorResourceInt;
	private int width = 80;
	private int height = 50;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public KeyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public KeyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.KeyButton, 0, 0);
		try {
			value = a.getInt(R.styleable.KeyButton_value, 0);
			useText = a.getBoolean(R.styleable.KeyButton_useText, true);
			if (useText) {
				text = a.getString(R.styleable.KeyButton_text);
				textColorResourceInt = a.getColor(R.styleable.KeyButton_myTextColor, R.color.black);
			} else {
				drawable = a.getDrawable(R.styleable.KeyButton_image);
			}
		} finally {
			a.recycle();
		}
		backgroundResourceInt = R.drawable.key_button;
		createLayout();
	}

	public void createLayout() {
		removeAllViews();
		float densityMultiplier;
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		densityMultiplier = metrics.density;
		int mwidth = (int) (width * densityMultiplier);
		int mheight = (int) (height * densityMultiplier);
		LayoutParams params = new LayoutParams(mwidth, mheight);
		params.leftMargin = 5;
		params.topMargin = 5;
		if (useText) {
			button = new Button(context);
			button.setText(text);
			button.setLayoutParams(params);
			button.setBackgroundResource(backgroundResourceInt);
			button.setTextColor(textColorResourceInt);
			button.setTextSize(26);
			addView(button);
		} else {
			imageButton = new ImageButton(context);
			imageButton.setLayoutParams(params);
			imageButton.setImageDrawable(drawable);
			imageButton.setBackgroundResource(backgroundResourceInt);
			addView(imageButton);
		}
	}
	
	public boolean isUseText() {
		return useText;
	}

	public void setUseText(boolean useText) {
		this.useText = useText;
	}
	
	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public KeyButton(Context context) {
		this(context, null);
	}
	
	public void setBackgroundResourceInt(int backgroundResourceInt) {
		this.backgroundResourceInt = backgroundResourceInt;
	}

	public int getValue() {
		return value;
	}
	
	public void setTextColorResourceInt(int textColorResourceInt) {
		this.textColorResourceInt = textColorResourceInt;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

}
