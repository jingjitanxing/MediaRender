package com.geniusgithub.mediarender;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.gxh.video_cache.HttpProxyCacheServer;
import com.gxh.video_cache.file.TotalSizeLruDiskUsage;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CrashHandler;
import com.geniusgithub.mediarender.util.CrashHelper;
import com.geniusgithub.mediarender.util.LogFactory;
import com.geniusgithub.mediarender.util.LogUtil;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import java.io.File;
import java.util.HashMap;

public class RenderApplication extends Application implements ItatisticsEvent {

  private static final CommonLog log = LogFactory.createLog();

  private static RenderApplication mInstance;

  private DeviceInfo mDeviceInfo;

  private HttpProxyCacheServer proxy = null;

  public static HttpProxyCacheServer getProxy(Context context) {
    RenderApplication app = (RenderApplication) context.getApplicationContext();
    if (app.proxy == null) {
      app.proxy = app.newProxy();
      return app.proxy;
    } else {
      return app.proxy;
    }
  }

  private HttpProxyCacheServer newProxy() {
    long maxSize = (1024 * 1024 * 1024L);
    return new HttpProxyCacheServer.Builder(this)
        .cacheDirectory(getProxyFile())
        .diskUsage(new TotalSizeLruDiskUsage(maxSize))
        .maxCacheSize(maxSize)
        .build();
  }

  private File getProxyFile() {
    String cacheName = "Document";
    File documentFile = this.getCacheDir(cacheName);
    if (documentFile == null) {
      documentFile = new File(this.getApplicationContext().getExternalCacheDir(), cacheName);
    }
    return documentFile;
  }

  private void checkLog() {
    LogUtil.getInstance().checkOutDateCommonLog();
    LogUtil.getInstance().checkOutDateCrashLog();
  }


  public synchronized static RenderApplication getInstance() {
    return mInstance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    log.e("RenderApplication onCreate");
    mInstance = this;
    LogUtil.getInstance().setRenderApplication(mInstance);
    uploadCommonLogPath = getAppCacheDir() + "/xlogs/";
    uploadCrashLogPath =
        Environment.getExternalStorageDirectory() + "/MideaRender/CrashLog/";
    mDeviceInfo = new DeviceInfo();
    CrashHandler.getInstance().init(this);

    System.loadLibrary("c++_shared");
    System.loadLibrary("marsxlog");

    String logPath = getAppCacheDir().toString() + "/logs/";

    String cachePath = getAppCacheDir().toString() + "/xlogs/";

    if (BuildConfig.DEBUG) {
      Xlog.appenderOpen(
          Xlog.LEVEL_VERBOSE, Xlog.AppednerModeAsync, cachePath, logPath, "MediaRender",
          10, ""
      );
      Xlog.setConsoleLogOpen(true);
      log.i("xlog init DEBUG");
    } else {
      Xlog.appenderOpen(
          Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logPath, "MediaRender", 10,
          ""
      );
      Xlog.setConsoleLogOpen(true);
      log.i("xlog init Release ");
    }

    Log.setLogImp(new Xlog());
    CrashHelper.init(mInstance);
    deleteFolderFile(getProxyFile().getAbsolutePath(),true);

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                checkLog();
//            }
//        });
//        thread.start();
  }

  public void deleteFolderFile(String filePath, boolean deleteThisPath) {
    if (!TextUtils.isEmpty(filePath)) {
      try {
        File file = new File(filePath);
        if (file.isDirectory()) {// 处理目录
          File files[] = file.listFiles();
          for (int i = 0; i < files.length; i++) {
            deleteFolderFile(files[i].getAbsolutePath(), true);
          }
        }
        if (deleteThisPath) {
          if (!file.isDirectory()) {// 如果是文件，删除
            file.delete();
          } else {// 目录
            if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
              file.delete();
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Boolean existsSdcard() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }

  public File getAppCacheDir() {
    File result;
    if (existsSdcard()) {
      File cacheDir = this.getExternalCacheDir();
      if (cacheDir == null) {
        result = new File(Environment.getExternalStorageDirectory(),
            "Android/data/" + this.getPackageName() + "/cache");
      } else {
        result = cacheDir;
      }
    } else {
      result = this.getCacheDir();
    }
    if (result.exists() || result.mkdirs()) {
      return result;
    } else {
      return null;
    }
  }

  public File getCacheDir(String dirName) {
    File result;
    if (existsSdcard()) {
      File cacheDir = this.getExternalCacheDir();
      if (cacheDir == null) {
        result = new File(Environment.getExternalStorageDirectory(),
            "Android/data/" + this.getPackageName() + "/cache/" + dirName);
      } else {
        result = new File(cacheDir, dirName);
      }
    } else {
      result = new File(this.getCacheDir(), dirName);
    }
    if (result.exists() || result.mkdirs()) {
      return result;
    } else {
      return null;
    }
  }

  public String uploadCommonLogPath;

  public String uploadCrashLogPath;

  public void updateDevInfo(String name, String uuid) {
    mDeviceInfo.dev_name = name;
    mDeviceInfo.uuid = uuid;
  }

  public void setDevStatus(boolean flag) {
    mDeviceInfo.status = flag;
    DeviceUpdateBrocastFactory.sendDevUpdateBrocast(this);
  }

  public DeviceInfo getDevInfo() {
    return mDeviceInfo;
  }

  @Override
  public void onTerminate() {
    log.i("onTerminate--->");
    Log.appenderClose();
    super.onTerminate();
  }

  @Override
  public void onEvent(String eventID) {
    log.i("eventID = " + eventID);
    //TCAgent.onEvent(this, eventID);
  }

  @Override
  public void onEvent(String eventID, HashMap<String, String> map) {
    log.i("eventID = " + eventID);
    //TCAgent.onEvent(this, eventID, "", map);
  }

  public static void onPause(Activity context) {
    //MobclickAgent.onResume(context);
    //TCAgent.onPause(context);
  }

  public static void onResume(Activity context) {
    //MobclickAgent.onResume(context);
    //TCAgent.onResume(context);
  }

  public static void onCatchError(Context context) {
    //TCAgent.setReportUncaughtExceptions(true);
  }
}
