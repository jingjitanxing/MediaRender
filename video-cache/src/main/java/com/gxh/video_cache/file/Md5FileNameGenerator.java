package com.gxh.video_cache.file;

import android.text.TextUtils;
import com.gxh.video_cache.ProxyCacheUtils;

/**
 * Implementation of {@link FileNameGenerator} that uses MD5 of url as file name
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class Md5FileNameGenerator implements FileNameGenerator {

  private static final int MAX_EXTENSION_LENGTH = 4;

  @Override public String generate(String url) {
    String subUrl = url.substring(url.indexOf("/") + 1, url.length());
    String extension = getExtension(subUrl);
    String name = ProxyCacheUtils.computeMD5(subUrl);
    return TextUtils.isEmpty(extension) ? name : name + "." + extension;
  }

  private String getExtension(String url) {
    int dotIndex = url.lastIndexOf('.');
    int slashIndex = url.lastIndexOf('/');
    return dotIndex != -1
        && dotIndex > slashIndex
        && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length() ? url.substring(dotIndex + 1,
        url.length()) : "";
  }
}
