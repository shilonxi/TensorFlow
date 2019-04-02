package com.example.administrator.tensorflow.tft;

public class Recognition
{
    private final String id;
    //标签编号
    private final String title;
    //标签名称
    private final Float confidence;
    //标签可信度

    public Recognition(
            final String id, final String title, final Float confidence) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
    }
    //识别结果

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence == null ? 0f : confidence;
    }

    @Override
    public String toString()
    {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        return resultString.trim();
        //得到整体结果，并去掉字符串首尾空格（防止不必要的空格导致错误）
    }
}
