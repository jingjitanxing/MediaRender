package com.gxh.dlnalib;

import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CrashHelper;
import com.geniusgithub.mediarender.util.LogFactory;

import java.io.UnsupportedEncodingException;

public class PlatinumJniProxy {

  private static final CommonLog log = LogFactory.createLog();

  static {
    System.loadLibrary("platinum");
  }

  public static native int startMediaRender(byte[] friendname, byte[] uuid);

  public static native int stopMediaRender();

  public static native boolean responseGenaEvent(int cmd, byte[] value, byte[] data);
  //  public static native boolean enableLogPrint(boolean flag);


  public static int startMediaRender(String friendname, String uuid) {
    if (friendname == null) friendname = "";
    if (uuid == null) uuid = "";
    int ret = -1;
    try {
      ret = startMediaRender(friendname.getBytes("utf-8"), uuid.getBytes("utf-8"));
      log.i("start media render is " + uuid);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      CrashHelper.postException(e);
    }
    return ret;
  }

  public static boolean responseGenaEvent(int cmd, String value, String data) {
    if (value == null) value = "";
    if (data == null) data = "";
    boolean ret = false;
    try {
      log.i("set value " + value);
      ret = responseGenaEvent(cmd, value.getBytes("utf-8"), data.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      CrashHelper.postException(e);
    }
    return ret;
  }
}
