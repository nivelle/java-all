package com.nivelle.base.algorithms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/11/25
 */
public class TestSum {
    public static void main(String[] args) {
        List<TestDTO> l = new ArrayList<TestDTO>();
        String[] a = {"183478.5","175587","168995","168085","165654","143421","135372.5","130829","126935","126900.5","124212.5","113729","99062","84436","82333","79723.5","78193","70526","68022","67118.5","62685.5","57327","52759.5","46810.5","44944","31316","26764","26672.5","17809","17635.5","12173.5","10169","9417","6205.5","225"};

        for (int i = 0; i < a.length; i++) {
            TestDTO d = new TestDTO();
            d.setId(i + "");
            d.setNum(new BigDecimal(a[i]));
            l.add(d);
        }
        List<List<TestDTO>> list = Test(l, new BigDecimal("1580000"));
        for (int i = 0; i < list.size(); i++) {
            String str = "";
            for (int j = 0; j < list.get(i).size(); j++) {
                str = str + list.get(i).get(j).getNum() + "+";
            }
            System.out.println("第" + i + "个结果：" + str.substring(0, str.length() - 1));
        }
    }

    public static List<List<TestDTO>> Test(List<TestDTO> dtoParam, BigDecimal samplesNumber) {
        List<List<TestDTO>> reust = new ArrayList<List<TestDTO>>();
        int a = 1;
        int c = 1;
        List<TestDTO> d = null;
        for (int i = 0; i < dtoParam.size(); i++) {
            BigDecimal s = dtoParam.get(i).getNum();
            StringBuffer str = new StringBuffer(dtoParam.get(i).getNum() + "+");//用于控制台打印显示，和逻辑无关
            boolean bb = true;
            while (bb) {
                if (bb = false) {
                    break;
                }
                if (dtoParam.size() == a) {
                    bb = false;
                    break;
                }
                boolean b = true;
                while (b) {
                    if (dtoParam.size() == c) {
                        a++;
                        b = false;
                        c = a;
                        break;
                    }
                    d = new ArrayList<TestDTO>();
                    d.add(dtoParam.get(i));
                    for (int j = c; j < dtoParam.size(); j++) {
                        s = s.add(dtoParam.get(j).getNum());
                        d.add(dtoParam.get(j));
                        str.append(dtoParam.get(j).getNum() + "+");//用于控制台打印显示，和逻辑无关
                        System.out.println(str.substring(0, str.length() - 1));//用于控制台打印显示，和逻辑无关
                        if (s.equals(samplesNumber)) {
                            reust.add(d);
                            break;
                        }
                        if (dtoParam.size() - j == 1) {
                            s = dtoParam.get(i).getNum();
                            str = new StringBuffer(dtoParam.get(i).getNum() + "+");//用于控制台打印显示，和逻辑无关
                            c++;
                            break;
                        }
                    }
                }
            }
        }
        return reust;
    }
}

class TestDTO {
    String id; //id
    BigDecimal num;//数字

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }


}
