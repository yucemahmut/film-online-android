package pack.filmonline;

import io.pen.bluepixel.filmonline.R;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;

public class PlaylistActivity extends ListActivity {
	private String playlistID = "";
	private final String apiKey = "AI39si6qtLTRcvw1camffVD8vogTIgvaeWsCBa5zPwJjWK6D8-WJpldFaTk3A_D8GwCbDipFuP0AYum_kfV0eBYmEL0HEfMCrg";
	private final String appName = "Film OnLine";
	private final YouTubeService service = new YouTubeService(appName, apiKey);
	private List<String> TITLES = new ArrayList<String>();
	private List<String> URLS = new ArrayList<String>();
	public static final String TITLES_LABEL = "titles";
	public static final String URLS_LABEL = "urls";
	private ProgressDialog progDailog;
	protected InitTask initTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		try {
			TITLES = (List) savedInstanceState.getSerializable(TITLES_LABEL);
			URLS = (List) savedInstanceState.getSerializable(URLS_LABEL);
			buildList();
		} catch (Exception e) {
			loadList();
		}		
	}
	
	private void apiCall(){
		//Get playlist ID
		Bundle extras = getIntent().getExtras(); 
		playlistID = extras.getString(FilmCompletiActivity.PLAYLISTID_LABEL);
		//Get playlist parts 
		try {
			PlaylistFeed playlistFeed = service.getFeed(new URL(playlistID), PlaylistFeed.class);
			for(PlaylistEntry playlistEntry : playlistFeed.getEntries()) {
				TITLES.add(playlistEntry.getTitle().getPlainText());
				URLS.add(playlistEntry.getHtmlLink().getHref());
			}
		} catch (Exception e) {
			setResult(FilmCompletiActivity.RESULTCODE_KO);
			finish();
		}
	}
	
	private void loadList(){
		progDailog = ProgressDialog.show(this, ""
				+ getText(R.string.loadingTitle), ""
				+ getText(R.string.playlistLoading), true);
		progDailog.setIndeterminate(true);
		initTask = new InitTask();
		initTask.execute(this);
	}
	
	private void buildList(){
		setListAdapter(new ArrayAdapter<String>(this, R.layout.child_row, TITLES));

		ListView lv = (ListView) findViewById(R.id.playlistList);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openMovie(position);
			}
		});
	}
	
	private void openMovie(int pos) {
		// Opens the selected movie, identified by "title"
		Uri uri = Uri.parse(URLS.get(pos));
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save API results to Bundle
		savedInstanceState.putSerializable(TITLES_LABEL,(Serializable) TITLES);
		savedInstanceState.putSerializable(URLS_LABEL,(Serializable) URLS);
		super.onSaveInstanceState(savedInstanceState);
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Get API results from Bundle
		super.onRestoreInstanceState(savedInstanceState);
		TITLES = (List) savedInstanceState.getSerializable(TITLES_LABEL);
		URLS = (List) savedInstanceState.getSerializable(URLS_LABEL);
	}
	
	
	protected class InitTask extends AsyncTask<Context, Integer, String> {
		// Background task to parse the remote XML list, while showing a nice "Loading" dialog.
		@Override
		protected String doInBackground(Context... params) {
			apiCall();
			return "COMPLETE!";
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			progDailog.dismiss();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progDailog.dismiss();
			buildList();
		}
	}
}
