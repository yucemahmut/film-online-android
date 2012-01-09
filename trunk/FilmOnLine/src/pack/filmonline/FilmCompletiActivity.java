package pack.filmonline; 

import io.pen.bluepixel.filmonline.R;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class FilmCompletiActivity extends ExpandableListActivity{ 

	//Variables Initialization
	private final String ONLINE_LIST_URL = "http://dl.dropbox.com/u/12706770/FilmGratis/list.xml";
	private final String CONTACT_MAIL = "a.s.hereb@gmail.com";
	private Document doc; 
	private SimpleExpandableListAdapter expListAdapter;
	protected InitTask initTask;
	private ProgressDialog progDailog;
	private List groupList, childrenList;
	private List globalList = new ArrayList();
	private int tot;
	private AdView adView;
	private static final String AD_UNIT_ID = "a14f0647dbdca27";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.main);	
	    
		try{
			//If the movie list has been already read, don't reload it.
			groupList = (List) savedInstanceState.getSerializable("groupList");
			childrenList = (List) savedInstanceState.getSerializable("childrenList");
			tot = savedInstanceState.getInt("tot");
			globalList = (List) savedInstanceState.getSerializable("globalList");
		} catch (Exception e){
			groupList = null;
			childrenList = null;
		}
		if(groupList==null | childrenList==null)  {
			//If the movie list need to be initialized, do it here.
			progDailog = ProgressDialog.show(this, ""+getText(R.string.loadingTitle), ""+getText(R.string.loading), true);
			progDailog.setIndeterminate(true);
			initTask = new InitTask();
			initTask.execute( this );			
		} else {
			buildList();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		//Save lists read from XML to Bundle, so we don't need to read the XML again each time the activity is recreated.
		savedInstanceState.putSerializable("groupList", (Serializable) groupList);
		savedInstanceState.putSerializable("childrenList", (Serializable) childrenList);
		savedInstanceState.putInt("tot", tot);
		savedInstanceState.putSerializable("globalList", (Serializable) globalList);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		//Get the movie list from Bundle, instead of reading the XML all over again
		super.onRestoreInstanceState(savedInstanceState);
		groupList = (List) savedInstanceState.getSerializable("groupList");
		childrenList = (List) savedInstanceState.getSerializable("childrenList");
		globalList = (List) savedInstanceState.getSerializable("globalList");
		tot = savedInstanceState.getInt("tot");
	}

	private void buildList() {
		//Populate the movie list
		TextView total = (TextView)findViewById(R.id.total);
		total.setText(""+getText(R.string.total)+" "+tot);
		try{	
			expListAdapter = new SimpleExpandableListAdapter(
					this,
					groupList,              		// Creating group List.
					R.layout.group_row,             // Group item layout XML.
					new String[] { "Group Item" },  // the key of group item.
					new int[] { R.id.row_name },    // ID of each group item.-Data under the key goes into this TextView.
					childrenList,              		// childData describes second-level entries.
					R.layout.child_row,             // Layout for sub-level entries(second level).
					new String[] {"Sub Item"},      // Keys in childData maps to display.
					new int[] { R.id.grp_child}     // Data under the keys above go into these TextViews.
			);
			setListAdapter( expListAdapter );       // setting the adapter in the list.
		}catch(Exception e){
			System.out.println("Error: " + e.getMessage());
		}
		registerForContextMenu(getExpandableListView());
	}
	
	private void buildAd(){
		// Create the adView
		Map<String, Object> extras = new HashMap<String, Object>();
		extras.put("color_bg", "ffffff");
		extras.put("color_bg_top", "eeeeee");
		extras.put("color_border", "999999");
		extras.put("color_link", "000000");
		extras.put("color_text", "333333");
		extras.put("color_url", "666666");
	    adView = new AdView(this, AdSize.BANNER, AD_UNIT_ID);
	    adView.loadAd(new AdRequest());
	}

	public int initialize(String urlXml) {
		//Parsing of the remote XML file
		//Returns the total number of movies, 0 if errors occur
		try { 
			URL url = new URL(urlXml);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(new InputSource(url.openStream())); 
			doc.getDocumentElement().normalize();			
			return (doc.getElementsByTagName("film")).getLength();
		} catch (Exception e) {
			System.out.println("XML Pasing Excpetion = " + e);
		}
		return 0;
	}

	private void createGroupList() {
		//Create the HashMap for the row
		ArrayList resultG = new ArrayList();
		NodeList nodeList = doc.getElementsByTagName("category");
		for( int i = 0 ; i < nodeList.getLength() ; ++i ) {
			HashMap m = new HashMap();
			m.put( "Group Item",((Element)nodeList.item(i)).getAttribute("cat"));
			resultG.add( m );
		}
		groupList = (List)resultG;
	}

	private void createChildList() {
		//Create the HashMap for the children
		ArrayList resultC = new ArrayList();
		NodeList catList = doc.getElementsByTagName("category");

		//"category" node iteration
		for( int i = 0 ; i < catList.getLength() ; ++i ) { 
			NodeList filmList = ((Element)catList.item(i)).getChildNodes();
			ArrayList secList = new ArrayList();

			//"film" nodes iteration
			for( int n = 0 ; n < filmList.getLength() ; n++ ) { 
				List<String> film = new ArrayList<String>();
				//populate childrenList
				HashMap child = new HashMap();
				child.put( "Sub Item", ((Element)filmList.item(n)).getAttribute("id") );
				secList.add( child );
				//populate globalList
				film.add(""+((Element)filmList.item(n)).getAttribute("id"));
				film.add(""+((Element)filmList.item(n)).getAttribute("v"));
				film.add(""+((Element)filmList.item(n)).getAttribute("list"));
				globalList.add(film);
			}
			resultC.add( secList );
		}
		childrenList = resultC;
	}

	public void  onContentChanged  () {
		super.onContentChanged();
	}

	public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
		//Called on each child click
		String title = ((HashMap)expListAdapter.getChild(groupPosition, childPosition)).get("Sub Item").toString();
		openMovie(title);
		return true;
	}

	private void openMovie(String title){
		//Opens the selected movie, identified by "title"
		Uri uri = Uri.parse(getMovieUrl(title));
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);	
	}

	private void searchTrailer(String title){
		//Searches YouTube for the movie trailer
		String urlTrailer= "http://www.youtube.com/results?search_query=trailer+"+ title.replace(" ", "+")+"+italiano";
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(urlTrailer));
		startActivity(i);
	}		

	private void openImdb(String title){
		//Opens movie info in IMDB
		String urlImdb = title.replace(" ", "%20");
		try{
			//If IMDB is installed on device, open movie info using the local the app
			getPackageManager().getPackageInfo("com.imdb.mobile", 0);
			urlImdb = "imdb:///find?q="+urlImdb;
		} catch (Exception e) {
			//If IMDB app is not found, search movie info on their website
			urlImdb = "http://www.imdb.it/find?s=tt&q="+urlImdb;
		}	
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(urlImdb));
		startActivity(i);
	}

	private void openUri(Uri uri){
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(i);
	}

	private String getMovieUrl(String title){
		//Gets the movie URL on YouTube starting from its title
		for (int i = 0; i < globalList.size(); i++) {
			List<String> film = (List<String>) globalList.get(i);
			if(film.get(0).equals(title)){
				String temp = "http://www.youtube.com/watch?v="+film.get(1);
				if (!film.get(2).equals(""))
					temp += "&list="+film.get(2)+"&feature=plpp_play_all"; 
				return temp;
			}
		}
		return null;
	}

	public void  onGroupExpand  (int groupPosition) {
		//Called on expansion of the group
		//Does nothing for the moment
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		//Called on long-tap on child element in movie list
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
		//Only create a context menu for child items
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			menu.setHeaderTitle(((HashMap)expListAdapter.getChild(group, child)).get("Sub Item").toString());
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.cm_menu, menu);
		}
	}

	public boolean onContextItemSelected(MenuItem menuItem) {
		//Called when a context menu item is clicked
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		int groupPos, childPos;
		groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
		String title = ((HashMap)expListAdapter.getChild(groupPos, childPos)).get("Sub Item").toString();
		String urlWiki = "http://it.wikipedia.org/wiki/" + title.replace(" ", "_");
		//
		switch (menuItem.getItemId()) {
		case R.id.cm_info:
			//Get movie info
			openImdb(title);
			return true;
		case R.id.cm_wiki:
			//Search on Wikipedia
			openUri(Uri.parse(urlWiki));
			return true;
		case R.id.cm_trailer:
			//Get movie trailer
			searchTrailer(title);
			return true;
		case R.id.cm_play:
			//Play movie
			openMovie(title);
			return true;
		case R.id.cm_broken:
			//Report broken link
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"a.s.hereb@gmail.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ""+getText(R.string.brokenSubj));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, ""+getText(R.string.brokenText)+title+" - ("+getMovieUrl(title)+")");
			startActivity(Intent.createChooser(emailIntent, "Invia Mail"));
			return true;
		case R.id.cm_share:
			//Share movie link
			Intent i=new Intent(android.content.Intent.ACTION_SEND);
			i.setType("text/plain"); 
			i.putExtra(Intent.EXTRA_SUBJECT, title+" "+getText(R.string.share_subj));
			i.putExtra(Intent.EXTRA_TEXT, ""+getMovieUrl(title));
			startActivity(Intent.createChooser(i, title));
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.opt_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle menu selections
		switch (item.getItemId()) {
		case R.id.opt_info:
			//Info
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.app_name);
			builder.setCancelable(true);
			builder.setMessage(getText(R.string.infoText_1) +""+ getText(R.string.disclaimer));
			builder.setPositiveButton(R.string.infoButtonMail, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//Send mail
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{CONTACT_MAIL});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.subjectMail));
					startActivity(Intent.createChooser(emailIntent, getText(R.string.infoButtonMail)));
				}
			});
			builder.setNeutralButton(R.string.infoButtonWeb, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//Go to url
					openUri(Uri.parse("http://filmonlineandroid.pen.io"));
				}
			});
			builder.show();
			return true;
		case R.id.opt_rate:
			//Rate in market
			openUri(Uri.parse("market://details?id=io.pen.bluepixel.filmonline"));
			return true;
		case R.id.opt_contrib:
			//Contribute
			AlertDialog.Builder contrBuilder = new AlertDialog.Builder(this);
			contrBuilder.setTitle(R.string.contribTitle);
			contrBuilder.setCancelable(true);
			contrBuilder.setMessage(getText(R.string.contribText));
			contrBuilder.setPositiveButton(R.string.contribBtnRep, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//Go to project development page
					openUri(Uri.parse("http://code.google.com/p/film-online-android/"));
				}
			});
			contrBuilder.setNeutralButton(R.string.infoButtonMail, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//Send mail
					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{CONTACT_MAIL});
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.subjectMail));
					startActivity(Intent.createChooser(emailIntent, getText(R.string.infoButtonMail)));
				}
			});
			contrBuilder.show();
			return true;
		case R.id.opt_premium:
			//Get donation version
			openUri(Uri.parse("market://details?id=io.pen.bluepixel.filmonlinedonation"));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected class InitTask extends AsyncTask<Context, Integer, String> {
		//Background task to parse the remote XML list, while showing a nice "Loading" dialog.
		@Override
		protected String doInBackground( Context... params ) {
			tot = initialize(ONLINE_LIST_URL); 
			if (tot!=0){
				createGroupList();
				createChildList();				
			}
			Looper.prepare();
			//buildAd();
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
		protected void onCancelled(){
			progDailog.dismiss();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute( String result ) {
			super.onPostExecute(result);
			progDailog.dismiss();
			buildList();	
			
		}
	}
	 @Override
	  public void onDestroy() {
	    if (adView != null)
	    	adView.destroy();
	    super.onDestroy();
	  }
}

