package com.lyentech.bdc.md.auth.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lyentech.bdc.md.auth.util.PinYinUtils.getChinesePinyinFromName;

/**
 * 处理多音字的扩展工具类
 *
 * @author zhangcy
 * @date 2021-12-02
 */
public class PinYinMultiCharactersUtils {

    private static final Logger logger = LoggerFactory.getLogger(PinYinMultiCharactersUtils.class);

    private static Map<String, List<String>> pinyinMap = new HashMap<>();

    private static Map<String, List<String>> otherSpecialWord = new HashMap<>();

    static {
        //这里如果apollo上面没有配置任何的值，默认初始化一些常用的
        otherSpecialWord.put("阿", Arrays.asList("e"));
        otherSpecialWord.put("艾", Arrays.asList("yi"));
        otherSpecialWord.put("膀", Arrays.asList("pang"));
        otherSpecialWord.put("磅", Arrays.asList("pang"));
        otherSpecialWord.put("蚌", Arrays.asList("beng"));
        otherSpecialWord.put("扒", Arrays.asList("ba"));
        otherSpecialWord.put("伯", Arrays.asList("bai"));
        otherSpecialWord.put("暴", Arrays.asList("pu"));
        otherSpecialWord.put("辟", Arrays.asList("bi"));
        otherSpecialWord.put("呗", Arrays.asList("bai"));
        otherSpecialWord.put("秘", Arrays.asList("bi"));
        otherSpecialWord.put("屏", Arrays.asList("bing"));
        otherSpecialWord.put("藏", Arrays.asList("zang"));
        otherSpecialWord.put("刹", Arrays.asList("cha"));
        otherSpecialWord.put("禅", Arrays.asList("shan"));
        otherSpecialWord.put("称", Arrays.asList("chen"));
        otherSpecialWord.put("畜", Arrays.asList("xu"));
        otherSpecialWord.put("帱", Arrays.asList("dao"));
        otherSpecialWord.put("绰", Arrays.asList("chuo"));
        otherSpecialWord.put("龟", Arrays.asList("ci"));
        otherSpecialWord.put("伺", Arrays.asList("ci"));
        otherSpecialWord.put("的", Arrays.asList("di"));
        otherSpecialWord.put("丁", Arrays.asList("zheng"));
        otherSpecialWord.put("得", Arrays.asList("dei"));
        otherSpecialWord.put("读", Arrays.asList("dou"));
        otherSpecialWord.put("度", Arrays.asList("duo"));
        otherSpecialWord.put("恶", Arrays.asList("wu"));
        otherSpecialWord.put("脯", Arrays.asList("fu"));
        otherSpecialWord.put("否", Arrays.asList("pi"));
        otherSpecialWord.put("咖", Arrays.asList("ga"));
        otherSpecialWord.put("伽", Arrays.asList("ga"));
        otherSpecialWord.put("戛", Arrays.asList("ga"));
        otherSpecialWord.put("扛", Arrays.asList("gang"));
        otherSpecialWord.put("蛤", Arrays.asList("ha"));
        otherSpecialWord.put("给", Arrays.asList("ji"));
        otherSpecialWord.put("谷", Arrays.asList("yu"));
        otherSpecialWord.put("柜", Arrays.asList("ju"));
        otherSpecialWord.put("傀", Arrays.asList("kui"));
        otherSpecialWord.put("纶", Arrays.asList("guan"));
        otherSpecialWord.put("莞", Arrays.asList("guan"));
        otherSpecialWord.put("貉", Arrays.asList("hao"));
        otherSpecialWord.put("行", Arrays.asList("hang"));
        otherSpecialWord.put("合", Arrays.asList("ge"));
        otherSpecialWord.put("核", Arrays.asList("hu"));
        otherSpecialWord.put("鹄", Arrays.asList("gu"));
        otherSpecialWord.put("浒", Arrays.asList("xu"));
        otherSpecialWord.put("参", Arrays.asList("shen"));
        otherSpecialWord.put("差", Arrays.asList("chai"));
        otherSpecialWord.put("朝", Arrays.asList("zhao"));
        otherSpecialWord.put("拗", Arrays.asList("niu"));
        otherSpecialWord.put("调", Arrays.asList("diao"));
        otherSpecialWord.put("会", Arrays.asList("kuai"));
        otherSpecialWord.put("珲", Arrays.asList("hun"));
        otherSpecialWord.put("稽", Arrays.asList("qi"));
        otherSpecialWord.put("缉", Arrays.asList("qi"));
        otherSpecialWord.put("藉", Arrays.asList("ji"));
        otherSpecialWord.put("荠", Arrays.asList("ji"));
        otherSpecialWord.put("茄", Arrays.asList("jia"));
        otherSpecialWord.put("校", Arrays.asList("jiao"));
        otherSpecialWord.put("角", Arrays.asList("jue"));
        otherSpecialWord.put("嚼", Arrays.asList("jue"));
        otherSpecialWord.put("降", Arrays.asList("xiang"));
        otherSpecialWord.put("劲", Arrays.asList("jin"));
        otherSpecialWord.put("和", Arrays.asList("huo"));
        otherSpecialWord.put("炅", Arrays.asList("gui"));
        otherSpecialWord.put("还", Arrays.asList("huan"));
        otherSpecialWord.put("卡", Arrays.asList("qia"));
        otherSpecialWord.put("雀", Arrays.asList("qiao"));
        otherSpecialWord.put("壳", Arrays.asList("qiao"));
        otherSpecialWord.put("色", Arrays.asList("shai"));
        otherSpecialWord.put("圈", Arrays.asList("juan"));
        otherSpecialWord.put("楷", Arrays.asList("jie"));
        otherSpecialWord.put("扁", Arrays.asList("pian"));
        otherSpecialWord.put("薄", Arrays.asList("bao"));
        otherSpecialWord.put("卜", Arrays.asList("bu"));
        otherSpecialWord.put("落", Arrays.asList("la"));
        otherSpecialWord.put("了", Arrays.asList("liao"));
        otherSpecialWord.put("露", Arrays.asList("lou"));
        otherSpecialWord.put("靓", Arrays.asList("jing"));
        otherSpecialWord.put("率", Arrays.asList("shuai"));
        otherSpecialWord.put("绿", Arrays.asList("lu"));
        otherSpecialWord.put("抹", Arrays.asList("ma"));
        otherSpecialWord.put("埋", Arrays.asList("man"));
        otherSpecialWord.put("蔓", Arrays.asList("wan"));
        otherSpecialWord.put("脉", Arrays.asList("mo"));
        otherSpecialWord.put("氓", Arrays.asList("meng"));
        otherSpecialWord.put("没", Arrays.asList("mo"));
        otherSpecialWord.put("模", Arrays.asList("mu"));
        otherSpecialWord.put("牟", Arrays.asList("mu"));
        otherSpecialWord.put("粘", Arrays.asList("nian"));
        otherSpecialWord.put("迫", Arrays.asList("pai"));
        otherSpecialWord.put("刨", Arrays.asList("bao"));
        otherSpecialWord.put("胖", Arrays.asList("pan"));
        otherSpecialWord.put("便", Arrays.asList("pian"));
        otherSpecialWord.put("泊", Arrays.asList("po"));
        otherSpecialWord.put("期", Arrays.asList("ji"));
        otherSpecialWord.put("栖", Arrays.asList("xi"));
        otherSpecialWord.put("纤", Arrays.asList("qian"));
        otherSpecialWord.put("券", Arrays.asList("xuan"));
        otherSpecialWord.put("厦", Arrays.asList("xia"));
        otherSpecialWord.put("什", Arrays.asList("shen"));
        otherSpecialWord.put("乘", Arrays.asList("sheng"));
        otherSpecialWord.put("盛", Arrays.asList("cheng"));
        otherSpecialWord.put("省", Arrays.asList("xing"));
        otherSpecialWord.put("石", Arrays.asList("dan"));
        otherSpecialWord.put("泷", Arrays.asList("shuang"));
        otherSpecialWord.put("南", Arrays.asList("na"));
        otherSpecialWord.put("属", Arrays.asList("zhu"));
        otherSpecialWord.put("拓", Arrays.asList("ta"));
        otherSpecialWord.put("沓", Arrays.asList("da"));
        otherSpecialWord.put("汤", Arrays.asList("shang"));
        otherSpecialWord.put("弹", Arrays.asList("tan"));
        otherSpecialWord.put("提", Arrays.asList("di"));
        otherSpecialWord.put("尾", Arrays.asList("yi"));
        otherSpecialWord.put("町", Arrays.asList("ding"));
        otherSpecialWord.put("万", Arrays.asList("mo"));
        otherSpecialWord.put("亡", Arrays.asList("wu"));
        otherSpecialWord.put("系", Arrays.asList("ji"));
        otherSpecialWord.put("塞", Arrays.asList("se"));
        otherSpecialWord.put("宿", Arrays.asList("xiu"));
        otherSpecialWord.put("血", Arrays.asList("xie"));
        otherSpecialWord.put("削", Arrays.asList("xiao"));
        otherSpecialWord.put("拽", Arrays.asList("ye"));
        otherSpecialWord.put("说", Arrays.asList("shui"));
        otherSpecialWord.put("殷", Arrays.asList("yan"));
        otherSpecialWord.put("员", Arrays.asList("yun"));
        otherSpecialWord.put("咱", Arrays.asList("za"));
        otherSpecialWord.put("扎", Arrays.asList("za"));
        otherSpecialWord.put("传", Arrays.asList("zhuan"));
        otherSpecialWord.put("番", Arrays.asList("pan"));
        otherSpecialWord.put("仔", Arrays.asList("zi"));
        otherSpecialWord.put("爪", Arrays.asList("zhua"));
        otherSpecialWord.put("着", Arrays.asList("zhao"));
        otherSpecialWord.put("著", Arrays.asList("zhuo"));
        otherSpecialWord.put("折", Arrays.asList("zhe"));
        otherSpecialWord.put("解", Arrays.asList("xie"));
        otherSpecialWord.put("查", Arrays.asList("zha"));
        otherSpecialWord.put("单", Arrays.asList("shan"));
        otherSpecialWord.put("区", Arrays.asList("ou"));
        otherSpecialWord.put("仇", Arrays.asList("qiu"));
        otherSpecialWord.put("阚", Arrays.asList("han"));
        otherSpecialWord.put("种", Arrays.asList("chong"));
        otherSpecialWord.put("盖", Arrays.asList("ge"));
        otherSpecialWord.put("繁", Arrays.asList("po"));
        otherSpecialWord.put("重", Arrays.asList("chong"));
        otherSpecialWord.put("黑", Arrays.asList("he"));
        otherSpecialWord.put("曾", Arrays.asList("zeng"));
        otherSpecialWord.put("缪", Arrays.asList("miao"));
        otherSpecialWord.put("晟", Arrays.asList("cheng"));
        otherSpecialWord.put("乐", Arrays.asList("yue"));
        otherSpecialWord.put("召", Arrays.asList("shao"));
        otherSpecialWord.put("翟", Arrays.asList("zhai"));
        otherSpecialWord.put("谌", Arrays.asList("shen"));
        otherSpecialWord.put("覃", Arrays.asList("tan"));
        otherSpecialWord.put("长", Arrays.asList("zhang"));
        otherSpecialWord.put("都", Arrays.asList("du"));
        otherSpecialWord.put("莘", Arrays.asList("xin"));
        otherSpecialWord.put("奇", Arrays.asList("ji"));
        otherSpecialWord.put("柏", Arrays.asList("bai"));
        otherSpecialWord.put("大", Arrays.asList("dai"));
        otherSpecialWord.put("朴", Arrays.asList("piao"));
        otherSpecialWord.put("调", Arrays.asList("tiao"));
        otherSpecialWord.put("车", Arrays.asList("ju"));
    }

    public static String toPinyin(String str) {
        try {


            initPinyin("/data/duoyinzi.dic.txt");
//            initPinyin("opt/ar-auth-backend/duoyinzi.txt");

            logger.info("多音字读取文件在这里 进入pu==---------{} ");
            String py = convertChineseToPinyin(str);
            logger.info("多音字读取文件在这里 py==---------{} ");
            System.out.println(str + " = " + py);
            return py;
        } catch (Exception e) {
            logger.error("convert pinyin error,e : {}", e);
            return null;
        }
    }

    /**
     * 通过拆分名字的方式 获取多音字的名字的完整拼音
     *
     * @param chinese
     * @return
     */
    public static String getMultiCharactersPinYin(String chinese) {
        if (StringUtils.isEmpty(chinese)) {
            return null;
        }
        String result = null;
        if (chinese.length() >= 2) {
            String[] nameElements = chinese.split("");
            StringBuilder sb = new StringBuilder();
            for (String str : nameElements) {
                if (isMultiChineseWord(str)) {
                    if (StringUtils.isEmpty(result)) {
                        str = PinYinMultiCharactersUtils.toPinyin(str).concat(" ");
                        result = str;

                    } else {
                        result = result.concat(PinYinMultiCharactersUtils.toPinyin(str)).concat(" ");
                    }
                } else {
                    str = getChinesePinyinFromName(str);
                    if (StringUtils.isEmpty(result)) {
                        result = str.concat(" ");

                    } else {
                        result = result.concat(str).toLowerCase().concat(" ");
                    }
                    continue;
                }
            }
//            String firstName = nameElements[0];
//            if (!isMultiChineseWord(firstName)) {
//                return null;
//            }
//            String secondName = null;
//            for (String str : nameElements) {
//                if (!str.equals(firstName)) {
//                    sb.append(str);
//                }
//            }
//            String name = sb.toString();
//            //获取多音字的拼音
//            String partOne = PinYinMultiCharactersUtils.toPinyin(name);
//            String partTwo = PinYinMultiCharactersUtils.toPinyin(secondName);
//            result = partOne.concat(partTwo).toLowerCase();
        } else {
            result = PinYinMultiCharactersUtils.toPinyin(chinese).concat(" ");
        }
        return result;
    }

    /**
     * 将某个字符串的首字母大写
     *
     * @param str
     * @return
     */
    public static String convertInitialToUpperCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        char[] arr = str.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            if (i == 0) {
                sb.append(String.valueOf(ch).toUpperCase());
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    /**
     * 判断当前中文字是否多音字
     *
     * @param chinese
     * @return
     */
    public static boolean isMultiChineseWord(String chinese) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        char[] arr = chinese.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            if (ch > 128) {
                // 非ASCII码,取得当前汉字的所有全拼
                try {
                    String[] results = PinyinHelper.toHanyuPinyinStringArray(ch, defaultFormat);
                    if (results == null) {
                        //非中文
                        return false;
                    } else {
                        int len = results.length;
                        if (len == 1) {
                            // 不是多音字
                            continue;
                        } else if (results[0].equals(results[1])) {
                            //非多音字 有多个音，默认取第一个
                            if (otherSpecialWord.containsKey(chinese)) {
                                return true;
                            }
                            return false;
                        } else {
                            // 多音字
                            return true;
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    logger.error("BadHanyuPinyinOutputFormatCombination ,e :{}", e);
                }
            }
        }
        return false;
    }

    /**
     * 汉字转拼音 最大匹配优先
     *
     * @param chinese
     * @return
     */
    private static String convertChineseToPinyin(String chinese) {
        StringBuffer pinyin = new StringBuffer();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        char[] arr = chinese.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char ch = arr[i];
            if (ch > 128) {
                // 非ASCII码 取得当前汉字的所有全拼
                try {
                    String[] results = PinyinHelper.toHanyuPinyinStringArray(ch, defaultFormat);
                    if (results == null) {  //非中文
                        return "";
                    } else {
                        int len = results.length;
                        if (len == 1) {
                            // 不是多音字
                            String py = results[0];
                            if (py.contains("u:")) {  //过滤 u:
                                py = py.replace("u:", "v");
                                logger.info("filter u: {}", py);
                            }
                            pinyin.append(convertInitialToUpperCase(py));
                        } else if (results[0].equals(results[1])) {
                            //非多音字 有多个音，取第一个
                            if (otherSpecialWord.containsKey(chinese)) {
                                return otherSpecialWord.get(chinese).get(0);
                            }
                            pinyin.append(convertInitialToUpperCase(results[0]));
                        } else {
                            logger.info("多音字：{}", ch);
                            if (otherSpecialWord.containsKey(chinese)) {
                                pinyin.append(otherSpecialWord.get(chinese).get(0));
                                continue;
                            }
                            int length = chinese.length();
                            boolean flag = false;
                            String s = null;
                            List<String> keyList = null;
                            for (int x = 0; x < len; x++) {
                                String py = results[x];
                                if (py.contains("u:")) {
                                    py = py.replace("u:", "v");
                                    logger.info("filter u ：{}", py);
                                }
                                keyList = pinyinMap.get(py);
                                if (i + 3 <= length) {
                                    //后向匹配2个汉字  大西洋
                                    s = chinese.substring(i, i + 3);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));
                                        flag = true;
                                        break;
                                    }
                                }
                                if (i + 2 <= length) {
                                    //后向匹配 1个汉字  大西
                                    s = chinese.substring(i, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));
                                        flag = true;
                                        break;
                                    }
                                }
                                if ((i - 2 >= 0) && (i + 1 <= length)) {
                                    // 前向匹配2个汉字 龙固大
                                    s = chinese.substring(i - 2, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));
                                        flag = true;
                                        break;
                                    }
                                }
                                if ((i - 1 >= 0) && (i + 1 <= length)) {
                                    // 前向匹配1个汉字   固大
                                    s = chinese.substring(i - 1, i + 1);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));
                                        flag = true;
                                        break;
                                    }
                                }
                                if ((i - 1 >= 0) && (i + 2 <= length)) {
                                    //前向1个，后向1个  固大西
                                    s = chinese.substring(i - 1, i + 2);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                            if (!flag) {
                                //都没有找到，匹配默认的 读音  大
                                s = String.valueOf(ch);
                                for (int x = 0; x < len; x++) {
                                    String py = results[x];
                                    if (py.contains("u:")) {  //过滤 u:
                                        py = py.replace("u:", "v");
                                    }
                                    keyList = pinyinMap.get(py);
                                    if (keyList != null && (keyList.contains(s))) {
                                        pinyin.append(convertInitialToUpperCase(py));//拼音首字母 大写
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    logger.error("BadHanyuPinyinOutputFormatCombination :{}", e);
                }
            } else {
                pinyin.append(arr[i]);
            }
        }
        return pinyin.toString();
    }

    /**
     * 初始化 所有的多音字词组
     *
     * @param fileName
     */
    public static void initPinyin(String fileName) throws IOException {
        if (pinyinMap != null && !pinyinMap.isEmpty()) {
            return;
        }
        String encoding = "GBK";
        File file  =  new File("/opt/ar-auth-backend/duoyinzi.txt");
        if (file.isFile() && file.exists()) {
            InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);
            BufferedReader bufferedReader = new BufferedReader(read);
            String s = null;
            try {
                while ((s = bufferedReader.readLine()) != null) {
                    if (s != null) {
                        String[] arr = s.split("#");
                        String pinyin = arr[0];
                        int length = arr.length;
                        String chinese = null;
                        if (length > 1) {
                            chinese = arr[1];
                        }
                        if (chinese != null) {
                            String[] strs = chinese.split(" ");
                            List<String> list = Arrays.asList(strs);
                            pinyinMap.put(pinyin, list);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("IOException,{}", e);
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("IOException,{}", e);
                }
            }
        }


    }

}


