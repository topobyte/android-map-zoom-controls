// Copyright 2018 Sebastian Kuerten
//
// This file is part of android-map-zoom-controls.
//
// android-map-zoom-controls is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// android-map-zoom-controls is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with android-map-zoom-controls. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.android.maps.utils.events;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ZoomControls;
import de.topobyte.interactiveview.ZoomChangedListener;
import de.topobyte.interactiveview.Zoomable;

public class MapZoomControls<T extends View & Zoomable> implements
		ZoomChangedListener, OnTouchListener
{

	/**
	 * Message code for the handler to hide the zoom controls.
	 */
	private static final int MSG_ZOOM_CONTROLS_HIDE = 0;

	/**
	 * Delay in milliseconds after which the zoom controls disappear.
	 */
	private static final long ZOOM_CONTROLS_TIMEOUT = ViewConfiguration
			.getZoomControlsTimeout();

	private final T zoomable;
	private boolean showMapZoomControls;
	private final ZoomControls zoomControls;
	private final Handler zoomControlsHideHandler;

	public MapZoomControls(T zoomable, ZoomControls zoomControls)
	{
		this.zoomable = zoomable;
		this.zoomControls = zoomControls;
		this.showMapZoomControls = true;
		this.zoomControls.setVisibility(View.GONE);

		zoomControls
				.setOnZoomInClickListener(new ZoomInClickListener(zoomable));
		zoomControls.setOnZoomOutClickListener(new ZoomOutClickListener(
				zoomable));
		zoomControlsHideHandler = new ZoomControlsHideHandler(zoomControls);
	}

	/**
	 * @return true if the zoom controls are visible, false otherwise.
	 */
	public boolean isShowMapZoomControls()
	{
		return this.showMapZoomControls;
	}

	/**
	 * @param showMapZoomControls
	 *            true if the zoom controls should be visible, false otherwise.
	 */
	public void setShowMapZoomControls(boolean showMapZoomControls)
	{
		this.showMapZoomControls = showMapZoomControls;
	}

	void showZoomControls()
	{
		this.zoomControlsHideHandler.removeMessages(MSG_ZOOM_CONTROLS_HIDE);
		if (this.zoomControls.getVisibility() != View.VISIBLE) {
			this.zoomControls.show();
		}
	}

	void showZoomControlsWithTimeout()
	{
		showZoomControls();
		this.zoomControlsHideHandler.sendEmptyMessageDelayed(
				MSG_ZOOM_CONTROLS_HIDE, ZOOM_CONTROLS_TIMEOUT);
	}

	public int getMeasuredHeight()
	{
		return this.zoomControls.getMeasuredHeight();
	}

	public int getMeasuredWidth()
	{
		return this.zoomControls.getMeasuredWidth();
	}

	public void measure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.zoomControls.measure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		int action = event.getAction();
		if (this.showMapZoomControls) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				showZoomControls();
				break;
			case MotionEvent.ACTION_CANCEL:
				showZoomControlsWithTimeout();
				break;
			case MotionEvent.ACTION_UP:
				showZoomControlsWithTimeout();
				break;
			}
		}
		return false;
	}

	@Override
	public void zoomChanged()
	{
		updateZoomButtonStates();
	}

	public void updateZoomButtonStates()
	{
		boolean zoomInEnabled = zoomable.canZoomIn();
		boolean zoomOutEnabled = zoomable.canZoomOut();
		this.zoomControls.setIsZoomInEnabled(zoomInEnabled);
		this.zoomControls.setIsZoomOutEnabled(zoomOutEnabled);
	}

	private class ZoomControlsHideHandler extends Handler
	{
		private final ZoomControls zoomControls;

		ZoomControlsHideHandler(ZoomControls zoomControls)
		{
			super();
			this.zoomControls = zoomControls;
		}

		@Override
		public void handleMessage(Message message)
		{
			this.zoomControls.hide();
		}
	}

	private class ZoomInClickListener implements View.OnClickListener
	{
		private final Zoomable zoomable;

		ZoomInClickListener(Zoomable zoomable)
		{
			this.zoomable = zoomable;
		}

		@Override
		public void onClick(View view)
		{
			this.zoomable.zoomIn();
			showZoomControlsWithTimeout();
		}
	}

	private class ZoomOutClickListener implements View.OnClickListener
	{
		private final Zoomable zoomable;

		ZoomOutClickListener(Zoomable zoomable)
		{
			this.zoomable = zoomable;
		}

		@Override
		public void onClick(View view)
		{
			this.zoomable.zoomOut();
			showZoomControlsWithTimeout();
		}
	}

}