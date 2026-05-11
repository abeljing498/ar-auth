package com.lyentech.bdc.md.auth.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中文名字转拼音工具类
 *
 * @author zhangcy
 * @date 2021-11-04
 */
@Slf4j
public class PinYinUtils {

    private final static int[] li_SecPosValue = {1601, 1637, 1833, 2078, 2274,
            2302, 2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858,
            4027, 4086, 4390, 4558, 4684, 4925, 5249, 5590};

    private final static String[] lc_FirstLetter = {"a", "b", "c", "d", "e",
            "f", "g", "h", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "w", "x", "y", "z"};
    private final static Pattern p = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * 取得给定汉字串的首字母串,即声母串
     *
     * @param str 给定汉字串
     * @return 声母串
     */
    public static String getAllFirstLetter(String str) {
        if (str == null || str.trim().length() == 0) {
            return "";
        }

        String _str = "";
        for (int i = 0; i < str.length(); i++) {
            _str = _str + getFirstLetter(str.substring(i, i + 1));
        }

        return _str;
    }

    /**
     * 取得给定汉字的首字母,即声母
     *
     * @param chinese 给定的汉字
     * @return 给定汉字的声母
     */
    public static String getFirstLetter(String chinese) {
        if (chinese == null || chinese.trim().length() == 0) {
            return "";
        }
        chinese = conversionStr(chinese, "GB2312", "ISO8859-1");

        if (chinese.length() > 1) // 判断是不是汉字
        {
            int li_SectorCode = (int) chinese.charAt(0); // 汉字区码
            int li_PositionCode = (int) chinese.charAt(1); // 汉字位码
            li_SectorCode = li_SectorCode - 160;
            li_PositionCode = li_PositionCode - 160;
            int li_SecPosCode = li_SectorCode * 100 + li_PositionCode; // 汉字区位码
            if (li_SecPosCode > 1600 && li_SecPosCode < 5590) {
                for (int i = 0; i < 23; i++) {
                    if (li_SecPosCode >= li_SecPosValue[i]
                            && li_SecPosCode < li_SecPosValue[i + 1]) {
                        chinese = lc_FirstLetter[i];
                        break;
                    }
                }
            } else // 非汉字字符,如图形符号或ASCII码
            {
                chinese = conversionStr(chinese, "ISO8859-1", "GB2312");
                chinese = chinese.substring(0, 1);
            }
        }

        return chinese;
    }

    /**
     * 字符串编码转换
     *
     * @param str           要转换编码的字符串
     * @param charsetName   原来的编码
     * @param toCharsetName 转换后的编码
     * @return 经过编码转换后的字符串
     */
    public static String conversionStr(String str, String charsetName, String toCharsetName) {
        try {
            str = new String(str.getBytes(charsetName), toCharsetName);
        } catch (UnsupportedEncodingException ex) {
            System.out.println("字符串编码转换异常：" + ex.getMessage());
        }
        return str;
    }

    /**
     * 首字母大写
     *
     * @param name 参数中文字符串
     * @return result
     * @throws {@link BadHanyuPinyinOutputFormatCombination}
     */
    public static String getChinesePinyinFromName(String name) {
        String result = null;
        try {
            HanyuPinyinOutputFormat pyFormat = new HanyuPinyinOutputFormat();
            pyFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            pyFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            pyFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
            name = name.replace("", " ").trim();
            result = PinyinHelper.toHanyuPinyinString(name, pyFormat, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isChineseName(String name) {
        boolean result = true;
        if (StringUtils.isNotEmpty(name)) {
            String[] strChars = name.split("");
            for (String singleStr : strChars) {
                if (!isContainChinese(singleStr)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isContainChinese(String str) {
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static String getChineseFirstPingYingName(String str) {
        String[] split = str.split("");
        return PinYinUtils.getChinesePinyinFromName(split[0]);
    }

    public static String convert(String chineseName) {
        String nameVar1 = getChinesePinyinFromName(chineseName);
        String nameVar2 = getAllFirstLetter(chineseName);
        String nameVar3 = getChineseFirstPingYingName(chineseName);
        String result = nameVar1 + "," + nameVar2 + "," + nameVar3;
        return result;
    }

    public static boolean isMixedStr(String realname) {
        String[] splitStr = realname.split("");
        List<String> allStrs = Arrays.asList(splitStr);
        List<String> allLastStr = new ArrayList<>();
        boolean hasChinese = false;
        for (String single : splitStr) {
            if (isChineseName(single)) {
                hasChinese = true;
            } else {
                allLastStr.add(single);
            }
        }
        if (hasChinese) {
            if (!CollectionUtils.isEmpty(allStrs) && !CollectionUtils.isEmpty(allLastStr) && allLastStr.size() < allStrs.size()) {
                hasChinese = true;
            }
        }
        return hasChinese;
    }

//
//    public static void main(String[] args) {
//        PinYinUtils pinYinUtils = new PinYinUtils();
//        String chineseName = "胡二三";
//        String nameVar1 = pinYinUtils.getChinesePinyinFromName(chineseName);
//        String nameVar2 = pinYinUtils.getAllFirstLetter(chineseName);
//        String nameVar3 = pinYinUtils.getChineseFirstPingYingName(chineseName);
//        System.out.println(nameVar1);
//        System.out.println(nameVar2);
//        System.out.println(nameVar3);
//    }

    private static PinYinMultiCharactersUtils pinYinMultiCharactersUtils = new PinYinMultiCharactersUtils();

    /**
     * 如果是中文何字符串等混合过来的，只需原样解析，比如：111董aaa飞飞333 ，解析为：111dongaaafeifei333
     *
     * @param realname
     * @return
     */
    public String getMixPinyinStr(String realname) {
        if (StringUtils.isEmpty(realname)) {
            return null;
        }
        String[] splitStr = realname.split("");
        StringBuilder stringBuilder = new StringBuilder();
        int firstIndex = 0;
        for (String single : splitStr) {
            if (isChineseName(single)) {
                //只有第一个中文多音字做解析
                if (firstIndex == 0 && PinYinMultiCharactersUtils.isMultiChineseWord(single)) {
                    String chinesePinyinFromName = PinYinMultiCharactersUtils.getMultiCharactersPinYin(single);
                    stringBuilder.append(chinesePinyinFromName);
                    continue;
                }
                String chinesePinyinFromName = getChinesePinyinFromName(single);
                stringBuilder.append(chinesePinyinFromName);
                firstIndex++;
            } else {
                stringBuilder.append(single);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 首字母大写
     *
     * @param
     * @return result
     * @throws {@link BadHanyuPinyinOutputFormatCombination}
     */
    public static String[] chineseToPinYin(char chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {
        try {
            HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
            outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            if (chineseCharacter >= 32 && chineseCharacter <= 125) {
                return new String[]{
                        String.valueOf(chineseCharacter)
                };
            }
            String[] strings = PinyinHelper.toHanyuPinyinStringArray(chineseCharacter, outputFormat);
            HashSet<String> set = new HashSet<>();
            if (!ObjectUtils.isEmpty(strings)) {
                for (int x = 0; x < strings.length; x++) {
                    set.add(strings[x]);
                }
            }
            return set.toArray(new String[]{});

        } catch (Exception e) {
            throw e;
        }

    }

    public static String chineseToPinYinS(String chineseCharacter) throws BadHanyuPinyinOutputFormatCombination {

        if (StringUtils.isEmpty(chineseCharacter)) {
            return null;
        }
        int counter = 0;
        char[] chs = chineseCharacter.toCharArray();

        String[] result = null;
        HashSet<String> firstList = new HashSet<>();
        for (int i = 0; i < chs.length; i++) {
            String[] arr = chineseToPinYin(chs[i]);
            if (ObjectUtils.isEmpty(arr)) {
                return null;
            } else {
                if (result != null) {
                    if (firstList.size() > 6) {
                        HashSet<String> newSet = new HashSet<>();
                        for (String s : firstList) {

                            newSet.add(s.concat(getChineseFirstPingYingName(String.valueOf(chs[i])).substring(0, 1)));

                        }
                        firstList = newSet;
                    } else {
                        firstList = combine(firstList, arr, false);
                    }
                } else {
                    arr[i] = arr[i].substring(0, 1);
                    result = arr;
                    firstList.add(arr[i]);
                }
            }
        }

        //去重
        String abreviations = null;

        for (String s : firstList) {
            s = s.replace("", " ").trim();
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(abreviations)) {
                abreviations = s.concat(",");
            } else {
                abreviations = abreviations.concat(s).concat(",");
            }
        }

        return abreviations;
    }

    protected static HashSet combine(HashSet<String> t1, String[] t2, boolean full) {

        if (t2 == null || t2.length == 0) {    //为特殊字符，直接跳过
            return t1;
        }
        HashSet<String> retVal = new HashSet<>();
        String s = null;
        int count = 0;

        for (String i : t1) {
            for (String str : t2) {
                if (full) {
                    retVal.add(i.concat(str));
                } else {
                    retVal.add(i.concat(str.substring(0, 1)));
                }
                if (retVal.size() > 8) {
                    return retVal;
                }
            }

        }
        return retVal;
    }
}

