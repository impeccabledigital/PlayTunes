/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ideabag.playtunes.DragNDrop;

import com.ideabag.playtunes.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;

public class DragNDropListView extends ListView {
	
	// Used to toggle whether the list items can be dragged or not
	
	boolean mDraggingEnabled = false;
	
	boolean mDragMode;

	int mStartPosition;
	int mEndPosition;
	int mDragPointOffset;		//Used to adjust drag view location
	
	int mDragHandleWidth;
	
	ImageView mDragView;
	//View mDragShadow;
	
	//GestureDetector mGestureDetector;
	
	DropListener mDropListener;
	RemoveListener mRemoveListener;
	DragListener mDragListener;
	
	public DragNDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Resources r = context.getResources();
		
		mDragHandleWidth = r.getDimensionPixelSize( R.dimen.drag_handle_width );
		
		
		//setOnScrollListener( mScrollListener );
		
	}
	
	public void setDropListener(DropListener l) {
		mDropListener = l;
	}

	public void setRemoveListener(RemoveListener l) {
		mRemoveListener = l;
	}
	
	public void setDragListener(DragListener l) {
		mDragListener = l;
	}
	
	public void setDraggingEnabled( boolean enabled ) {
		
		mDraggingEnabled = enabled;
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();	
		
		if ( !mDraggingEnabled ) {
			
			return super.onTouchEvent( ev );
			
		}
		
		
		
		if ( action == MotionEvent.ACTION_DOWN && x >= ( this.getWidth() - mDragHandleWidth ) ) {
			
			mDragMode = true;
			
		}

		if ( !mDragMode ) 
			return super.onTouchEvent( ev );

		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mStartPosition = pointToPosition(x,y);
				if (mStartPosition != INVALID_POSITION) {
					int mItemPosition = mStartPosition - getFirstVisiblePosition();
                    mDragPointOffset = y - getChildAt(mItemPosition).getTop();
                    mDragPointOffset -= ((int)ev.getRawY()) - y;
					startDrag(mItemPosition,y);
					drag( 0,y);// replace 0 with x if desired
				}	
				break;
			case MotionEvent.ACTION_MOVE:
				drag(0,y);// replace 0 with x if desired
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default:
				mDragMode = false;
				mEndPosition = pointToPosition(x,y);
				// Where is the pointToPosition when dragged above?
				
				if ( mEndPosition == INVALID_POSITION && mStartPosition != INVALID_POSITION ) {
					
					if ( getFirstVisiblePosition() == 0 ) { // Above the first
						
						mEndPosition = 0;
						
					} else { // Below the last
						
						mEndPosition = getLastVisiblePosition();
						
					}
					//android.util.Log.i( "DragNDropListView", "First visible: " + getFirstVisiblePosition() );
					
				}
				
				android.util.Log.i( "DragNDropListView", "Dropped: " + mEndPosition );
				
				stopDrag(mStartPosition - getFirstVisiblePosition());
				if (mDropListener != null && mStartPosition != INVALID_POSITION && mEndPosition != INVALID_POSITION) 
	        		 mDropListener.onDrop(mStartPosition, mEndPosition);
				break;
		}
		
		return true;
		
	}	
	
	// move the drag view
	private void drag(int x, int y) {
		
		if (mDragView != null) {
			
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = x;
			layoutParams.y = y - mDragPointOffset;
			WindowManager mWindowManager = (WindowManager) getContext()
					.getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);
			
			if (mDragListener != null)
				mDragListener.onDrag( x, y, this );// change null to "this" when ready to use
			
		}
		
	}

	// enable the drag view for dragging
	@SuppressLint("NewApi")
	private void startDrag(int itemIndex, int y) {
		
		stopDrag(itemIndex);

		View item = getChildAt(itemIndex);
		if (item == null) return;
		item.setDrawingCacheEnabled(true);
		if (mDragListener != null)
			mDragListener.onStartDrag( itemIndex, item);
		
        // Create a copy of the drawing cache so that it does not get recycled
        // by the framework when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
        
        item.setDrawingCacheEnabled(false);

        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = y - mDragPointOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        //mWindowParams.format = PixelFormat.OPAQUE;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;
        
        Context context = getContext();
        
        
        ImageView v = new ImageView(context);
        
        v.setImageBitmap(bitmap);      
        v.setBackgroundResource( R.drawable.drag_shadow );
        
        if ( android.os.Build.VERSION.SDK_INT >= 11 ) {
        	
        	v.setAlpha( 0.8f );
        	
        }/* else if ( android.os.Build.VERSION.SDK_INT >= 16 ) {
        	
        	v.setImageAlpha( (int) 0.8 * 255 );
        	
        }
        */
        //LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		//mDragShadow = inflater.inflate( R.layout.list_item_placeholder, null );
        
        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        //mWindowManager.addView( mDragShadow , mWindowParams );
        mWindowManager.addView(v, mWindowParams );
        
        
        
        mDragView = v;
        
	}

	// destroy drag view
	private void stopDrag(int itemIndex) {
		if (mDragView != null) {
			if (mDragListener != null)
				mDragListener.onStopDrag(getChildAt(itemIndex));
            mDragView.setVisibility(GONE);
            WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(mDragView);
            
            //wm.removeView( mDragShadow );
            // Recycle the backing bitmap!
            BitmapDrawable bd = ( BitmapDrawable ) mDragView.getDrawable();
			
			if ( null != bd && null != bd.getBitmap() ) {
				
				bd.getBitmap().recycle();
				
			}
            
            
            mDragView.setImageDrawable(null);
            mDragView = null;
            //mDragShadow = null;
            
		}
		
	}
	
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override public void onScroll( AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			//if ( null != mDragShadow ) {
				
				//mDragShadow.setY( );
				
			//}
			android.util.Log.i( "DragNDropListView", "Is scrolling");
			
		}

		@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
			
			
			
		}
		
	};

//	private GestureDetector createFlingDetector() {
//		return new GestureDetector(getContext(), new SimpleOnGestureListener() {
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                    float velocityY) {         	
//                if (mDragView != null) {              	
//                	int deltaX = (int)Math.abs(e1.getX()-e2.getX());
//                	int deltaY = (int)Math.abs(e1.getY() - e2.getY());
//               
//                	if (deltaX > mDragView.getWidth()/2 && deltaY < mDragView.getHeight()) {
//                		mRemoveListener.onRemove(mStartPosition);
//                	}
//                	
//                	stopDrag(mStartPosition - getFirstVisiblePosition());
//
//                    return true;
//                }
//                return false;
//            }
//        });
//	}
}
