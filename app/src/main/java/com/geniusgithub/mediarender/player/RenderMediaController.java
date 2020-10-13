package com.geniusgithub.mediarender.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import java.util.ArrayList;

import tv.danmaku.ijk.media.widget.media.IMediaController;

public class RenderMediaController extends PlayerVideoController implements IMediaController {
  private ActionBar mActionBar;

  public RenderMediaController(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  public RenderMediaController(Context context, boolean useFastForward) {
    super(context);
    initView(context);
  }

  public RenderMediaController(Context context) {
    super(context);
    initView(context);
  }

  protected void initView(Context context) {
  }

  public void setSupportActionBar(@Nullable ActionBar actionBar) {
    mActionBar = actionBar;
    if (isShowing()) {
      actionBar.show();
    } else {
      actionBar.hide();
    }
  }

  @Override
  public void show() {
    super.show();
    if (mActionBar != null)
      mActionBar.show();
  }

  @Override
  public void hide() {
    super.hide();
    if (mActionBar != null)
      mActionBar.hide();
    for (View view : mShowOnceArray)
      view.setVisibility(View.GONE);
    mShowOnceArray.clear();
  }



  //----------
  // Extends
  //----------
  private ArrayList<View> mShowOnceArray = new ArrayList<View>();

  public void showOnce(@NonNull View view) {
    mShowOnceArray.add(view);
    view.setVisibility(View.VISIBLE);
    show();
  }

  @Override
  public void setAnchorView(ViewGroup view) {
    super.setAnchorView(view);
  }

  @Override
  public void setIjkEnabled(boolean enabled) {
    setEnabled(enabled);
  }

}
