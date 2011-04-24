package tappem.marguerite;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


import tappem.marguerite.BusLine;
import tappem.marguerite.BusStop;
import tappem.marguerite.MargueriteTransportation;
import android.app.Activity;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;




public class NextBus extends Activity implements OnClickListener{

	private MargueriteTransportation serverData;
	private String stopId;
	private BusStop currentBusStop;
	public static String uniqueId;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.next_bus);
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		 
		// Vibrate for 300 milliseconds
		v.vibrate(1000);
		checkVersion();
		final Button button = (Button) findViewById(R.id.refresh);
        button.setOnClickListener(this);
		
		uniqueId = SystemTools.getUniqueId(this);

		serverData = new MargueriteTransportation(uniqueId);
		if(savedInstanceState == null)
		{
			stopId = null;
		}else
		{
			stopId = (String) savedInstanceState.getSerializable(BusAdapter.KEY_ID);
		}
		resolveIntent(getIntent());

		if(stopId == null) {
			CharSequence text = "This bus stop is not currently registered in TAPPATS, we'll fix this as soon as we can!\n Sorry! :).";
			//if we could send the gps information it would be great here!! TODO
			int duration = Toast.LENGTH_LONG;

			Toast toast = Toast.makeText(this, text, duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			finish();
		}
		
		
		
		fillData();


	}
	void resolveIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		String tag = extras != null ? extras.getString(MargueriteTransportation.TAG_ID) : null;

		//System.out.println("RESOLVED!!!! " + tag);
		stopId = serverData.getStopIdFromTagId(tag);
		//stopId = serverData.getStopIdFromTagId("33");
		System.out.println("TAGID " + tag + "   stopID = " +  stopId);
		//stopId = "3";
	}
	
	private void checkVersion()
	{
		URL sourceUrl;
		try {
			sourceUrl = new URL("http://margueritenfc.heroku.com/version.xml");


			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							sourceUrl.openStream()));

			String version = in.readLine();
			in.close();

			System.out.println(version);
			
			if(!BusAdapter.VERSION.equals(version))
			{
				CharSequence text = "The app has a newer version, please update.";
				int duration = Toast.LENGTH_LONG;

				Toast toast = Toast.makeText(this, text, duration);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				
				
				
				//app is outdated
				String url = "http://www.tappem.com/stanfordtappats.htm";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				
				finish();
			}


		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected void fillData()
	{
		//System.out.println("HERE@!!");
		//query marguerite server and fill up the list view
		if(stopId != null){
			try {
				currentBusStop = serverData.getNextBusesWithStopId(stopId, uniqueId, "test");

				TextView stopName = (TextView) findViewById(R.id.stopNameText);
				stopName.setText(currentBusStop.getStopLabel());
				// Get all of the rows from the database and create the item list
				ListView l1 = (ListView) findViewById(R.id.buses);
				l1.setAdapter(new EfficientAdapter(this, currentBusStop));



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
	}

	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(BusAdapter.KEY_ID, stopId);

	}

	protected void onPause()
	{
		super.onPause();
		saveState();
		finish();

	}

	protected void onResume()
	{
		super.onResume();
		fillData();
	}

	private void saveState()
	{
		//TODO
	}




	private static class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private BusStop bus;
		private Context context;
		public EfficientAdapter(Context context, BusStop bus) {
			mInflater = LayoutInflater.from(context);
			this.bus = bus;
			this.context = context;
			System.out.println("IN ADAPTER: number of lines " + bus.getBusLines().size());
		}

		public int getCount() {
			return bus.getBusLines().size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.buses, null);
				holder = new ViewHolder();
				//	holder.busLine = (TextView) convertView.findViewById(R.id.busLine);
				holder.serviceTo = (TextView) convertView.findViewById(R.id.busServiceTo);
				holder.time = (TextView) convertView.findViewById(R.id.busTime);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			BusLine currentLine = bus.getBusLines().get(position);
			//	holder.busLine.setText(currentLine.getLineName());
			//System.out.println("FOR THE LIST: " + currentLine.toString());
			String imageName = currentLine.getIcon();
			ImageView busLineIcon = (ImageView) convertView.findViewById(R.id.busLineImage);
			String uri = "drawable/" + imageName;
			int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

			busLineIcon.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), imageResource));

			
			holder.serviceTo.setText(currentLine.getStatus());
			
			
			
			
			String minsToBus = currentLine.getEstimatedDepartureTime();
			String txt = "";
			
			try{
				int mins = Integer.parseInt(minsToBus);
				if(mins <= -1)
				{
					//System.out.println("Just Left");
					txt = "Just left";
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxgrey));
					
					
				}else if(mins <= 1)
				{
					txt = "Coming now";
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxgreen));
					
				}else if(mins <= 5)
				{
					txt = minsToBus;
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55);
					holder.time.setPadding(0, 0, 0, 0);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxorange));
					
				}else if(mins <= 15)
				{
					txt = minsToBus;
					holder.time.setPadding(0, 0, 0, 0);
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxred));
					
				}
				else if(mins <= 59)
				{
					//System.out.println("WEIRD");
					txt = minsToBus;
					holder.time.setPadding(0, 0, 0, 0);
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 55);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxgrey));
					
				}else
				{
					txt = "More than 1 hour";
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17);
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxgrey));
					
				}
				
			}catch(Exception e)
			{
				if(minsToBus.equals(MargueriteTransportation.noMoreBuses))
				{
					
					txt = minsToBus;
					
					holder.time.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17);
					
					
					holder.time.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.boxgrey));
					
				}
			}
			
			holder.time.setText(txt);


			
			
			

			return convertView;
		}

		static class ViewHolder {
			TextView busLine;
			TextView serviceTo;
			TextView time;
		}
	}




	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println("CLICKED");
    	fillData();
	}






}