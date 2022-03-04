package cc.banzhi.runfix.fix;

import androidx.annotation.StringDef;

/**
 * @program: RunFix
 * @description: 修复类型
 * @author: zoufengli01
 * @create: 2021-11-04 20:37
 **/
@StringDef({FixType.DEX, FixType.RES, FixType.SO})
public @interface FixType {
    String DEX = "DEX";
    String RES = "RES";
    String SO = "SO";
}
