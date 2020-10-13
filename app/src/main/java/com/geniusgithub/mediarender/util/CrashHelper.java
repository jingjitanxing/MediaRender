/*
 * Copyright (C) 2017 gxh Technology Co., Ltd. All Rights Reserved.
 */
package com.geniusgithub.mediarender.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.geniusgithub.mediarender.BuildConfig;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;


public class CrashHelper {

  public static void init(Context context) {
    // 获取当前包名
    String packageName = context.getPackageName();
    // 获取当前进程名
    String processName = getProcessName(android.os.Process.myPid());
    // 设置是否为上报进程
    CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
    strategy.setUploadProcess(processName == null || processName.equals(packageName));
//    strategy.setAppChannel(Build.BRAND.toUpperCase());
    strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
      @Override
      public synchronized byte[] onCrashHandleStart2GetExtraDatas(int i, String s, String s1,
                                                                  String s2) {
        return super.onCrashHandleStart2GetExtraDatas(i, s, s1, s2);
      }

      @Override
      public synchronized Map<String, String> onCrashHandleStart(int i, String s, String s1,
                                                                 String s2) {
        return super.onCrashHandleStart(i, s, s1, s2);
      }
    });

    CrashReport.initCrashReport(context.getApplicationContext(), "284fc53fce",
        BuildConfig.DEBUG,
        strategy);
  }

  /**
   * 上传自定义日志
   */
  public static void postException(Throwable throwable) {
    CrashReport.postCatchedException(throwable);
  }

  /**
   * 获取进程号对应的进程名
   *
   * @param pid 进程号
   * @return 进程名
   */
  private static String getProcessName(int pid) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
      String processName = reader.readLine();
      if (!TextUtils.isEmpty(processName)) {
        processName = processName.trim();
      }
      return processName;
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
    return null;
  }

  public static void testCrash() {
    CrashReport.testJavaCrash();
  }

  public static void testAnr() {
    CrashReport.testANRCrash();
  }
}
