//统计所有小于非负整数 n 的质数的数量。 
//
// 
//
// 示例 1： 
//
// 输入：n = 10
//输出：4
//解释：小于 10 的质数一共有 4 个, 它们是 2, 3, 5, 7 。
// 
//
// 示例 2： 
//
// 输入：n = 0
//输出：0
// 
//
// 示例 3： 
//
// 输入：n = 1
//输出：0
// 
//
// 
//
// 提示： 
//
// 
// 0 <= n <= 5 * 106 
// 
// Related Topics 数组 数学 枚举 数论 
// 👍 710 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 统计小于n的质数数量 {
    public int countPrimes(int n) {
        //return countPrimes1(n);
        return countPrimes2(n);

    }

    /**
     * 暴力解法
     *
     * @param n
     * @return
     */
    public int countPrimes1(int n) {
        int count = 0;
        for (int i = 2; i < n; ++i) {
            count += isPrime(i) ? 1 : 0;
        }
        return count;
    }

    //判断整数n是否是素数，如果n能够整除其他数就不是素数
    //除了1和本身外不再有其他因数的自然数 是质数
    //如果y是x的因数，那么x/y 必然也是x的因数，较小数落在了  [2,sqrt（x)]
    public static boolean isPrime(int n) {
        for (int i = 2; i * i < n; ++i) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 厄拉多塞筛法
     *
     * @param n
     * @return
     */
    public int countPrimes2(int n) {
        /**
         * 定义一个boolean 类型变量，代表这个数是否为素数，初始化是false,代表是素数
         */
        boolean[] isNotPrimes = new boolean[n];
        int count = 0;
        for (int i = 2; i < n; i++) {
            //如果当前数是质数，将质数的倍数删除，一定不是质数
            if (isNotPrimes[i] == false) {
                //是质数
                count++;
                for (int j = 2; i * j < n; j++) {
                    isNotPrimes[i * j] = true;
                }
            }
        }
        return count;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
