package util;

import sun.misc.BASE64Decoder;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecodeUtil {

    private final static byte TAB = 0x09;     // /t
    private final static byte LF = 0x0A;     // /n
    private final static byte CR = 0x0D;     // /r

    private static boolean IsHexDigit(byte b) {
        return ((b >= 0x30 && b <= 0x39) || (b >= 0x41 && b <= 0x46));
    }

    private static byte GetHexValue(byte b) {
        return (byte) Character.digit((char) b, 16);
    }

    /**
     * A method to HeaderDecode quoted printable encoded data.
     * It overrides the same input byte array to save memory. Can be done
     * because the result is surely smaller than the input.
     *
     * @param qp a byte array to HeaderDecode.
     * @return the length of the decoded array.
     */
    private static int Qdecode(byte[] qp) {
        int qplen = qp.length;
        int retlen = 0;

        for (int i = 0; i < qplen; i++) {
            // Handle encoded chars
            if (qp[i] == '=') {
                if (qplen - i > 2) {
                    // The sequence can be complete, check it
                    if (qp[i + 1] == CR && qp[i + 2] == LF) {
                        // soft line break, ignore it
                        i += 2;
                        continue;

                    } else if (IsHexDigit(qp[i + 1]) && IsHexDigit(qp[i + 2])) {
                        // convert the number into an integer, taking
                        // the ascii digits stored in the array.
                        qp[retlen++] = (byte) (GetHexValue(qp[i + 1]) * 16
                                + GetHexValue(qp[i + 2]));

                        i += 2;
                        continue;

                    }
                }
                // In all wrong cases leave the original bytes
                // (see RFC 2045). They can be incomplete sequence,
                // or a '=' followed by non hex digit.
            }

            // RFC 2045 says to exclude control characters mistakenly
            // present (unencoded) in the encoded stream.
            // As an exception, we keep unencoded tabs (0x09)
            if ((qp[i] >= 0x20 && qp[i] <= 0x7f) ||
                    qp[i] == TAB || qp[i] == CR || qp[i] == LF) {
                qp[retlen++] = qp[i];
            }
        }

        return retlen;
    }

    private static String Qdecode(String str, String enc) {
        byte[] qp;

        try {
            qp = str.getBytes(enc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        int len = Qdecode(qp);

        try {
            return new String(qp, 0, len, enc);
        } catch (UnsupportedEncodingException e) {
            return new String(qp, 0, len);
        }
    }

    private static String Qdecode(String str, Charset enc) {
        byte[] qp = str.getBytes(enc);
        int len = Qdecode(qp);

        return new String(qp, 0, len, enc);
    }

    public static String HeaderDecode(String str) {
        Matcher m = Pattern.compile("=\\?(.*?)\\?([bBqQ])\\?(.*?)\\?=").matcher(str);
        if (m.find()) {
            //base64解码
            if (m.group(2).toUpperCase().equals("B")) {
                String decodeString = m.group(3);
                BASE64Decoder decoder = new BASE64Decoder();
                try {
                    byte[] b = decoder.decodeBuffer(decodeString);
                    if (m.group(1).toUpperCase().contains("GB"))
                        decodeString = new String(b, "GBK");
                    else
                        decodeString = new String(b, m.group(1).toUpperCase());
                    str = str.replaceFirst("=\\?.*?\\?[bB]\\?.*?\\?=", Matcher.quoteReplacement(decodeString));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //Quoted-Printable解码
            else if (m.group(2).toUpperCase().equals("Q")) {
                String decodeString = m.group(3);
                try {
                    if (m.group(1).toUpperCase().contains("GB"))
                        decodeString = Qdecode(decodeString, "GBK");
                    else
                        decodeString = Qdecode(decodeString, m.group(1).toUpperCase());
                    str = str.replaceFirst("=\\?.*?\\?[qQ]\\?.*?\\?=", Matcher.quoteReplacement(decodeString));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return HeaderDecode(str);
        } else
            return str;

    }

    public static String ContentDecode(String contentInfo, String content) {
        try {
            if (contentInfo.contains("base64")) {
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] bytes = decoder.decodeBuffer(content);

                if (contentInfo.contains("GB") || contentInfo.contains("gb"))
                    content = new String(bytes, "GBK");

                else if (contentInfo.contains("utf-8") || contentInfo.contains("UTF-8"))
                    content = new String(bytes, StandardCharsets.UTF_8);

                else if (contentInfo.contains("ISO-8859-1") || contentInfo.contains("iso-8859-1"))
                    content = new String(bytes, StandardCharsets.ISO_8859_1);

            } else if (contentInfo.contains("quoted-printable")) {
                if (contentInfo.contains("GB") || contentInfo.contains("gb")) {
                    content = Qdecode(content, "GBK");

                } else if (contentInfo.contains("utf-8") || contentInfo.contains("UTF-8")) {
                    content = Qdecode(content, StandardCharsets.UTF_8);

                } else if (contentInfo.contains("ISO-8859-1") || contentInfo.contains("iso-8859-1")) {
                    content = Qdecode(content, StandardCharsets.ISO_8859_1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

}