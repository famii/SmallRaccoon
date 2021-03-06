package xmu.dayandnight.smallraccoon.cache.imgCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import xmu.dayandnight.smallraccoon.util.MD5Util;

/**
 * 图片缓存管理类
 * 需要进行改造，将SoftReference改成LRUCache，提高性能（SoftReference在新版本中已经不太适用，建议使用LURCache类）
 * 参见 http://developer.android.com/reference/java/lang/ref/SoftReference.html
 */
public class ImageCacheManager {
    private static final String TAG = "ImageCacheManager";
    private Map<String, SoftReference<Bitmap>> imgMap;
    private Context mContext;

    public ImageCacheManager(Context context) {
        this.mContext = context;
        imgMap = new HashMap<String, SoftReference<Bitmap>>();
    }

    public Bitmap getFromCache(String url) {
        Log.e(TAG, "getFromCache");
        Bitmap bmp = null;
        if (imgMap.containsKey(url)) {
            bmp = getFromMapCache(url);
        }
        if (null == bmp) {
            bmp = getFromFileCache(url);
            putIntoMap(url, bmp);
        }
        return bmp;
    }

    public void putIntoMap(String url, Bitmap bmp) {
        if (null != bmp) {
            imgMap.put(url, new SoftReference<Bitmap>(bmp));
        }
    }

    /**
     * 从内存中获取Bitmap
     *
     * @param path
     * @return
     */
    private Bitmap getFromMapCache(String path) {
        Log.e(TAG, "getFromMapCache");
        Bitmap bmp = null;
        SoftReference<Bitmap> ref = null;
        synchronized (this) {
            ref = imgMap.get(path);
        }
        if (null != ref) {
            bmp = ref.get();
        }
        return bmp;

    }

    /**
     * 从文件缓存中读取图片
     *
     * @param url
     * @return
     */
    private Bitmap getFromFileCache(String url) {
        Bitmap bmp = null;
        String fileName = this.MD5Encode(url);
        Log.e(TAG, "getFromFileCache  " + fileName);
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(fileName);
            bmp = BitmapFactory.decodeStream(fis);
            return bmp;
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 将图片缓存到文件中
     *
     * @param url
     * @param inputStream
     * @return
     */
    public String saveToFile(String url, InputStream inputStream) {
        String fileName = this.MD5Encode(url);// 加密后的文件名
        Log.e(TAG, "writeToFile  " + fileName);
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(inputStream);
            Log.e(TAG, "writeToFile inputStreamSize " + inputStream.available());
            bos = new BufferedOutputStream(mContext.openFileOutput(fileName,
                    Context.MODE_PRIVATE));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e2) {

            }
        }
        return mContext.getFilesDir() + "/" + fileName;
    }

    private String MD5Encode(String src) {
        return MD5Util.MD5Encode(src);
    }

}
