package com.ideabag.playtunes.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.ideabag.playtunes.R;
import com.ideabag.playtunes.activity.MainActivity;
import com.ideabag.playtunes.adapter.PlaylistsOneAdapter;
import com.ideabag.playtunes.dialog.SongMenuDialogFragment;
import com.ideabag.playtunes.util.PlaylistBrowser;
import com.ideabag.playtunes.util.TrackerSingleton;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PlaylistsOneFragment extends ListFragment implements PlaylistBrowser {
	
	public static final String TAG = "One Playlist Fragment";
	
	MainActivity mActivity;
	PlaylistsOneAdapter adapter;
	
	private String PLAYLIST_ID = "";
	
	private MenuItem menuItemEdit, menuItemDoneEditing;
	
	private String mTitle, mSubtitle;
	
	//private DragNDropListView ListView;
	
	@Override public void setMediaID( String media_id ) {
		
		PLAYLIST_ID = media_id;
		
	}
	
	@Override public String getMediaID() { return PLAYLIST_ID; }
	
	@Override public void onAttach( Activity activity ) {
			
		super.onAttach( activity );
		
		mActivity = ( MainActivity ) activity;
		
	}
    
	@Override public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated( savedInstanceState );
		
		setHasOptionsMenu( true );
		
    	adapter = new PlaylistsOneAdapter( mActivity, PLAYLIST_ID, songMenuClickListener );
    	
    	getView().setBackgroundColor( getResources().getColor( android.R.color.white ) );
    	
		getListView().setDivider( mActivity.getResources().getDrawable( R.drawable.list_divider ) );
		getListView().setDividerHeight( 1 );
		getListView().setSelector( R.drawable.list_item_background );
		
		// Dumb thing to have a bottom divider shown
		getListView().setFooterDividersEnabled( true );
		getListView().addFooterView( new View( getActivity() ), null, true);
		
		setListAdapter( adapter );
		
		getActivity().getContentResolver().registerContentObserver(
				MediaStore.Audio.Playlists.Members.getContentUri( "external", Long.parseLong( PLAYLIST_ID ) ), true, mediaStoreChanged );
		
		
	}

	@Override public void onResume() {
		super.onResume();
		
		mActivity.setActionbarTitle( adapter.PLAYLIST_NAME );
    	mActivity.setActionbarSubtitle( adapter.getCount() + " " + ( adapter.getCount() == 1 ? getString( R.string.song_singular ) : getString( R.string.songs_plural ) ) );

		Tracker tracker = TrackerSingleton.getDefaultTracker( mActivity );

	    // Set screen name.
	    // Where path is a String representing the screen name.
		tracker.setScreenName( TAG );
		tracker.send( new HitBuilders.AppViewBuilder().build() );
		
		//t.set( "_count", ""+adapter.getCount() );
		tracker.send( new HitBuilders.EventBuilder()
    	.setCategory( "playlist" )
    	.setAction( "show" )
    	.setLabel( TAG )
    	.setValue( adapter.getCount() )
    	.build());
		
		//mActivity.AdView.resume();
		
		
		
	}
	
	@Override public void onPause() {
		super.onPause();
		
		//mActivity.AdView.pause();
		
		
		
	}
	
	@Override public void onDestroy() {
		super.onDestroy();
		setHasOptionsMenu( false );
		getActivity().getContentResolver().unregisterContentObserver( mediaStoreChanged );
		
		
	}
	
	@Override public void onDestroyView() {
	    super.onDestroyView();
	    
	    setListAdapter( null );
	    
	}

	@Override public void onListItemClick( ListView l, View v, int position, long id ) {
		
		if ( adapter.isEditing ) {
			
			Toast.makeText( getActivity(), "Can't play songs while editing playlist.", Toast.LENGTH_SHORT ).show();
			
		} else {
			
			String playlistName = mActivity.getSupportActionBar().getTitle().toString();
			
			mActivity.mBoundService.setPlaylist( adapter.getCursor(), playlistName, PlaylistsOneFragment.class, PLAYLIST_ID );
			
			mActivity.mBoundService.setPlaylistPosition( position - l.getHeaderViewsCount() );
			
			mActivity.mBoundService.play();
			
		}
		
	}
	
	@Override public void onCreateOptionsMenu( Menu menu, MenuInflater inflater) {
		
	   inflater.inflate( R.menu.menu_playlist_one, menu );
	   
	   menuItemEdit = menu.findItem( R.id.MenuPlaylistEdit );
	   
	   menuItemDoneEditing = menu.findItem( R.id.MenuPlaylistDone );
	   
	   menuItemDoneEditing.setVisible( false );
	   
	}
	
	
	@Override public boolean onOptionsItemSelected( MenuItem item ) {
		
	   switch ( item.getItemId() ) {
	   
	      case R.id.MenuPlaylistEdit:
	         
	    	  menuItemDoneEditing.setVisible( true );
	    	  menuItemEdit.setVisible( false );
	    	  adapter.setEditing( true );
	    	  //ListView.invalidate();
	    	  mTitle = (String) mActivity.getSupportActionBar().getTitle();
	    	  mSubtitle = (String) mActivity.getSupportActionBar().getSubtitle();
	    	  mActivity.setActionbarSubtitle( null );
	    	  mActivity.setActionbarTitle( getActivity().getString( R.string.playlist_editing ) );
	    	  
	         return true;
	         
	      case R.id.MenuPlaylistDone:
	    	  
	    	  menuItemDoneEditing.setVisible( false );
	    	  menuItemEdit.setVisible( true );
	    	  adapter.setEditing( false );
	    	  //ListView.invalidate();
	    	  mActivity.setActionbarTitle( mTitle );
	    	  mActivity.setActionbarSubtitle( mSubtitle );
	    	  
	    	  return true;
	    	  
	      default:
	         return super.onOptionsItemSelected(item);
	         
	   }
	   
	}
	
	View.OnClickListener songMenuClickListener = new View.OnClickListener() {
		
		@Override public void onClick( View v ) {
			
			int viewID = v.getId();
			String songID = "" + v.getTag( R.id.tag_song_id );
			
			if ( viewID == R.id.StarButton ) {
				
				ToggleButton starButton = ( ToggleButton ) v;
				
				if ( starButton.isChecked() ) {
					
					mActivity.PlaylistManager.addFavorite( songID );
					//android.util.Log.i( "starred", songID );
					
				} else {
					
					mActivity.PlaylistManager.removeFavorite( songID );
					//android.util.Log.i( "unstarred", songID );
					
				}
				
			} else if ( viewID == R.id.MenuButton ) {
				
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
	        	
				SongMenuDialogFragment newFragment = new SongMenuDialogFragment();
				newFragment.setMediaID( songID );
	        	
	            newFragment.show( ft, "dialog" );
				
			}
			
			
			
		}
		
	};
	
	ContentObserver mediaStoreChanged = new ContentObserver( new Handler() ) {
		
        @Override public void onChange( boolean selfChange ) {
            
            mActivity.runOnUiThread( new Runnable() {

				@Override public void run() {
					
					adapter.requery();
					adapter.notifyDataSetChanged();
					
				}
            	
            });
            
            super.onChange( selfChange );
            
        }

	};
	
	
}