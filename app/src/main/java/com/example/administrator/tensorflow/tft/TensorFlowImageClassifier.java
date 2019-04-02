package com.example.administrator.tensorflow.tft;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.List;

public class TensorFlowImageClassifier
{
    private static final String TAG = "TFImageClassifier";
    private static final String LABELS_FILE = "tflabels.txt";
    //标签文件名
    private static final String MODEL_FILE = "mobilenet_quant_v1_224.tflite";
    //模型文件名
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    //Dimensions of inputs
    private List<String> labels;
    //类别标签
    private ByteBuffer imgData = null;
    //缓冲贮存区
    private byte[][] confidencePerLabel = null;
    //推断结果
    private int[] intValues;
    //预分配缓冲区
    private Interpreter tfLite;
    //TensorFlow Lite

    public TensorFlowImageClassifier(Context context, int inputImageWidth, int inputImageHeight)
            throws IOException {
        this.tfLite = new Interpreter(TensorFlowHelper.loadModelFile(context, MODEL_FILE));
        //实现了对张量（tensor）的基本操作，而整个tensorflow就是以张量为单位处理各种运算
        this.labels = TensorFlowHelper.readLabels(context, LABELS_FILE);
        imgData = ByteBuffer.allocateDirect(
                DIM_BATCH_SIZE * inputImageWidth * inputImageHeight * DIM_PIXEL_SIZE);
        //通过操作系统来创建内存块用作缓冲区，用于存放输入张量（典型的（batch_size, x, y, channel)结构）
        imgData.order(ByteOrder.nativeOrder());
        confidencePerLabel = new byte[1][labels.size()];
        //一个1 x labelList.size()的张量，可认为是一个向量，每一个元素代表模型判断到图片为某一类别的概率，对应于labels
        intValues = new int[inputImageWidth * inputImageHeight];
    }

    public void destroyClassifier() {
        tfLite.close();
    }

    public Collection<Recognition> doRecognize(Bitmap image) {
        TensorFlowHelper.convertBitmapToByteBuffer(image, intValues, imgData);
        //将bitmap中的像素值读出，并放入刚才初始化的imgData中
        long startTime = SystemClock.uptimeMillis();
        tfLite.run(imgData, confidencePerLabel);
        //喂数据得出结果，并将分类的结果存入labelProbArray中
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));
        return TensorFlowHelper.getBestResults(confidencePerLabel, labels);
    }
    //执行图像识别，返回最佳结果集

}