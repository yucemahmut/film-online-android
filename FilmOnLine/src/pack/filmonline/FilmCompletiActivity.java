package pack.filmonline;

import io.pen.bluepixel.filmonline.R;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FilmCompletiActivity extends ExpandableListActivity {

    // Variables Initialization
    public static final String GROUPLIST_LABEL="groupList";
    public static final String CHILDRENIST_LABEL="childrenList";
    public static final String GLOBALLIST_LABEL="globalList";
    public static final String GROUPLIST_NEW_LABEL="groupListNew";
    public static final String CHILDRENIST_NEW_LABEL="childrenListNew";
    public static final String GROUPLIST_LABEL_SEARCH="groupListSearch";
    public static final String CHILDRENIST_LABEL_SEARCH="childrenListSearch";
    public static final String GLOBALLIST_NEW_LABEL="globalListNew";
    public static final String TOT_LABEL="tot";
    public static final String TOT_NEW_LABEL="tot_new";
    public static final String TOT_SEARCH_LABEL="tot_search";
    public static final String LATEST_LABEL="latest";
    public static final String ONSEARCH_LABEL="onSearch";
    public static final String PLAYLISTID_LABEL="playlistId";
    public static final String TITLE_LABEL="title";
    public static final int RESULTCODE=0;
    public static final int RESULTCODE_OK=1;
    public static final int RESULTCODE_KO=2;
    //
    private static final String ONLINE_LIST_URL="http://dl.dropbox.com/u/12706770/FilmGratis/list.xml";
    private static final String NEW_LIST_URL="http://dl.dropbox.com/u/12706770/FilmGratis/new.xml";
    private static final String CONTACT_MAIL="film.online.android@gmail.com";
    private static final String PLAYLIST_TAG=" (Playlist)";
    //
    private Document doc;
    private SimpleExpandableListAdapter expListAdapter;
    protected InitTask initTask;
    private ProgressDialog progDailog;
    private List groupList, childrenList, groupListNew, childrenListNew, groupListSearch, childrenListSearch;
    private List globalList=new ArrayList();
    private List globalListNew=new ArrayList();
    private int tot, tot_new, tot_serach;
    private boolean onLatestFlag=false;
    private boolean onSearchFlag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	try {
	    // If the movie list has been already read, don't reload it.
	    getBundle(savedInstanceState);
	}
	catch (Exception e) {
	    groupList=null;
	    childrenList=null;
	    childrenListNew=null;
	    groupListNew=null;
	}
	if ((groupList == null) | (childrenList == null) | (groupListNew == null) | (childrenListNew == null)) {
	    // If the movie lists need to be initialized, do it here.
	    loadList();
	}
	else {
	    buildList();
	}

	// Set listeners
	final ImageButton listBtn=(ImageButton) findViewById(R.id.imageButtonList);
	final ImageButton latestBtn=(ImageButton) findViewById(R.id.imageButtonLatest);

	latestBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onSearchFlag=false;
		onLatestFlag=true;
		setButtonColors();
		if ((groupList == null) | (childrenList == null) | (groupListNew == null) | (childrenListNew == null)) {
		    // If the movie list need to be initialized, do it here.
		    loadList();
		}
		else {
		    buildList();
		}
	    }
	});

	listBtn.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		onSearchFlag=false;
		onLatestFlag=false;
		setButtonColors();
		if ((groupList == null) | (childrenList == null) | (groupListNew == null) | (childrenListNew == null)) {
		    // If the movie list need to be initialized, do it here.
		    loadList();
		}
		else {
		    buildList();
		}
	    }
	});
	setButtonColors();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
	// Save lists read from XML to Bundle, so we don't need to read the XML
	// again each time the activity is recreated.
	saveBundle(savedInstanceState);
	super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
	// Get the movie list from Bundle, instead of reading the XML all over again
	super.onRestoreInstanceState(savedInstanceState);
	getBundle(savedInstanceState);
    }

    @Override
    public boolean onSearchRequested() {
	// Pass initialized lists to search, so we don't have to read the XML file again
	Bundle appData=new Bundle();
	appData.putSerializable(GROUPLIST_LABEL, (Serializable) groupList);
	appData.putSerializable(CHILDRENIST_LABEL, (Serializable) childrenList);
	appData.putSerializable(GLOBALLIST_LABEL, (Serializable) globalList);
	startSearch(null, false, appData, false);
	return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
	setIntent(intent);
	// Get the intent, verify the action and get the query
	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    try {
		// Load budle with lists
		Bundle appData=getIntent().getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
		    groupList=(List) appData.getSerializable(GROUPLIST_LABEL);
		    childrenList=(List) appData.getSerializable(CHILDRENIST_LABEL);
		    globalList=(List) appData.getSerializable(GLOBALLIST_LABEL);
		    String query=intent.getStringExtra(SearchManager.QUERY);
		    search(query);
		}
	    }
	    catch (Exception e) {
		buildList();
	    }
	}
	else {
	    if ((groupList == null) | (childrenList == null) | (groupListNew == null) | (childrenListNew == null)) {
		// If the movie list need to be initialized, do it here.
		loadList();
	    }
	    else {
		buildList();
	    }
	}
    }

    @Override
    public void onBackPressed() {
	if (onSearchFlag) {
	    onSearchFlag=false;
	    onLatestFlag=false;
	    buildList();
	    setButtonColors();
	}
	else {
	    finish();
	}
    }

    private void getBundle(Bundle savedInstanceState) {
	groupList=(List) savedInstanceState.getSerializable(GROUPLIST_LABEL);
	childrenList=(List) savedInstanceState.getSerializable(CHILDRENIST_LABEL);
	groupListNew=(List) savedInstanceState.getSerializable(GROUPLIST_NEW_LABEL);
	childrenListNew=(List) savedInstanceState.getSerializable(CHILDRENIST_NEW_LABEL);
	groupListSearch=(List) savedInstanceState.getSerializable(GROUPLIST_LABEL_SEARCH);
	childrenListSearch=(List) savedInstanceState.getSerializable(CHILDRENIST_LABEL_SEARCH);
	tot=savedInstanceState.getInt(TOT_LABEL);
	tot_new=savedInstanceState.getInt(TOT_NEW_LABEL);
	tot_serach=savedInstanceState.getInt(TOT_SEARCH_LABEL);
	globalList=(List) savedInstanceState.getSerializable(GLOBALLIST_LABEL);
	globalListNew=(List) savedInstanceState.getSerializable(GLOBALLIST_NEW_LABEL);
	onSearchFlag=savedInstanceState.getBoolean(ONSEARCH_LABEL);
	onLatestFlag=savedInstanceState.getBoolean(LATEST_LABEL);
    }

    private void saveBundle(Bundle savedInstanceState) {
	savedInstanceState.putSerializable(GROUPLIST_LABEL, (Serializable) groupList);
	savedInstanceState.putSerializable(CHILDRENIST_LABEL, (Serializable) childrenList);
	savedInstanceState.putSerializable(GROUPLIST_NEW_LABEL, (Serializable) groupListNew);
	savedInstanceState.putSerializable(CHILDRENIST_NEW_LABEL, (Serializable) childrenListNew);
	savedInstanceState.putSerializable(GROUPLIST_LABEL_SEARCH, (Serializable) groupListSearch);
	savedInstanceState.putSerializable(CHILDRENIST_LABEL_SEARCH, (Serializable) childrenListSearch);
	savedInstanceState.putInt(TOT_LABEL, tot);
	savedInstanceState.putInt(TOT_SEARCH_LABEL, tot_serach);
	savedInstanceState.putInt(TOT_NEW_LABEL, tot_new);
	savedInstanceState.putSerializable(GLOBALLIST_LABEL, (Serializable) globalList);
	savedInstanceState.putSerializable(GLOBALLIST_NEW_LABEL, (Serializable) globalListNew);
	savedInstanceState.putBoolean(ONSEARCH_LABEL, onSearchFlag);
	savedInstanceState.putBoolean(LATEST_LABEL, onLatestFlag);
    }

    private void setButtonColors() {
	ImageButton listBtn=(ImageButton) findViewById(R.id.imageButtonList);
	ImageButton latestBtn=(ImageButton) findViewById(R.id.imageButtonLatest);

	if (onLatestFlag) {
	    latestBtn.setBackgroundColor(getResources().getColor(R.color.white));
	    listBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.black_white_gradient));
	}
	else {
	    latestBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.black_white_gradient));
	    listBtn.setBackgroundColor(getResources().getColor(R.color.white));
	}
    }

    private void buildList() {
	// Populate the movie list
	TextView total=(TextView) findViewById(R.id.total);
	List children, group;
	if (onLatestFlag) {
	    total.setText("" + getText(R.string.latest) + " " + tot_new);
	    children=childrenListNew;
	    group=groupListNew;
	}
	else if (onSearchFlag) {
	    total.setText("" + getText(R.string.search_total) + " " + tot_serach);
	    children=childrenListSearch;
	    group=groupListSearch;
	}
	else {
	    total.setText("" + getText(R.string.total) + " " + tot);
	    children=childrenList;
	    group=groupList;
	}
	try {
	    expListAdapter=new SimpleExpandableListAdapter(this, group, // Creating group List.
		R.layout.group_row, // Group item layout XML.
		new String[]{"Group Item" }, // the key of group item.
		new int[]{R.id.row_name }, // ID of each group item.-Data under the key goes into
					   // this TextView.
		children, // childData describes second-level entries.
		R.layout.child_row, // Layout for sub-level entries(second level).
		new String[]{"Sub Item" }, // Keys in childData maps to display.
		new int[]{R.id.grp_child } // Data under the keys above go into these TextViews.
	    );
	    setListAdapter(expListAdapter); // setting the adapter in the list.
	}
	catch (Exception e) {
	    // System.out.println("Error: " + e.getMessage());
	}
	registerForContextMenu(getExpandableListView());
	// Collapse all list groups
	try {
	    if ( ! onLatestFlag) {
		for (int i=1; i <= expListAdapter.getGroupCount(); i++) {
		    getExpandableListView().collapseGroup(i - 1);
		}
	    }
	    else {
		for (int i=1; i <= expListAdapter.getGroupCount(); i++) {
		    getExpandableListView().expandGroup(i - 1);
		}
	    }
	}
	catch (Exception e) {
	    // List is empty, nothing to close
	}
    }

    public int initialize(String urlXml) {
	// Parsing of the remote XML file
	// Returns the total number of movies, 0 if errors occur
	try {
	    URL url=new URL(urlXml);
	    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
	    DocumentBuilder db=dbf.newDocumentBuilder();
	    doc=db.parse(new InputSource(url.openStream()));
	    doc.getDocumentElement().normalize();
	    return (doc.getElementsByTagName("film")).getLength();
	}
	catch (Exception e) {
	    System.out.println("XML Pasing Excpetion = " + e);
	    return 0;
	}
    }

    private void createGroupList(boolean newestFlag) {
	// Create the HashMap for the row
	ArrayList resultG=new ArrayList();
	NodeList nodeList=doc.getElementsByTagName("category");
	for (int i=0; i < nodeList.getLength(); ++i) {
	    HashMap m=new HashMap();
	    m.put("Group Item", ((Element) nodeList.item(i)).getAttribute("cat"));
	    resultG.add(m);
	}
	if (newestFlag) {
	    groupListNew=resultG;
	}
	else {
	    groupList=resultG;
	}
    }

    private void createChildList(boolean newestFlag) {
	// Create the HashMap for the children
	ArrayList resultC=new ArrayList();
	NodeList catList=doc.getElementsByTagName("category");

	// "category" node iteration
	for (int i=0; i < catList.getLength(); ++i) {
	    NodeList filmList=((Element) catList.item(i)).getChildNodes();
	    ArrayList secList=new ArrayList();

	    // "film" nodes iteration
	    for (int n=0; n < filmList.getLength(); n++) {
		List<String> film=new ArrayList<String>();
		// populate childrenList
		String title=((Element) filmList.item(n)).getAttribute("id");
		// add "Playlist" tag if needed
		if ( ! ((Element) filmList.item(n)).getAttribute("list").equals("")) {
		    title=title + PLAYLIST_TAG;
		}
		HashMap child=new HashMap();
		child.put("Sub Item", title);
		secList.add(child);
		// populate globalList
		film.add("" + ((Element) filmList.item(n)).getAttribute("id"));
		film.add("" + ((Element) filmList.item(n)).getAttribute("v"));
		film.add("" + ((Element) filmList.item(n)).getAttribute("list"));
		if (newestFlag) {
		    globalListNew.add(film);
		}
		else {
		    globalList.add(film);
		}
	    }
	    resultC.add(secList);
	}
	if (newestFlag) {
	    childrenListNew=resultC;
	}
	else {
	    childrenList=resultC;
	}
    }

    @Override
    public void onContentChanged() {
	super.onContentChanged();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
	// Called on each child click
	String title=((HashMap) expListAdapter.getChild(groupPosition, childPosition)).get("Sub Item").toString();
	title=title.replace(PLAYLIST_TAG, "");
	if (isPlaylist(title)) {
	    // Call activity to handle the playlist
	    Intent intent=new Intent(getBaseContext(), PlaylistActivity.class);
	    intent.putExtra(PLAYLISTID_LABEL, getPlaylistId(title));
	    intent.putExtra(TITLE_LABEL, title);
	    startActivityForResult(intent, RESULTCODE);
	}
	else {
	    openMovie(title);
	}
	return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == RESULTCODE) {
	    if (resultCode == RESULTCODE_KO) {
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.playlistError),
		    Toast.LENGTH_SHORT).show();
	    }
	}
    }

    private boolean isPlaylist(String title) {
	List list;
	if (onLatestFlag) {
	    list=globalListNew;
	}
	else {
	    list=globalList;
	}
	for (int i=0; i < list.size(); i++) {
	    List<String> film=(List<String>) list.get(i);
	    if (film.get(0).equals(title)) {
		if ( ! film.get(2).equals("")) {
		    return true; // Playlist
		}
		else {
		    return false; // Full
		}
	    }
	}
	return false;
    }

    private void openMovie(String title) {
	// Opens the selected movie, identified by "title"
	Uri uri=Uri.parse(getMovieUrl(title));
	Intent i=new Intent(Intent.ACTION_VIEW, uri);
	startActivity(i);
    }

    private void searchTrailer(String title) {
	// Searches YouTube for the movie trailer
	String urlTrailer="http://www.youtube.com/results?search_query=trailer+" + title.replace(" ", "+")
	    + "+italiano";
	Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse(urlTrailer));
	startActivity(i);
    }

    private void openImdb(String title) {
	// Opens movie info in IMDB
	String urlImdb=title.replace(" ", "%20");
	try {
	    // If IMDB is installed on device, open movie info using the local
	    // the app
	    getPackageManager().getPackageInfo("com.imdb.mobile", 0);
	    urlImdb="imdb:///find?q=" + urlImdb;
	}
	catch (Exception e) {
	    // If IMDB app is not found, search movie info on their website
	    urlImdb="http://www.imdb.it/find?s=tt&q=" + urlImdb;
	}
	Intent i=new Intent(Intent.ACTION_VIEW, Uri.parse(urlImdb));
	startActivity(i);
    }

    private void openUri(Uri uri) {
	Intent i=new Intent(Intent.ACTION_VIEW, uri);
	startActivity(i);
    }

    private String getMovieUrl(String title) {
	// Gets the movie URL on YouTube starting from its title
	List list;
	if (onLatestFlag) {
	    list=globalListNew;
	}
	else {
	    list=globalList;
	}
	for (int i=0; i < list.size(); i++) {
	    List<String> film=(List<String>) list.get(i);
	    if (film.get(0).equals(title)) {
		String temp="http://www.youtube.com/watch?v=" + film.get(1);
		// if (!film.get(2).equals(""))
		// temp += "&list=" + film.get(2) + "&feature=plpp_play_all";
		return temp;
	    }
	}
	return null;
    }

    private String getPlaylistId(String title) {
	// Gets the playlist ID (without "PL") on YouTube starting from movie title
	List list;
	if (onLatestFlag) {
	    list=globalListNew;
	}
	else {
	    list=globalList;
	}
	for (int i=0; i < list.size(); i++) {
	    List<String> film=(List<String>) list.get(i);
	    if (film.get(0).equals(title)) {
		if ( ! film.get(2).equals("")) {
		    return film.get(2).substring(2);
		}
	    }
	}
	return null;
    }

    @Override
    public void onGroupExpand(int groupPosition) {
	// Called on expansion of the group
	// Does nothing for the moment
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	// Called on long-tap on child element in movie list
	super.onCreateContextMenu(menu, v, menuInfo);
	ExpandableListView.ExpandableListContextMenuInfo info=(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
	int type=ExpandableListView.getPackedPositionType(info.packedPosition);
	int group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
	int child=ExpandableListView.getPackedPositionChild(info.packedPosition);
	// Only create a context menu for child items
	if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	    String title=((HashMap) expListAdapter.getChild(group, child)).get("Sub Item").toString();
	    title=title.replace(PLAYLIST_TAG, "");
	    menu.setHeaderTitle(title);
	    // Create different menu for playlist or full entry
	    MenuInflater inflater=getMenuInflater();
	    List list;
	    if (onLatestFlag) {
		list=globalListNew;
	    }
	    else {
		list=globalList;
	    }
	    for (int i=0; i < list.size(); i++) {
		List<String> film=(List<String>) list.get(i);
		if (film.get(0).equals(title)) {
		    if ( ! film.get(2).equals("")) {
			inflater.inflate(R.menu.cm_menu_playlist, menu); // Playlist
		    }
		    else {
			inflater.inflate(R.menu.cm_menu, menu); // Full
		    }
		}
	    }
	}
    }

    // Search function (developed by Paolo Casillo)
    public void search(String dacercare) {
	dacercare=dacercare.trim().toUpperCase();
	if ("".equals(dacercare)) {
	    Toast toast=Toast.makeText(this, getString(R.string.no_search), Toast.LENGTH_LONG);
	    toast.show();
	    return;
	}
	List findFilm=new ArrayList();
	List findCat=new ArrayList();
	int trovati=0;
	boolean cat_trovata;
	for (int i=0; i < childrenList.size(); ++i) {
	    cat_trovata=false;
	    ArrayList<HashMap<String, String>> secList=new ArrayList<HashMap<String, String>>();
	    List<HashMap<String, String>> app=(List<HashMap<String, String>>) childrenList.get(i);
	    HashMap<String, String> child=null;
	    // "film" nodes iteration
	    for (int n=0; n < app.size(); n++) {
		// populate childrenList
		child=app.get(n);
		if (child.get("Sub Item").toUpperCase().contains(dacercare)) {
		    secList.add(child);
		    trovati++;
		    if ( ! cat_trovata) {
			findCat.add(groupList.get(i));
			cat_trovata=true;
		    }
		}
	    }
	    if (cat_trovata) {
		findFilm.add(secList);
	    }
	}
	// Save lists
	groupListSearch=findCat;
	childrenListSearch=findFilm;
	tot_serach=trovati;
	// Re-buildList
	TextView total=(TextView) findViewById(R.id.total);
	total.setText("" + getText(R.string.search_total) + " " + trovati);
	try {
	    expListAdapter=new SimpleExpandableListAdapter(this, findCat, // Creating group List.
		R.layout.group_row, // Group item layout XML.
		new String[]{"Group Item" }, // the key of group item.
		new int[]{R.id.row_name }, // ID of each group item.-Data under the key goes into
					   // this TextView.
		findFilm, // childData describes second-level entries.
		R.layout.child_row, // Layout for sub-level entries(second level).
		new String[]{"Sub Item" }, // Keys in childData maps to display.
		new int[]{R.id.grp_child } // Data under the keys above go into these TextViews.
	    );
	    setListAdapter(expListAdapter); // setting the adapter in the list.
	}
	catch (Exception e) {
	    // System.out.println("Error: " + e.getMessage());
	}
	registerForContextMenu(getExpandableListView());
	// Expand all list groups
	for (int i=1; i <= expListAdapter.getGroupCount(); i++) {
	    getExpandableListView().expandGroup(i - 1);
	}
	//
	onSearchFlag=true;
	onLatestFlag=false;
	setButtonColors();
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
	// Called when a context menu item is clicked
	ExpandableListContextMenuInfo info=(ExpandableListContextMenuInfo) menuItem.getMenuInfo();
	int groupPos, childPos;
	groupPos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
	childPos=ExpandableListView.getPackedPositionChild(info.packedPosition);
	String title=((HashMap) expListAdapter.getChild(groupPos, childPos)).get("Sub Item").toString();
	title=title.replace(PLAYLIST_TAG, "");
	String urlWiki="http://it.wikipedia.org/wiki/" + title.replace(" ", "_");
	String urlPlaybill="http://images.google.com/search?tbm=isch&q=locandina+" + title.replace(" ", "%20");
	//
	switch (menuItem.getItemId()) {
	    case R.id.cm_info:
		// Get movie info
		openImdb(title);
		return true;
	    case R.id.cm_wiki:
		// Search on Wikipedia
		openUri(Uri.parse(urlWiki));
		return true;
	    case R.id.cm_trailer:
		// Get movie trailer
		searchTrailer(title);
		return true;
	    case R.id.cm_play:
		// Play movie
		openMovie(title);
		return true;
	    case R.id.cm_broken:
		// Report broken link
		Intent emailIntent=new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"a.s.hereb@gmail.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "" + getText(R.string.brokenSubj));
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "" + getText(R.string.brokenText) + title
		    + " - (" + getMovieUrl(title) + ")");
		startActivity(Intent.createChooser(emailIntent, "Invia Mail"));
		return true;
	    case R.id.cm_share:
		// Share movie link
		Intent i=new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, title + " " + getText(R.string.share_subj));
		i.putExtra(Intent.EXTRA_TEXT, "" + getMovieUrl(title));
		startActivity(Intent.createChooser(i, title));
		return true;
	    case R.id.cm_playbill:
		// Search for playbill in G. Images
		openUri(Uri.parse(urlPlaybill));
		return true;
	    case R.id.cm_playlist:
		// Call activity to handle the playlist
		Intent intent=new Intent(getBaseContext(), PlaylistActivity.class);
		intent.putExtra(PLAYLISTID_LABEL, getPlaylistId(title));
		intent.putExtra(TITLE_LABEL, title);
		startActivityForResult(intent, RESULTCODE);
		return true;
	}
	return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater=getMenuInflater();
	inflater.inflate(R.menu.opt_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle menu selections
	switch (item.getItemId()) {
	    case R.id.opt_info:
		// Info
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setCancelable(true);
		builder.setMessage(getText(R.string.infoText_1) + "" + getText(R.string.disclaimer));
		builder.setPositiveButton(R.string.infoButtonMail, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int id) {
			// Send mail
			Intent emailIntent=new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{CONTACT_MAIL });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.subjectMail));
			startActivity(Intent.createChooser(emailIntent, getText(R.string.infoButtonMail)));
		    }
		});
		builder.setNeutralButton(R.string.infoButtonWeb, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int id) {
			// Go to url
			openUri(Uri.parse("http://filmonlineandroid.pen.io"));
		    }
		});
		builder.show();
		return true;
	    case R.id.opt_rate:
		// Rate in market
		openUri(Uri.parse("market://details?id=io.pen.bluepixel.filmonline"));
		return true;
	    case R.id.opt_contrib:
		// Contribute
		AlertDialog.Builder contrBuilder=new AlertDialog.Builder(this);
		contrBuilder.setTitle(R.string.opt_contribTitle);
		contrBuilder.setCancelable(true);
		contrBuilder.setMessage(getText(R.string.opt_contribText));
		contrBuilder.setPositiveButton(R.string.opt_contribBtnRep, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int id) {
			// Go to project development page
			openUri(Uri.parse("http://code.google.com/p/film-online-android/"));
		    }
		});
		contrBuilder.setNeutralButton(R.string.infoButtonMail, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int id) {
			// Send mail
			Intent emailIntent=new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{CONTACT_MAIL });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.subjectMail));
			startActivity(Intent.createChooser(emailIntent, getText(R.string.infoButtonMail)));
		    }
		});
		contrBuilder.show();
		return true;
	    case R.id.opt_premium:
		// Get donation version
		openUri(Uri.parse("market://details?id=io.pen.bluepixel.filmonlinedonation"));
		return true;
	    case R.id.opt_search:
		// Open search dialog (as if search button pressed)
		onSearchRequested();
		return true;
	    case R.id.opt_latest:
		// Open "recently added" tab
		onSearchFlag=false;
		onLatestFlag=true;
		setButtonColors();
		if ((groupList == null) | (childrenList == null) | (groupListNew == null) | (childrenListNew == null)) {
		    // If the movie list need to be initialized, do it here.
		    loadList();
		}
		else {
		    buildList();
		}
		return true;
		//
		// case R.id.opt_credits:
		// //Show credits
		// AlertDialog.Builder creditsBuilder = new
		// AlertDialog.Builder(this);
		// creditsBuilder.setTitle(R.string.opt_credits);
		// creditsBuilder.setCancelable(true);
		// creditsBuilder.setMessage(getText(R.string.opt_credits_text));
		// creditsBuilder.show();
		// return true;
		//
	    default:
		return super.onOptionsItemSelected(item);
	}
    }

    private void loadList() {
	progDailog=ProgressDialog.show(this, "" + getText(R.string.loadingTitle), "" + getText(R.string.loading), true);
	progDailog.setIndeterminate(true);
	initTask=new InitTask();
	initTask.execute(this);
    }

    protected class InitTask extends AsyncTask<Context, Integer, String> {
	// Background task to parse the remote XML list, while showing a nice "Loading" dialog.
	@Override
	protected String doInBackground(Context... params) {
	    tot=initialize(ONLINE_LIST_URL);
	    if (tot != 0) {
		createGroupList(false);
		createChildList(false);
	    }
	    tot_new=initialize(NEW_LIST_URL);
	    if (tot_new != 0) {
		createGroupList(true);
		createChildList(true);
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
	    try {
		progDailog.dismiss();
	    }
	    catch (Exception e) {
		Intent intent=getIntent();
		finish();
		startActivity(intent);
	    }
	    buildList();

	}
    }
}
