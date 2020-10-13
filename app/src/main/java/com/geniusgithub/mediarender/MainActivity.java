package com.geniusgithub.mediarender;

import android.os.Bundle;

import com.geniusgithub.mediarender.center.MediaRenderProxy;
import com.geniusgithub.mediarender.proxy.IUdpRecvDelegate;
import com.geniusgithub.mediarender.proxy.UdpProxy;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CrashHelper;
import com.geniusgithub.mediarender.util.DlnaUtils;
import com.geniusgithub.mediarender.util.LogFactory;
import com.geniusgithub.mediarender.util.LogUtil;
import com.teprinciple.mailsender.Mail;
import com.teprinciple.mailsender.MailSender;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author lance
 * @csdn http://blog.csdn.net/geniuseoe2012
 * @github https://github.com/geniusgithub
 */

public class MainActivity extends BaseActivity {

    private static final CommonLog log = LogFactory.createLog();


    private MediaRenderProxy mRenderProxy;
    private RenderApplication mApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {
        mApplication = RenderApplication.getInstance();
        mRenderProxy = MediaRenderProxy.getInstance();


        DlnaUtils.setDevName(this, "gxh");

        final String currentDateTimeString =
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US).format(new Date());

        log.d("currentDateTimeString = " + currentDateTimeString);

        mRenderProxy.startEngine();
    }


}
