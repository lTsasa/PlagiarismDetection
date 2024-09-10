package com.example.testcontroller2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.util.*;


@SuppressWarnings("all")
public class Main {
    public static String path = "src/main/java/com/example/text";

    public static void main(String[] args) throws IOException {
        Scanner scan ;
        Scanner target;
        File file;
        if (args.length == 0) {
             scan = new Scanner(new FileReader(new File(path, "orig.txt")));
             target = new Scanner(new FileReader(new File(path, "orig_0.8_add.txt")));
             file = new File(path, "answer.txt");
        } else if (args.length==3){
             scan = new Scanner(new FileReader(new File(args[0])));
             target = new Scanner(new FileReader(new File( args[1])));
             file = new File(args[2]);
        }else{
            System.out.println("输入参数不正确");
            return;
        }
/*
*
* java -jar Main.jar D:\demo\src\main\java\com\example\text\orig.txt D:\demo\src\main\java\com\example\text\orig_0.8_add.txt D:\demo\src\main\java\com\example\text\answer.txt
*
* */


        String s1 = "";
        String s2 = "";
        String s3;

        while (!(s3 = scan.nextLine()).contains("EOF")) {
            s1 += s3;
        }
        while (!(s3 = target.nextLine()).contains("EOF")) {
            s2 += s3;
        }
        // 词典和分词结果
        List<String> s1Cut = cut(s1);
        List<String> s2Cut = cut(s2);

        // 构建词典
        Set<String> wordSet = new HashSet<>(s1Cut);
        wordSet.addAll(s2Cut);

        Map<String, Integer> wordDict = new HashMap<>();
        int index = 0;
        for (String word : wordSet) {
            wordDict.put(word, index++);
        }

        // 构建词频向量
        int[] s1Vector = buildFrequencyVector(s1Cut, wordDict);
        int[] s2Vector = buildFrequencyVector(s2Cut, wordDict);

//        System.out.println(Arrays.toString(s1Vector));
//        System.out.println(Arrays.toString(s2Vector));
        // 计算余弦相似度
        double similarity = calculateCosineSimilarity(s1Vector, s2Vector);
        System.out.printf("Cosine Similarity: %.2f", similarity);

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(String.format("%.2f", similarity));
        writer.close();
        scan.close();
        target.close();


    }

    private static List<String> cut(String text) {
        // 要分词的文本

        // 创建 IKAnalyzer 实例（使用分词器的默认配置）
        Analyzer analyzer = new IKAnalyzer();
        List<String> list = new ArrayList<>();

        try {
            // 获取 TokenStream
            TokenStream tokenStream = analyzer.tokenStream("field", text);

            // 获取字符属性
            CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);

            // 进行分词
            tokenStream.reset(); // 重置流的状态
            while (tokenStream.incrementToken()) { // 逐词读取
                //  System.out.println(charTermAttr.toString());
                list.add(charTermAttr.toString());
            }
            tokenStream.end(); // 结束
            tokenStream.close(); // 关闭 TokenStream
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                analyzer.close(); // 关闭 Analyzer
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private static int[] buildFrequencyVector(List<String> words, Map<String, Integer> wordDict) {
        int[] vector = new int[wordDict.size()];
        for (String word : words) {
            Integer index = wordDict.get(word);
            if (index != null) {
                vector[index]++;
            }
        }
        return vector;
    }

    private static double calculateCosineSimilarity(int[] vector1, int[] vector2) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }
}
