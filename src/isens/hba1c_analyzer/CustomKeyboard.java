package isens.hba1c_analyzer;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CustomKeyboard {
	
	public final static int BACKSPACE = 8;
	
	public KeyboardView mKeyboardView;
	public Activity mActivity;

	public CustomKeyboard(Activity activity, int viewId, int layoutId) {

		mActivity = activity;
		mKeyboardView = (KeyboardView) mActivity.findViewById(viewId);
		mKeyboardView.setKeyboard(new Keyboard(mActivity, layoutId));
		mKeyboardView.setPreviewEnabled(false);
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
	}	
	
	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {
		
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			// TODO Auto-generated method stub
			
			View focusCurrent = mActivity.getWindow().getCurrentFocus();
	        EditText mEditText = (EditText) focusCurrent;
	        Editable mEditable = mEditText.getText();

	        int start = mEditText.getSelectionStart();
			
	        if(primaryCode == BACKSPACE) {
	        	
	        	if(mEditable != null && start > 0) mEditable.delete(start - 1, start);
	        	
	        } else mEditable.insert(start, Character.toString((char) primaryCode));
		}

		@Override
		public void onPress(int primaryCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRelease(int primaryCode) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onText(CharSequence text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeDown() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeLeft() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeRight() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void swipeUp() {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void ShowCustomKeyboard(View v) {
		
		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);
		
		if(v != null) ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
	
	public void HideCustomKeyboard() {
		
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}
	
	public void RegisterEditText(int viewId) {
		
		EditText mEditText = (EditText) mActivity.findViewById(viewId);
		
		mEditText.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				EditText mEditText = (EditText) v;
				mEditText.setInputType(InputType.TYPE_NULL);
				mEditText.onTouchEvent(event);
				mEditText.setSelection(mEditText.length());
				
				return true;
			}
		});
		
		mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				if(hasFocus) ShowCustomKeyboard(v);
				else HideCustomKeyboard();
			}
		});
	}
}
