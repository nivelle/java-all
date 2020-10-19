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

    @Test
    public void assString() throws Exception {
        redisCommandService.set("name", "nivelle");
        String value = redisCommandService.get("name");
        System.err.println("查询结果：" + value);
        Assert.assertEquals("nivelle", value);
    }
}
