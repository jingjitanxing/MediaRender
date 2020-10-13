package com.geniusgithub.mediarender.video

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.gxh.video_cache.CacheListener
import com.geniusgithub.mediarender.BaseActivity
import com.geniusgithub.mediarender.R
import com.geniusgithub.mediarender.center.DLNAGenaEventBrocastFactory
import com.geniusgithub.mediarender.center.DlnaMediaModel
import com.geniusgithub.mediarender.center.DlnaMediaModelFactory
import com.geniusgithub.mediarender.center.MediaControlBrocastFactory
import com.geniusgithub.mediarender.player.*
import com.geniusgithub.mediarender.util.CommonUtil
import com.geniusgithub.mediarender.util.LogFactory
import kotlinx.android.synthetic.main.activity_ijk_video.*
import kotlinx.android.synthetic.main.media_prepare_layout.*
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.File
import kotlin.math.roundToInt

class IjkVideoActivity: BaseActivity(), CacheListener, MediaControlBrocastFactory.IMediaControlListener,
  PlayerVideoController.LessonRecordVideoListener, Visualizer.OnDataCaptureListener {

  private var mMediaInfo = DlnaMediaModel()

  private val log = LogFactory.createLog()

  private var mNetWorkTimer: AbstractTimer? = null

  private var mPlayPosTimer: AbstractTimer? = null
  private var controller: RenderMediaController? = null

  private var mMediaControlBorcastFactory: MediaControlBrocastFactory? = null

  private var playerType = - 1

  private var mVisualizer: Visualizer? = null

  private val mHandler: Handler = @SuppressLint("HandlerLeak")
  object: Handler() {
    override fun handleMessage(msg: Message?) {
      super.handleMessage(msg)
      when(msg?.what) {
        VideoActivity.REFRESH_CURPOS -> {
          refreshCurPos()
        }
        VideoActivity.HIDE_TOOL -> {

        }
        VideoActivity.EXIT_ACTIVITY -> finish()
        VideoActivity.REFRESH_SPEED -> {
          refreshSpeed()
        }
        VideoActivity.CHECK_DELAY -> {
        }
      }
    }
  }

  fun refreshCurPos() {
    val pos: Int = getVideoPos()
    log.i("refreshCurPos->$pos")
    DLNAGenaEventBrocastFactory.sendSeekEvent(this, pos, mMediaInfo.playingCount)
  }

  private fun getVideoPos(): Int {
    return ijk_video_player.currentPosition
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_ijk_video)
    initData()
    intent?.let {
      resetVideoPlayer(it)
      setCacheVisibility(View.VISIBLE)
    }
  }

  fun refreshSpeed() {
    if (ijk_player_buffer.visibility == View.VISIBLE) {
      val speed: Int = CommonUtil.getSysNetworkDownloadSpeed().roundToInt()
      val showString: String = "$speed KB/" + resources.getString(R.string.second)
      tv_prepare_speed.text = showString
    }
  }

  private fun setCacheVisibility(visibility: Int) {
    ijk_player_buffer.visibility = visibility
    refreshSpeed()
  }

  private fun initData() {

    mMediaControlBorcastFactory = MediaControlBrocastFactory(this)
    mMediaControlBorcastFactory?.register(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    log.i("onDestroy->")
    mMediaControlBorcastFactory?.unregister()
    stopTimer()
  }

  private fun startTimer() {
    mPlayPosTimer?.stopTimer()
    mNetWorkTimer?.stopTimer()

    mPlayPosTimer = SingleSecondTimer(this)
    mPlayPosTimer?.setHandler(mHandler, VideoActivity.REFRESH_CURPOS)
    mNetWorkTimer = SingleSecondTimer(this)
    mNetWorkTimer?.setHandler(mHandler, VideoActivity.REFRESH_SPEED)
    mPlayPosTimer?.startTimer()
    mNetWorkTimer?.startTimer()
  }

  private fun stopTimer() {
    mPlayPosTimer?.stopTimer()
    mNetWorkTimer?.stopTimer()
    ijk_video_player.release(true)
    ijk_video_player.releaseWithoutStop()

    mVisualizer?.enabled = false
    mVisualizer?.release()
    mVisualizer = null
  }

  override fun onStop() {
    log.i("onStop->")
    stopTimer()
    super.onStop()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
    log.i("IjkVideoActivity->onNewIntent")
    intent?.let {
      setCacheVisibility(View.VISIBLE)
      resetVideoPlayer(it)
    }

  }

  private val iBasePlayEngine = object: IBasePlayEngine {
    override fun play() {
      startTimer()
      DLNAGenaEventBrocastFactory.sendPlayStateEvent(this@IjkVideoActivity, mMediaInfo.playingCount)
    }

    override fun stop() {
      stopTimer()
      DLNAGenaEventBrocastFactory.sendPauseStateEvent(this@IjkVideoActivity, mMediaInfo.playingCount)
    }

    override fun pause() {
      stopTimer()
      DLNAGenaEventBrocastFactory.sendPauseStateEvent(this@IjkVideoActivity, mMediaInfo.playingCount)
    }

    override fun skipTo(time: Int) {
      DLNAGenaEventBrocastFactory.sendSeekEvent(this@IjkVideoActivity, time, mMediaInfo.playingCount)
    }

  }

  private fun resetVideoPlayer(thisIntent: Intent) {
    mMediaInfo = DlnaMediaModelFactory.createFromIntent(thisIntent)
    playerType = thisIntent.getIntExtra(DlnaMediaModelFactory.PARAM_PLAYER_TYPE, - 1)
    log.i("resetVideoPlayer->mMediaInfo->$mMediaInfo")
    log.i("resetVideoPlayer->playerType->$playerType")
    DLNAGenaEventBrocastFactory.sendPlayStateEvent(this, mMediaInfo.playingCount)
    if (playerType == 0) {
      //      ijk_video_player.clearTexture()
      ijk_video_player.createRender()
    }
    ijk_video_player.setSoftEncode(false)
    ijk_video_player.changeAspectRaito(0)
    //    val proxy = RenderApplication.getProxy(this)
    //    proxy.registerCacheListener(this , mMediaInfo.url)
    //    val proxyUrl = proxy.getProxyUrl(mMediaInfo.url)
    controller = RenderMediaController(this, false)
    controller?.setListener(mMediaInfo.title, this)
    controller?.basePlayEngine = iBasePlayEngine

    ijk_video_player.setMediaController(controller)
    ijk_video_player.setVideoPath(mMediaInfo.url)
    ijk_video_player.setOnInfoListener {mp: IMediaPlayer?, what: Int, extra: Int ->
      when(what) {
        IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING -> log.d("MEDIA_INFO_VIDEO_TRACK_LAGGING:")
        IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> log.d("MEDIA_INFO_VIDEO_RENDERING_START:$extra")
        IMediaPlayer.MEDIA_INFO_BUFFERING_START -> log.d("MEDIA_INFO_BUFFERING_START:")
        IMediaPlayer.MEDIA_INFO_BUFFERING_END -> log.d("MEDIA_INFO_BUFFERING_END:")
        IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH -> log.d("MEDIA_INFO_NETWORK_BANDWIDTH: $extra")
        IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING -> log.d("MEDIA_INFO_BAD_INTERLEAVING:")
        IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE -> log.d("MEDIA_INFO_NOT_SEEKABLE:")
        IMediaPlayer.MEDIA_INFO_METADATA_UPDATE -> log.d("MEDIA_INFO_METADATA_UPDATE:")
        IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE -> log.d("MEDIA_INFO_UNSUPPORTED_SUBTITLE:")
        IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT -> log.d("MEDIA_INFO_SUBTITLE_TIMED_OUT:")
        IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED -> log.d("MEDIA_INFO_VIDEO_ROTATION_CHANGED: $extra")
        IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START -> log.d("MEDIA_INFO_AUDIO_RENDERING_START:$extra")
      }
      true
    }

    ijk_video_player.setBufferingUpdateListener {mp, percent ->
      log.d("setBufferingUpdateListener->$percent")
    }

    ijk_video_player.setOnCompletionListener {
      log.d("setOnCompletionListener->")
      DLNAGenaEventBrocastFactory.sendStopStateEvent(this, mMediaInfo.playingCount)
      stopTimer()
      delayToExit()
    }

    ijk_video_player.setOnPreparedListener {mp: IMediaPlayer? ->
      controller?.setIjkEnabled(false)
      setCacheVisibility(View.GONE)
      startTimer()
      val duration: Int = mp?.duration?.toInt() ?: 0
      DLNAGenaEventBrocastFactory.sendDurationEvent(this, duration, mMediaInfo.playingCount)
      if (playerType == 0) {
        //        mVisualizer?.enabled = false
        mVisualizer?.release()
        mVisualizer = null
        val maxCR = Visualizer.getMaxCaptureRate()
        mVisualizer = Visualizer(mp?.audioSessionId ?: 0)
        mVisualizer?.enabled = false
        mVisualizer?.captureSize = 256
        mVisualizer?.setDataCaptureListener(this, maxCR / 2, false, true)
        mVisualizer?.enabled = true
      }
      ijk_video_player.start()
      log.d("setOnPreparedListener->")
    }
    ijk_video_player.setOnErrorListener {_: IMediaPlayer?, _: Int, _: Int ->
      log.i("documentVideoView error")
      DLNAGenaEventBrocastFactory.sendStopStateEvent(this, mMediaInfo.playingCount)
      false
    }
    when(playerType) {
      0 -> { //音频
        ijk_audio_player.visibility = View.VISIBLE
        //        ijk_video_player.visibility = View.INVISIBLE
        tv_title.text = mMediaInfo.title
        tv_album.text = mMediaInfo.album
        tv_artist.text = mMediaInfo.artist
        ijk_video_player.setBackgroundColor(Color.parseColor("#00000000"))
      }
      1 -> { //视频
        ijk_audio_player.visibility = View.GONE
        //        ijk_video_player.visibility = View.VISIBLE
        ijk_video_player.setBackgroundColor(Color.parseColor("#000000"))
      }
      else -> {
        finish()
      }
    }
  }

  private fun removeExitMessage() {
    mHandler.removeMessages(VideoActivity.EXIT_ACTIVITY)
  }

  private fun delayToExit() {
    removeExitMessage()
    mHandler.sendEmptyMessageDelayed(VideoActivity.EXIT_ACTIVITY, VideoActivity.EXIT_DELAY_TIME.toLong())
  }

  override fun onCacheAvailable(cacheFile: File?, url: String?, percentsAvailable: Int) {
    log.i("IjkVideoActivity->onCacheAvailable->$percentsAvailable")
  }

  override fun onSeekCommand(time: Int) {
    controller?.show()
    ijk_video_player.seekTo(time)
  }

  override fun onPauseCommand() {
    controller?.show()
    stopTimer()
    ijk_video_player.pause()
  }

  override fun onPlayCommand() {
    controller?.hide()
    startTimer()
    ijk_video_player.start()
  }

  override fun onStopCommand() {
    controller?.show()
    stopTimer()
    ijk_video_player.stopPlayback()
  }

  override fun clickDirectory() {
  }

  override fun finishPage() {

  }

  override fun screenShot() {
  }

  override fun clickVideoSpeed() {
  }

  override fun onFftDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
    p1?.let {
      mp_freq_view.updateVisualizer(it)
    }

  }

  override fun onWaveFormDataCapture(p0: Visualizer?, p1: ByteArray?, p2: Int) {
    p1?.let {
      mp_freq_view.updateVisualizer(it)
    }
  }
}
