# Calculate_Captcha
若依验证码生成器，用于训练用。

## 1.使用
1.自行打包使用

```
git clone https://github.com/bright-angel/Calculate_Captcha
cd Calculate_Captcha
mvn clean package -DskipTests=true
```
然后执行target下的jar文件

2.直接下载使用

https://github.com/bright-angel/Calculate_Captcha/releases

## 2.使用说明

### 1.查看帮助
```bash
java -jar  CalculateCaptcha-1.1.jar -h 
```
结果：
```
usage: java -jar CalculateCaptcha-1.1.jar [-h] -n NUM [-p PATH]
generate Calculate Captcha .

named arguments:
  -h, --help             show this help message and exit
  -n NUM, --num NUM      生成的验证码数量
  -p PATH, --path PATH   验证码生成的位置 (default: train)
```

### 2.生成验证码
如生成200个验证码，目录为`test`（默认生成目录为`train`）
```bash
java -jar  CalculateCaptcha-1.1.jar -n 200 -p test
一共生成200个样本，生成路径test，开始生成中...
样本生成完毕，请查看目录datasets/test
```

支持绝对路径输出，如：
```bash
java -jar CalculateCaptcha-1.1.jar -n 500 -p /root/images_set
```

### 3.输出格式
每个样本集目录结构如下：
```
datasets/<path>/
|---- labels.txt
|---- images
      |---- <32位md5hash>.jpg
      |---- <32位md5hash>.jpg
      |---- <32位md5hash>.jpg
```

`labels.txt` 每行为 `文件名\tlabel`，其中 `\t` 为制表符，label 为原始验证码文本（不做任何字符替换）：
```
603c3c2148eb40401f2d4e7c87dbae5d.jpg	6+7=?
74b1c9d0937967e352b671634bf3bdb6.jpg	5-5=?
6b110f1da1596c26aee5c18aeecf71f8.jpg	2+5=?
```

## 3.说明
直接使用的若依的生成验证码的程序。

生成的验证码文本（如 `0*6=?`）作为训练 label 原样记录到 `labels.txt`，不做任何字符替换（`*`、`/`、`?` 均保留）。

为避免重复和文件系统不兼容问题，图片文件名统一使用随机 hash，不包含验证码文本，因此无需针对特殊字符做文件名替换。

主要代码如下：
```java
import com.google.code.kaptcha.Producer;
import org.apache.commons.codec.digest.DigestUtils;

public class GenCalculateCaptchaTest {
    public static void main(String[] args) throws IOException {
        Producer kaptcha = new CaptchaConfig().getKaptchaBeanMath();
        String capText = kaptcha.createText();
        String capStr = capText.substring(0, capText.lastIndexOf("@"));
        BufferedImage bi = kaptcha.createImage(capStr);
        String fileName = DigestUtils.md5Hex("" + System.nanoTime() + "-" + i) + ".jpg";
        ImageIO.write(bi, "jpg", new File(imagesDir, fileName));
        labelsWriter.write(fileName + "\t" + capStr);
    }
}
```
结果:
```
生成的验证码：0*6=?
图片文件名：  a21a06df03aac093674100949e8ff582.jpg
labels.txt： a21a06df03aac093674100949e8ff582.jpg	0*6=?
```