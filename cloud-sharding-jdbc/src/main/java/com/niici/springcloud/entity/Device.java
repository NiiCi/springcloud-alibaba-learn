package com.niici.springcloud.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_device")
public class Device {
    private Long deviceId;
    private int deviceType;
}
