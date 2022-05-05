package com.niici.springcloud;

import com.niici.springcloud.entity.Device;
import com.niici.springcloud.mapper.DeviceMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShardingJdbcApplication.class)
public class ShardingJdbcApplicationTests {

    @Resource
    private DeviceMapper deviceMapper;

    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            Device device = new Device();
            device.setDeviceId((long) i);
            device.setDeviceType(i);
            deviceMapper.insert(device);
        }
    }
}
