import com.nivelle.container.ContainerBootstrapApplication;
import com.nivelle.container.redis.RedisServiceApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试
 *
 * @author fuxinzhong
 * @date 2020/10/20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ContainerBootstrapApplication.class)
public class RedisTestController {

    @Autowired
    RedisServiceApi redisCommandService;



    //@Test
    public void setGetString() {
        redisCommandService.set("name", "nivelle");
        String value = redisCommandService.get("name");
        System.err.println("查询结果：" + value);
        Assert.assertEquals("nivelle", value);
    }

   //@Test
    public void setPfadd() {
        long result = redisCommandService.pfadd("user2", "nivelle1", "jessy");
        Assert.assertEquals(result, 1L);
    }

    /**
     * 当 PFCOUNT key [key …] 命令作用于单个键时， 返回储存在给定键的 HyperLogLog 的近似基数， 如果键不存在， 那么返回 0 。
     *
     * 当 PFCOUNT key [key …] 命令作用于多个键时， 返回所有给定 HyperLogLog 的并集的近似基数， 这个近似基数是通过将所有给定 HyperLogLog 合并至一个临时 HyperLogLog 来计算得出的。
     *
     * 命令返回的可见集合（observed set）基数并不是精确值， 而是一个带有 0.81% 标准错误（standard error）的近似值。
     */
    //@Test
    public void pfCount() {
        long result = redisCommandService.pfCount("user");
        Assert.assertEquals(result, 2L);
    }

    //@Test
    public void pfMerge(){
        long result = redisCommandService.pfMerge("user","user2");
        Assert.assertEquals(result, 3L);
    }



}
