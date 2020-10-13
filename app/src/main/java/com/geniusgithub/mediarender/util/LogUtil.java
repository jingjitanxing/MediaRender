package com.geniusgithub.mediarender.util;

import com.geniusgithub.mediarender.RenderApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by leibingsheng on 2018/10/30.
 */

public class LogUtil {

    private static final CommonLog log = LogFactory.createLog();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

    public static int timeInstance = 10;  //过期时间间隔

    private RenderApplication renderApplication;

    private static LogUtil INSTANCE = new LogUtil();

    public static LogUtil getInstance() {
        return INSTANCE;
    }

    public void setRenderApplication(RenderApplication renderApplication) {
        this.renderApplication = renderApplication;
    }

    /**
     * 检查过期日志，过期就删除，过期间隔为10天
     */
    public void checkOutDateCommonLog() {
        File file = new File(renderApplication.uploadCommonLogPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                List<File> commonlist = Arrays.asList(files);
                Iterator<File> iterator = commonlist.iterator();
                while (iterator.hasNext()) {
                    File item = iterator.next();
                    String fileName = item.getName();
                    if (fileName.endsWith(".xlog")) { // 这里的操作只取app生成的log,防止取时间报错
                        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                        String finalString = fileName.substring(0, fileName.length() - suffix.length() - 1);
                        String dateArray[] = finalString.split("_");
                        long diffDays = dayDiffValue(Integer.valueOf(dateArray[2].substring(0, 4)), Integer.valueOf(dateArray[2].substring(4, 6)), Integer.valueOf(dateArray[2].substring(6, 8)));
                        if (diffDays > timeInstance) {
                            FileHelper.deleteDirectory(renderApplication.uploadCommonLogPath + fileName);
                            log.d("====checkOutDateCommonLog 删除成功，时间差大于十天====");
                        } else {
                            log.d("====checkOutDateCommonLog 删除失败，时间差小于十天====");
                        }
                    } else {
                        FileHelper.deleteDirectory(renderApplication.uploadCommonLogPath + fileName);
                    }
                }
            }
        }
        //generateCommonLogZip();
    }

    public static long dayDiffValue(int year, int month, int day) {
        Calendar calendarNow = Calendar.getInstance(), calendarBefore = Calendar.getInstance();
        calendarNow.set(calendarNow.get(Calendar.YEAR), calendarNow.get(Calendar.MONTH) + 1,
                calendarNow.get(Calendar.DAY_OF_MONTH));
        calendarBefore.set(year, month, day);
        return (calendarNow.getTimeInMillis() - calendarBefore.getTimeInMillis()) / (1000 * 60 * 60 * 24);
    }


    /**
     * 作用：进行解压并返回解压后的zip文件路径名称
     * 解压文件列表中的文件,该种解压方法是将目标文件中的文件列表解压成一个zip包，目标zip包的路径和文件列表的路径相同
     * 只能用这种方法，如果采用整个文件夹解压成zip包容易造成死循环
     */
    public String generateCommonLogZip() {
        String generateZipPath = "";
        List<File> fileList = new ArrayList<>();
        File file = new File(renderApplication.uploadCommonLogPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (fileName.endsWith(".xlog")) {//只要.xlog结尾的，假如有zip包的话，zip在解压成zip可能会形成ANR
                        fileList.add(files[i]);
                    }
                }
            }
        }
        if (fileList.size() != 0) {
            generateZipPath = renderApplication.uploadCommonLogPath + "commonLog" + dateFormat.format(new Date()) + ".zip";
            FileOutputStream fos2 = null;
            try {
                fos2 = new FileOutputStream(new File(generateZipPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fos2 != null) {
                ZipUtils.toZip(fileList, fos2);
            }
        }
        //返回具体的zip文件路径方便上传时找到相应的文件
        return generateZipPath;
    }


    /**
     * 检查过期Crash日志，过期就删除，过期间隔为10天
     */
    public void checkOutDateCrashLog() {
        File file = new File(renderApplication.uploadCrashLogPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                List<File> crashlist = Arrays.asList(files);
                Iterator<File> iterator = crashlist.iterator();
                while (iterator.hasNext()) {
                    File item = iterator.next();
                    String fileName = item.getName();
                    if (fileName.endsWith(".log")) {// 这里的操作只取app生成的log,防止取时间报错
                        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                        String finalString = fileName.substring(0, fileName.length() - suffix.length() - 1);
                        String dateArray[] = finalString.split("_");
                        long diffDays = dayDiffValue(Integer.valueOf(dateArray[1]), Integer.valueOf(dateArray[2]), Integer.valueOf(dateArray[3]));
                        if (diffDays > timeInstance) {
                            FileHelper.deleteDirectory(renderApplication.uploadCrashLogPath + fileName);
                            log.d("====checkOutDateCrashLog  删除成功，时间差大于十天====" + diffDays + "=======" + dateArray[1] + dateArray[2] + dateArray[3]);
                        } else {
                            log.d("==== checkOutDateCrashLog  删除失败，时间差小于十天====" + diffDays + "=======" + dateArray[1] + dateArray[2] + dateArray[3]);
                        }
                    } else {
                        FileHelper.deleteDirectory(renderApplication.uploadCrashLogPath + fileName);
                    }
                }
            }
        }
    }


    /**
     * 解压文件列表中的文件,该种解压方法是将目标文件中的文件列表解压成一个zip包，目标zip包的路径和文件列表的路径相同
     * 只能用这种方法，如果采用整个文件夹解压成zip包容易造成死循环
     */
    public String generateCrashLogZip() {
        String generateZipPath = "";
        List<File> fileList = new ArrayList<>();
        File file = new File(renderApplication.uploadCrashLogPath);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].getName();
                    if (fileName.endsWith(".log")) {//只要.log结尾的，假如有zip包的话，zip在解压成zip可能会形成ANR
                        fileList.add(files[i]);
                    }
                }
            }
        }
        if (fileList.size() != 0) {
            generateZipPath = renderApplication.uploadCrashLogPath + "crashLog" + dateFormat.format(new Date()) + ".zip";
            FileOutputStream fos2 = null;
            try {
                fos2 = new FileOutputStream(new File(generateZipPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fos2 != null) {
                ZipUtils.toZip(fileList, fos2);
            }
        }
        return generateZipPath;
    }

}
