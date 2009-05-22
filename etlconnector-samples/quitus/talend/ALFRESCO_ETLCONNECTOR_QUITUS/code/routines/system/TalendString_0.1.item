package routines;

import java.util.Random;

public class TalendString {

    /**
     * return Replace the special character(e.g. <,>,& etc) within a string for XML file.
     * 
     * 
     * {talendTypes} String
     * 
     * {Category} TalendString
     * 
     * {param} string("") input: The string with the special character(s) need to be replaced.
     * 
     * {example} replaceSpecialCharForXML("<title>Empire <>Burlesque</title>") # <title>Empire &lt;&gt;Burlesque</title>
     */
    public static String replaceSpecialCharForXML(String input) {
        input = input.replaceAll("&", "&amp;");
        input = input.replaceAll("<", "&lt;");
        input = input.replaceAll(">", "&gt;");
        input = input.replaceAll("'", "&apos;");
        input = input.replaceAll("\"", "&quot;");
        return input;
    }

    /**
     * 
     */
    public static String checkCDATAForXML(String input) {
        if (input.startsWith("<![CDATA[") && input.endsWith("]]>")) {
            return input;
        } else {
            return TalendString.replaceSpecialCharForXML(input);
        }
    }

    /**
     * getAsciiRandomString : Return a randomly generated String
     * 
     * 
     * {talendTypes} String
     * 
     * {Category} TalendString
     * 
     * {param} int(6) length: length of the String to return
     * 
     * {example} getAsciiRandomString(6) # Art34Z
     */
    public static String getAsciiRandomString(int length) {
        Random random = new Random();
        int cnt = 0;
        StringBuffer buffer = new StringBuffer();
        char ch;
        int end = 'z' + 1;
        int start = ' ';
        while (cnt < length) {
            ch = (char) (random.nextInt(end - start) + start);
            if (Character.isLetterOrDigit(ch)) {
                buffer.append(ch);
                cnt++;
            }
        }
        return buffer.toString();
    }

    /**
     * talendTrim: return the trimed String according the padding char and align of the content.
     * 
     * 
     * {talendTypes} String
     * 
     * {Category} TalendString
     * 
     * {param} string("") origin: The original string need to be trimed.
     * 
     * {param} char(' ') padding_char: The padding char for triming.
     * 
     * {param} int(0) align: The alignment of the content in the original strin. Positive int for right, negative int
     * for left and zero for center.
     * 
     * 
     * {example} talendTrim("$$talend open studio$$$$", '$', 0) # talend open studio
     */
    public static String talendTrim(String origin, char padding_char, int align) {
        if (origin.length() < 1) {
            return "";
        }
        if (align > 0) { // Align right, to trim left
            int start = 0;
            char temp = origin.charAt(start);
            while (temp == padding_char) {
                start++;
                if (start == origin.length()) {
                    break;
                }
                temp = origin.charAt(start);
            }
            return origin.substring(start);
        } else if (align == 0) {
            int start = 0;
            char temp = origin.charAt(start);
            while (temp == padding_char) {
                start++;
                if (start == origin.length()) {
                    break;
                }
                temp = origin.charAt(start);
            }
            int end = origin.length();
            temp = origin.charAt(end - 1);
            while (temp == padding_char) {
                if (end == start) {
                    break;
                }
                end--;
                temp = origin.charAt(end - 1);
            }
            return origin.substring(start, end);
        } else { // align left, to trim right
            int end = origin.length();
            char temp = origin.charAt(end - 1);
            while (temp == padding_char) {
                end--;
                if (end == 0) {
                    break;
                }
                temp = origin.charAt(end - 1);
            }
            return origin.substring(0, end);
        }
    }
}
