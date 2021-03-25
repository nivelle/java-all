### 解题思路
通过HashMap key->数值  value->数值在数组中的下标

一次遍历循环
如target - 当前数值 在map中存在，则返回两者下标。
如不存在，则往map中塞入当前值与下标。

### 代码

```java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer index = map.get(target - nums[i]);
            if (index != null) {
                result[0] = index;
                result[1] = i;
                break;
            } else {
                map.put(nums[i], i);
            }
        }
        return result;
    }
}
```