package pack.filmonline;

import io.pen.bluepixel.filmonline.R;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PlaylistActivity extends ListActivity {
    private static final String CONTACT_MAIL="film.online.android@gmail.com";
    private String playlistID="";
    private final String PART_TAG="Playlist - Parte ";
    private List<String> TITLES=new ArrayList<String>();
    private List<String> URLS=new ArrayList<String>();
    public static final String TITLES_LABEL="titles";
    public static final String URLS_LABEL="urls";
    private ProgressDialog progDailog;
    protected InitTask initTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.playlist);
	try {
	    try {
		TITLES=(List) savedInstanceState.getSerializable(TITLES_LABEL);
		URLS=(List) savedInstanceState.getSerializable(URLS_LABEL);
		buildList();
	    }
	    catch (Exception e) {
		loadList();
	    }
	    // Set title
	    Bundle extras=getIntent().getExtras();
	    TextView tv=(TextView) findViewById(R.id.playlistTitle);
	    tv.setText(extras.getString(FilmCompletiActivity.TITLE_LABEL));

	    // Set listeners
	    final ImageButton backBtn=(ImageButton) findViewById(R.id.playlistBack);
	    backBtn.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
		    finish();
		}
	    });
	}
	catch (Exception e) {
	    // System.out.println("XML Pasing Excpetion = " + e);
	    setResult(FilmCompletiActivity.RESULTCODE_KO);
	    finish();
	}
    }

    private void apiCall() throws ParserConfigurationException, SAXException, IOException {
	// Get playlist ID
	Bundle extras=getIntent().getExtras();
	playlistID=extras.getString(FilmCompletiActivity.PLAYLISTID_LABEL);
	Document doc=null;
	// Get playlist parts
	URL url=new URL("http://gdata.youtube.com/feeds/api/playlists/" + playlistID + "?v=2");
	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
	DocumentBuilder db=dbf.newDocumentBuilder();
	doc=db.parse(new InputSource(url.openStream()));
	doc.getDocumentElement().normalize();
	NodeList nodeList=doc.getElementsByTagName("yt:videoid");
	for (int i=0; i < nodeList.getLength(); ++i) {
	    int index=i + 1;
	    TITLES.add(PART_TAG + index);
	    URLS.add("http://www.youtube.com/watch?v=" + ((Element) nodeList.item(i)).getTextContent());
	}
    }

    private void loadList() {
	progDailog=ProgressDialog.show(this, "" + getText(R.string.loadingTitle), ""
	    + getText(R.string.playlistLoading), true);
	progDailog.setIndeterminate(true);
	initTask=new InitTask();
	initTask.execute(this);
    }

    private void buildList() {
	setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, TITLES));

	ListView lv=getListView();
	lv.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		openMovie(position);
	    }
	});
    }

    private void openMovie(int pos) {
	// Opens the selected movie, identified by "title"
	Uri uri=Uri.parse(URLS.get(pos));
	Intent i=new Intent(Intent.ACTION_VIEW, uri);
	startActivity(i);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
	// Save API results to Bundle
	savedInstanceState.putSerializable(TITLES_LABEL, (Serializable) TITLES);
	savedInstanceState.putSerializable(URLS_LABEL, (Serializable) URLS);
	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
	// Get API results from Bundle
	super.onRestoreInstanceState(savedInstanceState);
	TITLES=(List) savedInstanceState.getSerializable(TITLES_LABEL);
	URLS=(List) savedInstanceState.getSerializable(URLS_LABEL);
    }

    private void openUri(Uri uri) {
	Intent i=new Intent(Intent.ACTION_VIEW, uri);
	startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater=getMenuInflater();
	inflater.inflate(R.menu.opt_menu_playlist, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle menu selections
	switch (item.getItemId()) {
	    case R.id.opt_back:
		// Go back
		finish();
		return true;
	    default:
		return super.onOptionsItemSelected(item);
	}
    }

    protected class InitTask extends AsyncTask<Context, Integer, String> {
	// Background task to parse the remote XML list, while showing a nice "Loading" dialog.
	@Override
	protected String doInBackground(Context... params) {
	    try {
		apiCall();
	    }
	    catch (Exception e) {
		// System.out.println("XML Pasing Excpetion = " + e);
		setResult(FilmCompletiActivity.RESULTCODE_KO);
		progDailog.dismiss();
		finish();
	    }
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
