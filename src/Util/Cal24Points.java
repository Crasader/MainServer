package Util;


import java.util.Scanner;
/*
* 利用递归计算四个数能否算出24
* 递归到只剩下一个data数组，返回结果
* */
public class Cal24Points implements GameConstants {

/*
* Data类数据封装了一个数字和对应的字符串表达式，表达式用于输出
* */
    public static String isCanWorkOut24(int num1, int num2, int num3, int num4) {

        Data[] datas = new Data[]{new Data(num1, num1 + ""), new Data(num2, num2 + ""),
                new Data(num3, num3 + ""), new Data(num4, num4 + "")};
        return calculate(datas);
    }

    private static String calculate(Data[] datas) {
        int length = datas.length;
        if (length == 1) {
            if (datas[0].elem == 24) return datas[0].exp;
            return FALSE;
        }

        //从4个数中两两组合，进行递归
        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                int x = datas[i].elem;
                int y = datas[j].elem;
                Data[] newDatas = new Data[length - 1];
                //添加旧的datas给新datas，进行下一轮的递归
                if (length > 2) add(newDatas, datas, i, j);

                String result;

                //加
                newDatas[0] = new Data(x + y, datas[i].getExp() + add + datas[j].getExp());
                result = calculate(newDatas);
                if (!result.equals(FALSE)) return result;

                //乘，加括号
                newDatas[0] = new Data(x * y, left + datas[i].getExp() + right + mul + left + datas[j].getExp() + right);
                result = calculate(newDatas);
                if (!result.equals(FALSE)) return result;

                //减，减数加括号
                if (x > y) {
                    newDatas[0] = new Data(x - y, datas[i].getExp() + '-' + left + datas[j].getExp() + right);
                } else {
                    newDatas[0] = new Data(y - x, datas[j].getExp() + '-' + left + datas[i].getExp() + right);
                }
                result = calculate(newDatas);
                if (!result.equals(FALSE)) return result;

                //除，加括号，要求整除
                if (y != 0 && x % y == 0) {//x/y
                    newDatas[0] = new Data(x / y, left + datas[i].getExp() + right + '/' + left + datas[j].getExp() + right);
                    result = calculate(newDatas);
                    if (!result.equals(FALSE)) return result;
                } else if (x != 0 && y % x == 0) {//y/x
                    newDatas[0] = new Data(y / x, left + datas[j].getExp() + right + '/' + left + datas[i].getExp() + right);
                    result = calculate(newDatas);
                    if (!result.equals(FALSE)) return result;
                }

            }
        }
        return FALSE;

    }

    //给新data数组添加旧数组中除了下标i和j 之外的数
    private static void add(Data[] newDatas, Data[] datas, int i, int j) {
        int length = datas.length;
        for (int m = 0, index = 1; m < length; m++) {
            if (m == i || m == j) continue;//排掉i，j下标
            newDatas[index++] = new Data(datas[m].getElem(), datas[m].getExp());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int a[] = new int[4];
            for (int i = 0; i < 4; i++) a[i] = scanner.nextInt();
            System.out.println(isCanWorkOut24(a[0], a[1], a[2], a[3]));
        }
    }
    private static class Data {
        int elem;
        String exp;

        private Data(int elem, String exp) {
            this.elem = elem;
            this.exp = exp;
        }

        private int getElem() {
            return elem;
        }

        private String getExp() {
            return exp;
        }

    }
}
