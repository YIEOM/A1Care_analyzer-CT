package isens.hba1c_analyzer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ErrorPopup {

	public Activity activity;
	public Context context;
	public int layoutid;
	
	public View popupView;
	public PopupWindow popupWindow;
	public RelativeLayout hostLayout;
	
	public TextView errorBtnText;
	public Button errorBtn;
	
	
	public ErrorPopup(Activity activity, Context context, int layoutid) {
		
		this.activity = activity;
		this.context = context;
		this.layoutid = layoutid;
	}
	
	public void ErrorBtnDisplay(int str) {
		
		hostLayout = (RelativeLayout)activity.findViewById(layoutid);
		popupView = View.inflate(context, R.layout.errorbtnpopup, null);
		popupWindow = new PopupWindow(popupView, 800, 480, true);
	
		errorBtnText = (TextView) popupView.findViewById(R.id.errortext);
		errorBtnText.setText(str);
		
		errorBtn = (Button) popupView.findViewById(R.id.errorbtn);
		errorBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				popupWindow.dismiss();
			}
		});
		
		hostLayout.post(new Runnable() {
			public void run() {
		
				popupWindow.showAtLocation(hostLayout, Gravity.CENTER, 0, 0);
				popupWindow.setAnimationStyle(0);
			}
		});
	}
}
