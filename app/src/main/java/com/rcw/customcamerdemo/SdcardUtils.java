package com.rcw.customcamerdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rcw on 2017/9/5.
 */

public class SdcardUtils {
    private static String SDPATH;

    public SdcardUtils() {
        SDPATH = Environment.getExternalStorageDirectory() + "";
    }

    public static String getSDPATH() {
        return SDPATH = Environment.getExternalStorageDirectory() + "";
    }

    /**
     * 判断判断内存卡是否存在
     *
     * @return
     */
    public static boolean existSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在  
    }

    /**
     * * 在SD卡上创建目录 
     *
     * @param dirName
     */
    public File creatSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     *  判断SD卡上的文件夹是否存在     
     */
    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    //建立保存头像的路径及名称
    public static File getOutputMediaFile(String fileDir,String mImageName) {// + "/Android/data/"+ getApplicationContext().getPackageName()
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/pajx/stu_photo/"+fileDir);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }else mediaStorageDir.mkdirs();
        }
        File mediaFile;
        // 设置图片名字
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName + ".jpg");
        return mediaFile;
    }

    //建立保存头像的路径及名称
    public static File[] getImageFiles() {// + "/Android/data/"+ getApplicationContext().getPackageName()
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/TEST");
        File[] listFils = new File[0];
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        } else {
            listFils = mediaStorageDir.listFiles();
        }
        return listFils;
    }

    //保存图像
    public static void saveBitmap2SD(Bitmap bitmap, String fileDir, String mImageName) {
        FileOutputStream fos = null;
        File bitmapFile = getOutputMediaFile(fileDir,mImageName);
        if (bitmapFile == null) {
            //Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            fos = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从sd卡获取图片资源
     *
     * @return
     */
    public static List<String> getImagePathFromSD() {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        // 得到该路径文件夹下所有的文件
        File[] files = getImageFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (checkIsImageFile(file.getPath())) {
                imagePathList.add(file.getPath().substring(file.getPath().lastIndexOf("/") + 1));
            }
        }
        // 返回得到的图片列表
        return imagePathList;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    public static boolean fileIsExists(String strFile) {
        try {
            Log.i("Aaaa","path="+strFile);
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }

        return true;
    }


}
