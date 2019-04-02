package com.example.administrator.tensorflow.tft;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 工具类，写法基本不变
 */

public class TensorFlowHelper
{
    private static final int RESULTS_TO_SHOW = 3;

    public static MappedByteBuffer loadModelFile(Context context, String modelFile)
            throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFile);
        //assets中文件打开
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        //输入流
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        //FileChannel提供了map方法来把文件影射为内存映像文件
    }
    //大文件读写，这里为加载已训练好的模型文件

    public static List<String> readLabels(Context context, String labelsFile)
    {
        AssetManager assetManager = context.getAssets();
        //assets中文件打开，与上方并为两种方式
        ArrayList<String> result = new ArrayList<>();
        try (InputStream is = assetManager.open(labelsFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
            return result;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read labels from " + labelsFile);
        }
    }
    //这里为加载与模型相对应的标签文件

    public static Collection<Recognition> getBestResults(byte[][] labelProbArray,
                                                         List<String> labelList)
    {
        PriorityQueue<Recognition> sortedLabels = new PriorityQueue<>(RESULTS_TO_SHOW,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        return Float.compare(lhs.getConfidence(), rhs.getConfidence());
                    }
                });
        //Collection API，一个基于优先级堆的无界优先级队列，使用指定的初始容量创建，并根据指定的比较器进行排序

        for (int i = 0; i < labelList.size(); ++i) {
            Recognition r = new Recognition( String.valueOf(i),
                    labelList.get(i), (labelProbArray[0][i] & 0xff) / 255.0f);
            sortedLabels.add(r);
            //入队
            if (r.getConfidence() > 0) {
                Log.d("ImageRecognition", r.toString());
            }
            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
                //获取并删除队首元素（最小元素）
            }
        }

        List<Recognition> results = new ArrayList<>(RESULTS_TO_SHOW);
        for (Recognition r: sortedLabels) {
            results.add(0, r);
        }

        return results;
    }
    //获取最佳的结果集

    public static void convertBitmapToByteBuffer(Bitmap bitmap, int[] intValues, ByteBuffer imgData)
    {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        //使缓冲区为重新读取已包含的数据做好准备：它使限制保持不变，将位置设置为0
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        //获取像素值
        int pixel = 0;
        for (int i = 0; i < bitmap.getWidth(); ++i) {
            for (int j = 0; j < bitmap.getHeight(); ++j) {
                final int val = intValues[pixel++];
                imgData.put((byte) ((val >> 16) & 0xFF));
                imgData.put((byte) ((val >> 8) & 0xFF));
                imgData.put((byte) (val & 0xFF));
            }
        }
    }
    //Bitmap转成ByteBuffer
}
