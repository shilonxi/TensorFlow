package com.example.administrator.tensorflow;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.administrator.tensorflow.tft.Recognition;
import com.example.administrator.tensorflow.tft.TensorFlowImageClassifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 TensorFlow开源实现，这里主要采用Tensorflow Lite，未采用TensorFlow Mobile
 注意assets相关操作，着重注意build.gradle
 */

public class TensorFlow_Activity extends Activity
{
    private EditText address;
    private Button button;
    private ImageView imageView;
    //建立变量
    private String string;
    private FileInputStream fileInputStream;
    private Bitmap bitmap;
    //图片相关
    private TensorFlowImageClassifier tensorFlowImageClassifier;
    private Recognition r;
    private String choice;
    //识别相关

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tensorflow_layout);
        address=findViewById(R.id.address);
        button=findViewById(R.id.button);
        imageView=findViewById(R.id.imageView);
        //获取实例
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                string=address.getText().toString();
                //得到图片地址
                try
                {
                    fileInputStream=new FileInputStream(string);
                }catch (FileNotFoundException f){
                    Toast.makeText(TensorFlow_Activity.this,"no file",Toast.LENGTH_SHORT).show();
                }
                bitmap=BitmapFactory.decodeStream(fileInputStream);
                //读取与生成
                bitmap=Bitmap.createScaledBitmap(bitmap,224,224,true);
                imageView.setImageBitmap(bitmap);
                //显示图片（注意宽高）
                try
                {
                    tensorFlowImageClassifier=new TensorFlowImageClassifier(TensorFlow_Activity.this,224,224);
                }catch (IOException e){
                    Toast.makeText(TensorFlow_Activity.this,"tensorflow error",Toast.LENGTH_SHORT).show();
                }
                final Collection<Recognition> results=tensorFlowImageClassifier.doRecognize(bitmap);
                //识别图片，得到类别结果
                Iterator<Recognition> it=results.iterator();
                int counter=0;
                while(it.hasNext())
                {
                    r=it.next();
                    if(r.getConfidence()>0.5)
                    {
                        choice=r.getTitle();
                        break;
                    }else
                        counter++;
                }
                if(counter==results.size())
                    choice="unknown";
                //这里只取超过50%可能性的最佳结果名称，若无则显示判断不出
                Toast.makeText(TensorFlow_Activity.this,"识别结果为"+choice,Toast.LENGTH_LONG).show();
            }
        });
        //按钮监听
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(tensorFlowImageClassifier!=null)
            tensorFlowImageClassifier.destroyClassifier();
    }
}
