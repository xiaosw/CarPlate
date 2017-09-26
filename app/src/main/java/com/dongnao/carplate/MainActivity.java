package com.dongnao.carplate;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private ProgressDialog pd;

    private TextView textView;

    private ImageView plateView;
    private ImageView src;

    private void showLoading() {
        if (null == pd) {
            pd = new ProgressDialog(this);
            pd.setIndeterminate(true);
        }
        pd.show();
    }

    private void dismissLoading() {
        if (null != pd) {
            pd.dismiss();
        }
    }

    private int index = 0;
    private int[] ids = {R.drawable.test1, R.drawable.test2, R.drawable.test3, R.drawable.test4,
            R.drawable.test5, R.drawable.test6, R.drawable.test7, R.drawable.test8, R.drawable
            .test9, R.drawable.test10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        src = (ImageView) findViewById(R.id.src);
        textView = (TextView) findViewById(R.id.result);
        plateView = (ImageView) findViewById(R.id.plate);
        src.setImageResource(ids[index]);
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), "car");
//            准备训练SVM车牌识别分类模型...
//            准备训练数据耗时: 4.70335
//            训练完成 耗时: 2493.37188 ,模型保存:xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize
// /resource/HOG_SVM_DATA.xml
//            准备训练ann中文识别模型...
//            准备训练数据耗时: 2.04729
//            训练ann中文识别模型 耗时: 253.23836 ,
// 保存:xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize/resource/HOG_ANN_ZH_DATA.xml
//            准备训练ann字符识别模型...
//            准备训练数据耗时: 8.80205
//            训练ann字符识别模型耗时: 63.18750 ,保存:
// xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize/resource/HOG_ANN_DATA.xml
            String ann = copyAssetsFile("HOG_ANN_DATA.xml", dir);
            String ann_zh = copyAssetsFile("HOG_ANN_ZH_DATA.xml", dir);
            String svm = copyAssetsFile("HOG_SVM_DATA.xml", dir);
            init(svm, ann, ann_zh);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String copyAssetsFile(String name, File dir) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, name);
        if (!file.exists()) {
            InputStream is = getAssets().open(name);
            FileOutputStream fos = new FileOutputStream(file);
            int len;
            byte[] buffer = new byte[2048];
            while ((len = is.read(buffer)) != -1)
                fos.write(buffer, 0, len);
            fos.close();
            is.close();
        }
        return file.getAbsolutePath();
    }

    public void previous(View view) {
        textView.setText(null);
        index--;
        if (index < 0) {
            index = ids.length - 1;
        }
        src.setImageResource(ids[index]);
    }

    public void next(View view) {
        textView.setText(null);
        index++;
        if (index >= ids.length) {
            index = 0;
        }
        src.setImageResource(ids[index]);
    }


    public void click(View view) {
        BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
        bfoOptions.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[index], bfoOptions);

        Bitmap plate = Bitmap.createBitmap(136, 36, Bitmap.Config.ARGB_8888);
        String recognition = recognition(bitmap, plate);
        plateView.setImageBitmap(plate);
        textView.setText(recognition);
        bitmap.recycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        dismissLoading();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void init(String svm, String ann, String ann_zh);

    public native void release();

    public native String recognition(Bitmap bitmap, Bitmap out);


}
