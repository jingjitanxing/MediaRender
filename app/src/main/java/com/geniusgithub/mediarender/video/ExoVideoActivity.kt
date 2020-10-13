package com.geniusgithub.mediarender.video

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.TextView
import com.geniusgithub.mediarender.BaseActivity
import com.geniusgithub.mediarender.R
import com.geniusgithub.mediarender.RenderApplication
import com.geniusgithub.mediarender.center.DLNAGenaEventBrocastFactory
import com.geniusgithub.mediarender.center.DlnaMediaModel
import com.geniusgithub.mediarender.center.DlnaMediaModelFactory
import com.geniusgithub.mediarender.center.MediaControlBrocastFactory
import com.geniusgithub.mediarender.player.AbstractTimer
import com.geniusgithub.mediarender.player.SingleSecondTimer
import com.geniusgithub.mediarender.util.LogFactory
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.SimpleExoPlayer.Builder
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import kotlinx.android.synthetic.main.activity_exo_video.*
import java.util.*


class ExoVideoActivity: BaseActivity(), MediaControlBrocastFactory.IMediaControlListener, PlaybackPreparer, Player.EventListener,
  Visualizer.OnDataCaptureListener, VideoListener, AudioListener {

  private var mMediaControlBorcastFactory: MediaControlBrocastFactory? = null
  private var mMediaInfo = DlnaMediaModel()
  private val log = LogFactory.createLog()
  private var mNetWorkTimer: AbstractTimer? = null

  private var mPlayPosTimer: AbstractTimer? = null

  private var simpleExoPlayer: SimpleExoPlayer? = null

  private var playerType = - 1
  private var mVisualizer: Visualizer? = null

  private var formatBuilder: java.lang.StringBuilder? = null
  private var formatter: Formatter? = null

  private val mHandler: Handler = @SuppressLint("HandlerLeak")
  object: Handler() {
    override fun handleMessage(msg: Message?) {
      super.handleMessage(msg)
      when(msg?.what) {
        VideoActivity.REFRESH_CURPOS -> {
          updatePosition()
          refreshCurPos()
        }
        VideoActivity.HIDE_TOOL -> {

        }
        VideoActivity.EXIT_ACTIVITY -> finish()
        VideoActivity.REFRESH_SPEED -> {
          //          refreshSpeed()
        }
        VideoActivity.CHECK_DELAY -> {
        }
      }
    }
  }

  fun updatePosition() {
    if (playerType == 0 && formatBuilder != null && formatter != null) {
      position.text = Util.getStringForTime(formatBuilder !!, formatter !!, getVideoPos())
      log.d("zzz getVideoContentPos = ${getVideoContentPos()}")
      log.d("zzz getVideoBufferPos = ${getVideoBufferPos()}")
      val position: Long = currentWindowOffset + getVideoContentPos()
      val bufferedPosition: Long = currentWindowOffset + getVideoBufferPos()
      progress_placeholder.setPosition(C.usToMs(getVideoContentPos()))
      progress_placeholder.setBufferedPosition(C.usToMs(getVideoBufferPos()))
    }
  }

  fun updateDuration() {
    if (playerType == 0 && formatBuilder != null && formatter != null) {
      duration.text = Util.getStringForTime(formatBuilder !!, formatter !!, getVideoDur())
    }
  }


  fun refreshCurPos() {
    val pos: Int = getVideoPos().toInt()
    log.i("refreshCurPos->$pos")
    DLNAGenaEventBrocastFactory.sendSeekEvent(this, pos, mMediaInfo.playingCount)
  }

  private fun getVideoDur(): Long {
    return simpleExoPlayer?.duration ?: 0
  }

  private fun getVideoBufferPos(): Long {
    return simpleExoPlayer?.contentBufferedPosition ?: 0
  }

  private fun getVideoContentPos(): Long {
    return simpleExoPlayer?.contentPosition ?: 0
  }

  private fun getVideoPos(): Long {
    return simpleExoPlayer?.currentPosition ?: 0
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_exo_video)
    initData()
    reset(intent)
  }

  private fun reset(thisIntent: Intent) {
    val mediaInfo = DlnaMediaModelFactory.createFromIntent(intent)
    if ((mediaInfo.title == mMediaInfo.title && mediaInfo.url == mMediaInfo.url) && isBuffering) {
      return
    }
    simpleExoPlayer?.removeListener(this)
    simpleExoPlayer?.removeVideoListener(this)
    simpleExoPlayer?.removeAudioListener(this)
    simpleExoPlayer?.stop()
    simpleExoPlayer?.release()
    stopTimer()
    mMediaInfo = DlnaMediaModelFactory.createFromIntent(thisIntent)
    playerType = thisIntent.getIntExtra(DlnaMediaModelFactory.PARAM_PLAYER_TYPE, - 1)

    DLNAGenaEventBrocastFactory.sendPlayStateEvent(this, mMediaInfo.playingCount)

    val type: Int = Util.inferContentType(mMediaInfo.url)
    log.i("解析出的视频格式为 ： $type")

    val exoController = exo_player.findViewById<View>(R.id.exo_controller)
    val title = exoController.findViewById<TextView>(R.id.exo_tv_title)
    title.text = mMediaInfo.title

    simpleExoPlayer = Builder(this).build()

    val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this,
      Util.getUserAgent(this, RenderApplication::class.java.name))
    val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
      .createMediaSource(Uri.parse(mMediaInfo.url))
    simpleExoPlayer?.addVideoListener(this)
    simpleExoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

    simpleExoPlayer?.addListener(this)
    simpleExoPlayer?.prepare(videoSource, true, false)
    simpleExoPlayer?.playWhenReady = true
    exo_player.setPlaybackPreparer(this)
    exo_player.player = simpleExoPlayer
    if (playerType == 0) {
      simpleExoPlayer?.addAudioListener(this)
      formatBuilder = StringBuilder()
      formatter = Formatter(formatBuilder, Locale.getDefault())
      //      initVisualizer(simpleExoPlayer?.audioSessionId ?: 0)
    }
    when(playerType) {
      0 -> { //音频
        exo_audio_player.visibility = View.VISIBLE
        exo_player.setBackgroundColor(Color.parseColor("#00000000"))
        exo_player.visibility = View.GONE
        simpleExoPlayer?.clearVideoSurface()
        //        exo_player.videoSurfaceView?.visibility = View.GONE
        exo_tv_title.text = mMediaInfo.title
        exo_tv_album.text = mMediaInfo.album
        exo_tv_artist.text = mMediaInfo.artist
      }
      1 -> { //视频
        exo_audio_player.visibility = View.GONE
        exo_player.visibility = View.VISIBLE
        exo_player.setBackgroundColor(Color.parseColor("#000000"))
      }
      else -> {
        finish()
      }
    }
  }

  override fun onAudioSessionId(audioSessionId: Int) {
    //    if (audioSessionId != 0) {
    //      initVisualizer(audioSessionId)
    //    }
  }

  override fun onResume() {
    super.onResume()
    //    exo_player.onResume()
  }

  override fun onPause() {
    super.onPause()
    //    exo_player.onPause()
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

  override fun onDestroy() {
    super.onDestroy()
    stopTimer()
    simpleExoPlayer?.stop()
    simpleExoPlayer?.release()
  }

  private fun stopTimer() {
    mPlayPosTimer?.stopTimer()
    mNetWorkTimer?.stopTimer()
    //    simpleExoPlayer?.stop()
    //    simpleExoPlayer?.release()

    //    mVisualizer?.enabled = false
    //    mVisualizer?.release()
    //    mVisualizer = null
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
    log.i("IjkVideoActivity->onNewIntent")
    intent?.let {
      //      setCacheVisibility(View.VISIBLE)
      reset(it)
    }

  }

  private fun initData() {
    mMediaControlBorcastFactory = MediaControlBrocastFactory(this)
    mMediaControlBorcastFactory?.register(this)
  }

  override fun onSeekCommand(time: Int) {
    log.i("onSeekCommand->${time}")
    simpleExoPlayer?.seekTo(time.toLong())
    exo_player.showController()
  }

  override fun onPauseCommand() {
    log.i("onPauseCommand->")

    simpleExoPlayer?.playWhenReady = false
    exo_player.showController()
    stopTimer()
  }

  override fun onPlayCommand() {
    log.i("onPlayCommand->")
    simpleExoPlayer?.playWhenReady = true
    exo_player.hideController()
    startTimer()
  }

  override fun onStopCommand() {
    log.i("onStopCommand->")
    stopTimer()
    exo_player.showController()
    //    simpleExoPlayer?.playWhenReady = false
    if (simpleExoPlayer?.playbackState == Player.STATE_ENDED) {
      simpleExoPlayer?.seekTo(simpleExoPlayer?.currentWindowIndex ?: 0, C.TIME_UNSET)
    }
  }

  override fun preparePlayback() {
    log.i("preparePlayback->")
    //    simpleExoPlayer?.stop()
  }

  var isBuffering = false
  private var currentWindowOffset: Long = 0

  override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    when(playbackState) {
      Player.STATE_IDLE -> { //这是初始状态，即播放器停止和播放失败时的状态。
        log.i("onPlayerStateChanged->STATE_IDLE  $playWhenReady")
      }
      Player.STATE_BUFFERING -> { //播放器无法立即从当前位置播放。这主要是因为需要加载更多数据。
        log.i("onPlayerStateChanged->STATE_BUFFERING  $playWhenReady")
        isBuffering = playWhenReady
      }
      Player.STATE_READY -> { //播放器可以立即从其当前位置播放。
        log.i("onPlayerStateChanged->STATE_READY  $playWhenReady")
        updateDuration()
        startTimer()
        currentWindowOffset = C.usToMs(getVideoDur())
        progress_placeholder.setDuration(currentWindowOffset)
        val duration: Int = getVideoDur().toInt()
        DLNAGenaEventBrocastFactory.sendDurationEvent(this, duration, mMediaInfo.playingCount)

      }
      Player.STATE_ENDED -> { //播放器完成了所有媒体的播放。
        log.i("onPlayerStateChanged->STATE_ENDED  $playWhenReady")
        DLNAGenaEventBrocastFactory.sendStopStateEvent(this, mMediaInfo.playingCount)
        stopTimer()
        delayToExit()
      }
    }
  }

  private fun initVisualizer(audioSessionId: Int) {
    mVisualizer?.release()
    mVisualizer = null
    val maxCR = Visualizer.getMaxCaptureRate()

    mVisualizer = Visualizer(audioSessionId)
    mVisualizer?.enabled = false
    mVisualizer?.captureSize = 256
    mVisualizer?.setDataCaptureListener(this, maxCR / 2, false, true)
    mVisualizer?.enabled = true
  }

  private fun delayToExit() {
    removeExitMessage()
    mHandler.sendEmptyMessageDelayed(VideoActivity.EXIT_ACTIVITY, VideoActivity.EXIT_DELAY_TIME.toLong())
  }

  private fun removeExitMessage() {
    mHandler.removeMessages(VideoActivity.EXIT_ACTIVITY)
  }

  override fun onPlayerError(error: ExoPlaybackException) {
    log.i("onPlayerError->${error.type}")
    DLNAGenaEventBrocastFactory.sendStopStateEvent(this, mMediaInfo.playingCount)
    when(error.type) {
      ExoPlaybackException.TYPE_SOURCE -> {

      }
    }
  }

  override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
    fft?.let {
      exo_mp_freq_view.updateVisualizer(fft)
    }
  }

  override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
    waveform?.let {
      exo_mp_freq_view.updateVisualizer(waveform)
    }
  }


}
