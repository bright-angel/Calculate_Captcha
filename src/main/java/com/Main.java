package com;

import com.google.code.kaptcha.Producer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.codec.digest.DigestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {


    public static void main(String[] args)  {
        String rootPath = "datasets";
        ArgumentParser parser = ArgumentParsers.newFor("java -jar CalculateCaptcha.jar").build()
                .defaultHelp(true)
                .description("generate Calculate Captcha .");
        parser.addArgument("-n", "--num")
                .required(true)
                .help("生成的验证码数量");
        parser.addArgument("-p", "--path")
                .setDefault("train")
                .help("验证码生成的位置");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        int num = 2000;
        try {
            num = Integer.parseInt(ns.getString("num"));
        } catch (NumberFormatException e) {
            System.err.printf("error:%s.\nreason: num is %s"
                    , e.getMessage(),ns.getString("num"));
            System.exit(1);
        }

        String dataPath = ns.getString("path");
        // 绝对路径直接作为输出根目录，相对路径仍走默认 datasets 前缀
        String path = new File(dataPath).isAbsolute()
                ? dataPath
                : rootPath + File.separator + dataPath;
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdirs();
        }

        // 图片输出到 images 子目录，label 写入 labels.txt
        File imagesDir = new File(dir, "images");
        if(!imagesDir.exists()){
            imagesDir.mkdirs();
        }
        Path labelsPath = Paths.get(dir.getPath(), "labels.txt");

        System.out.printf("一共生成%s个样本，生成路径%s，开始生成中...\n",num,dataPath);
        Producer DefaultKaptcha = new CaptchaConfig().getKaptchaBeanMath();

        try (BufferedWriter labelsWriter = Files.newBufferedWriter(labelsPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for(int i = 0 ;i < num;i++){
                String capText = DefaultKaptcha.createText();
                String capStr = capText.substring(0, capText.lastIndexOf("@"));
                BufferedImage bi = DefaultKaptcha.createImage(capStr);
                // 不替换原始字符。label 文件系统安全时用 label_hash.jpg，否则仅用 hash.jpg
                String digest = DigestUtils.md5Hex("" + System.nanoTime() + "-" + i);
                String fileName = isSafeFilename(capStr) ? capStr + "_" + digest + ".jpg" : digest + ".jpg";
                String fullName = imagesDir.getPath()+File.separator+fileName;
                try {
                    ImageIO.write(bi, "jpg", new File(fullName));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                labelsWriter.write(fileName + "\t" + capStr);
                labelsWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("样本生成完毕，请查看目录"+path);
    }

    private static boolean isSafeFilename(String label) {
        return label != null && label.length() > 0 && label.length() < 200
                && !label.contains("/") && !label.contains("\\") && !label.contains("\0");
    }
}
