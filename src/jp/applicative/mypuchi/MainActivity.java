package jp.applicative.mypuchi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;

import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    View.OnClickListener clickListener;

    final SimpleDateFormat  dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private ViewFlipper flipper;

	View[] views;
	int currentIndex = 0;

	Animation animInFromRight = inFromRightAnimation();
	Animation animOutToLeft = outToLeftAnimation();

	Animation animInFromLeft = inFromLeftAnimation();
	Animation animOutToRight = outToRightAnimation();


	Set<String> dateList;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dateList = loadDateList();

		gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        clickListener = new View.OnClickListener() {
			public void onClick(View view) {
				ImageButton button = (ImageButton) view;
				View parent = (View) button.getParent();
				ToggleButton toggle = (ToggleButton)parent.findViewById(R.id.toggleButton1);
				if(toggle.isChecked()){
					String dateText = (String) ((TextView)parent.findViewById(R.id.textView1)).getText();
					dateList.add(dateText);
					Vibrator vibrator = (Vibrator)getSystemService(Activity.VIBRATOR_SERVICE);
					vibrator.vibrate(400);
					refreshView(parent, dateText);
					saveDateList();
				}
				parent.refreshDrawableState();
			}
		};

        Date today = new Date();
        views = new View[]{
        	createPuchiView(today),
        	createPuchiView(addDate(today, 1))
        };

		flipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
		flipper.addView(views[0]);
		flipper.addView(views[1]);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void refreshView(View view, String dateText){
		ToggleButton toggle = (ToggleButton)view.findViewById(R.id.toggleButton1);
		ImageButton button = (ImageButton)view.findViewById(R.id.imageButton1);
		toggle.setChecked(false);

		if(dateText == null || !dateList.contains(dateText)){
			button.setImageResource(R.drawable.img_before_puchi);
			toggle.setEnabled(true);
		}else{
			button.setImageResource(R.drawable.img_after_puchi);
			toggle.setEnabled(false);
		}
		view.refreshDrawableState();
	}

	public View getCurrentView(){
		return views[currentIndex];
	}

	private int getPrepareNextView(int dir) throws ParseException{
		int idx = (views.length + currentIndex + dir) % views.length;
		View currentView = getCurrentView();
		View nextView = views[idx];
		TextView currentText = (TextView)currentView.findViewById(R.id.textView1);
		TextView nextText = (TextView)nextView.findViewById(R.id.textView1);
		Date date = dateFormat.parse((String) currentText.getText());
		String dateText = dateFormat.format(addDate(date,dir));
		nextText.setText(dateText);

		refreshView(nextView, dateText);

		return idx;
	}

	private View createPuchiView(Date date){
		View new_view = this.getLayoutInflater().inflate(R.layout.view_puchi, null);
		ImageButton button = (ImageButton)new_view.findViewById(R.id.imageButton1);
		button.setOnTouchListener(gestureListener);
		button.setOnClickListener(clickListener);
		TextView text = (TextView)new_view.findViewById(R.id.textView1);
		String dateText = dateFormat.format(date);
		text.setText(dateText);

		refreshView(new_view, dateText);

		return new_view;
	}

	private Date addDate(Date date, int offset_day){
		if(offset_day != 0){
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, offset_day);
			date = cal.getTime();
		}
		return date;
	}

	private Set<String> loadDateList(){
		try{
			ObjectInputStream ost = new ObjectInputStream(this.openFileInput("datelist"));
			return (Set<String>) ost.readObject();
		}catch(Exception e){
			Log.e("loadDateList","Failed to load a file.",e);
			return new HashSet<String>();
		}
	}

	private void  saveDateList(){
		try{
			ObjectOutputStream ost = new ObjectOutputStream(this.openFileOutput("datelist", Context.MODE_PRIVATE));
			ost.writeObject(dateList);
		}catch(Exception e){
			Log.e("loadDateList","Failed to save a file.",e);
		}
	}

	private Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	private Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoLeft.setDuration(500);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		return inFromLeft;
	}
	private Animation outToRightAnimation() {
		Animation outtoRight = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		);
		outtoRight.setDuration(500);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.reset_date_list : {
				dateList = new HashSet<String>();
				saveDateList();
				refreshView(getCurrentView() , null);
				return true;
			}
		}
		return false;
	}


    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	flipper.setInAnimation(animInFromRight);
                	flipper.setOutAnimation(animOutToLeft);
                	currentIndex = getPrepareNextView(1);
                	flipper.showNext();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	flipper.setInAnimation(animInFromLeft);
                	flipper.setOutAnimation(animOutToRight);
                	currentIndex = getPrepareNextView(-1);
                	flipper.showPrevious();
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }


    }

}
