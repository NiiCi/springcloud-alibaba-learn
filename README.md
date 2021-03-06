## springboot整合rabbitmq
消息队列是典型的：生产者、消费者模型。生产者不断向消息队列中生产消息，消费者不断的从队列中获取消息。因为消息的生产和消费都是异步的，而且只关心消息的发送和接收，没有业务逻辑的侵入，这样就实现了生产者和消费者的解耦。

**支持作者就Star Mua~**

## RabbitMQ 

RabbitMQ是基于AMQP的一款消息管理系统

官网： http://www.rabbitmq.com/

官方教程：http://www.rabbitmq.com/getstarted.html

## 1.1 下载和安装

详见该项目 rabbitmq 目录下的安装帮助文档:

![rabbit下载和安装帮助文档](rabbitmq/assets/rabbit下载和安装帮助文档.png)

## Srping AMPQ

Spring有很多不同的项目，其中就有对AMQP的支持: 

![1527089338661](rabbitmq/assets/1527089338661.png)

Spring AMQP的页面：<http://projects.spring.io/spring-amqp/> 

![1527089365281](rabbitmq/assets/1527089365281.png)

## 依赖
```xml
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
 </dependency>
```

## 配置 application.yml

````
spring:
  rabbitmq:
    host: 148.70.3.235
    username: niici
    password: niici
    virtual-host: /niici
    template:
        retry:
          #启用重试
          enabled: true
            #第一次重试的间隔时长
          initial-interval: 10000ms
            # 最长重试间隔，超过间隔将不在重试
          max-interval: 210000ms
             # 下次重试间隔的倍数，即下次重试时 间隔时间是上次的几倍
          multiplier: 2
    #生产者确认机制，确保消息会正确发送，如果发送失败会有错误回执，从而触发重试
    publisher-confirms: true
````

在SpringAmqp中，对消息的消费者进行了封装和抽象，一个普通的JavaBean中的普通方法，只要通过简单的注解，就可以成为一个消费者。

```java
@Component
public class Listener {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "spring.test.queue", durable = "true"),
            exchange = @Exchange(
                    value = "spring.test.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"#.#"}))
    public void listen(String msg){
        System.out.println("接收到消息：" + msg);
    }
}
```

- `@Componet`：类上的注解，注册到Spring容器
- `@RabbitListener`：方法上的注解，声明这个方法是一个消费者方法，需要指定下面的属性：
  - `bindings`：指定绑定关系，可以有多个。值是`@QueueBinding`的数组。`@QueueBinding`包含下面属性：
    - `value`：这个消费者关联的队列。值是`@Queue`，代表一个队列
    - `exchange`：队列所绑定的交换机，值是`@Exchange`类型
    - `key`：队列和交换机绑定的`RoutingKey`

类似listen这样的方法在一个类中可以写多个，就代表多个消费者。

**详情请查看该项目下的 RebbitMqListener 类**

## 1.2 AmpqTemplate

Spring为AMQP提供了统一的消息处理模板：AmqpTemplate，非常方便的发送消息，其发送方法：

![1527090258083](rabbitmq/assets/1527090258083.png)

红框圈起来的是比较常用的3个方法，分别是：

- 指定交换机、RoutingKey和消息体
- 指定消息
- 指定RoutingKey和消息，会向默认的交换机发送消息


## 1.3 rabbitmq的五种消息模型

RabbitMQ提供了6种消息模型，但是第6种其实是RPC，并不是MQ，因此不予学习。那么也就剩下5种。
但是其实3、4、5这三种都属于订阅模型，只不过进行路由的方式不同。

![1527068544487](rabbitmq/assets/1527068544487.png)

### 1.3.1 基本消息模型

 ![1527070619131](rabbitmq/assets/1527070619131.png)
 
 在上图的模型中，有以下概念：
 
 - P：生产者，也就是要发送消息的程序
 - C：消费者：消息的接受者，会一直等待消息到来。
 - queue：消息队列，图中红色部分。类似一个邮箱，可以缓存消息；生产者向其中投递消息，消费者从其中取出消息。
 
 #### 定义消息生产者
 
 ````
    /**
      * 基本消息模型发送消息
      * @throws InterruptedException
      */
     @Test
     public void  simple() throws InterruptedException {
         String msg = "Rabbitmq simple ....";
         for (int i = 0; i < 10; i++) {
             amqpTemplate.convertAndSend("niici.create.simple.queue",msg);
             Thread.sleep(5000);
         }
     }
````

#### 定义消费者
````
@Component
@Log4j2
public class RabbitMqListener {
    /**
     * 基本消息类型监听
     * @param msg
     * @throws Exception
     */
     @RabbitListener(queuesToDeclare = @Queue(value = "niici.create.simple.queue"))
        public void simpleListener(String msg)throws Exception{
        if (StringUtils.isEmpty(msg)){
            return;
        }
        log.info("SimpleListener listen 接收到消息：" + msg);
    }
}
````

### 1.3.2 消费者的消息确认机制 (AcKnowlage)

消息一旦被消费者接收，队列中的消息就会被删除.

那么问题来了，RabbitMQ 怎么知道消息被接受了呢?

这就要通过消息确认机制（Acknowlege）来实现了。当消费者获取消息后，会向RabbitMQ发送回执ACK，告知消息已经被接收。不过这种回执ACK分两种情况：

- 自动ACK：消息一旦被接收，消费者自动发送ACK
- 手动ACK：消息接收后，不会发送ACK，需要手动调用

这需要看消息的重要性：

- 如果消息不太重要，丢失也没有影响，那么自动ACK会比较方便
- 如果消息非常重要，不容丢失。那么最好在消费完成后手动ACK，否则接收消息后就自动ACK，RabbitMQ就会把消息从队列中删除。如果此时消费者宕机，那么消息就丢失了。

springboot 集成 rabbitmq 的情况下，可以在 application.yml 中设置

````
spring:
  rabbitmq:
    listener:
      direct:
        // 默认为auto 自动ack,manul 为 手动 ack
        acknowledge-mode: manual
      simple:
        acknowledge-mode: manual
````
### 1.3.3 work消息模型

#### 说明

在刚才的基本模型中，一个生产者，一个消费者，生产的消息直接被消费者消费。比较简单。

Work queues，也被称为（Task queues），任务模型。

当消息处理比较耗时的时候，可能生产消息的速度会远远大于消息的消费速度。长此以往，消息就会堆积越来越多，无法及时处理。此时就可以使用work 模型：**让多个消费者绑定到一个队列，共同消费队列中的消息**。队列中的消息一旦消费，就会消失，因此任务是不会被重复执行的。

 ![1527078437166](rabbitmq/assets/1527078437166.png)

角色：

- P：生产者：任务的发布者
- C1：消费者，领取任务并且完成任务，假设完成速度较慢
- C2：消费者2：领取任务并完成任务，假设完成速度快

面试题：避免消息堆积？

1） 采用workqueue，多个消费者监听同一队列。

2）接收到消息以后，而是通过线程池，异步消费

 #### 定义消息生产者
 ````
    /**
      * work 消息模型发送消息
      * @throws InterruptedException
      */
     @Test
     public void  work() throws InterruptedException {
         String msg = "Rabbitmq work ....";
         for (int i = 0; i < 10; i++) {
             amqpTemplate.convertAndSend("niici.create.work.queue",msg+i);
 
         }
     }
````

 #### 定义消息消费者
 ````
    /**
      * work消息类型监听
      * @param msg
      * @throws Exception
      */
     @RabbitListener(queuesToDeclare =@Queue(value = "niici.create.work.queue"))
     public void workListener1(String msg)throws Exception{
         if (StringUtils.isEmpty(msg)){
             return;
         }
         log.info("WorkListener1 listen 接收到消息：" + msg);
         Thread.sleep(5000);
     }
     
     /**
      * 创建两个 work 队列共同消费
      * @param msg
      * @throws Exception
      */
      @RabbitListener(queuesToDeclare =@Queue(value = "niici.create.work.queue"))
        public void workListener2(String msg)throws Exception{
        if (StringUtils.isEmpty(msg)){
            return;
        }
        log.info("WorkListener2 listen 接收到消息：" + msg);
      }
 ````
 
 ### 1.3.4 持久化
 
 如何避免消息丢失？
 
 1) 消费者的手动ACK机制，可以防止消费者丢失消息
 
 2) 但是，如果在消费者消费之前，MQ就宕机了，消息就没了
 
 如何将消息进行持久化？
 
 要将消息持久化，前提是: 队列、交换机都持久化

### 1.3.5 订阅模型分类

### 1.3.6 订阅模型 - Fanout

Fanout，也称为广播。

#### 流程说明

流程图：

![1527086564505](rabbitmq/assets/1527086564505.png)

在广播模式下，消息发送流程是这样的：

- 1）  可以有多个消费者
- 2）  每个**消费者有自己的queue**（队列）
- 3）  每个**队列都要绑定到Exchange**（交换机）
- 4）  **生产者发送的消息，只能发送到交换机**，交换机来决定要发给哪个队列，生产者无法决定。
- 5）  交换机把消息发送给绑定过的所有队列
- 6）  队列的消费者都能拿到消息。实现一条消息被多个消费者消费

 #### 定义消息生产者
 
 ````
     /**
      * fantou 广播消息模型发送消息
      * @throws InterruptedException
      */
     @Test
     public void  fanout() throws InterruptedException {
         String msg = "Rabbitmq fanout ....";
         for (int i = 0; i < 10; i++) {
             //广播消息模型是所有队列都能接收到的，所以没有 routeKey，即 为空
             amqpTemplate.convertAndSend("niici.fanout.exchange","",msg+i);
 
             Thread.sleep(5000);
         }
     }
````

 #### 定义消息消费者
 ````
    /**
      * fanout消息类型监听
      * 创建两个fanout 队列 查看是否广播成功
      * @param msg
      * @throws Exception
      */
     @RabbitListener(bindings = @QueueBinding(
             value = @Queue(value = "niici.create.fanout1.queue", durable = "true"),
             exchange = @Exchange(value = "niici.fanout.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT))
     )
     public void fanoutListener1(String msg)throws Exception{
         if (StringUtils.isEmpty(msg)){
             return;
         }
         log.info("FanoutListener1 listen 接收到消息：" + msg);
     }
     @RabbitListener(bindings = @QueueBinding(
                 value = @Queue(value = "niici.create.fanout2.queue", durable = "true"),
                 exchange = @Exchange(value = "niici.fanout.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.FANOUT)))
         public void fanoutListener2(String msg)throws Exception{
             if (StringUtils.isEmpty(msg)){
                 return;
             }
         log.info("FanoutListener2 listen 接收到消息：" + msg);
     }
 ````

### 1.3.7 订阅模型 - Direct

在Fanout模式中，一条消息，会被所有订阅的队列都消费。但是，在某些场景下，我们希望不同的消息被不同的队列消费。这时就要用到Direct类型的Exchange。

 在Direct模型下：

- 队列与交换机的绑定，不能是任意绑定了，而是要指定一个`RoutingKey`（路由key）
- 消息的发送方在 向 Exchange发送消息时，也必须指定消息的 `RoutingKey`。
- Exchange不再把消息交给每一个绑定的队列，而是根据消息的`Routing Key`进行判断，只有队列的`Routingkey`与消息的 `Routing key`完全一致，才会接收到消息

流程图：

 ![1527087677192](rabbitmq/assets/1527087677192.png)

图解：

- P：生产者，向Exchange发送消息，发送消息时，会指定一个routing key。
- X：Exchange（交换机），接收生产者的消息，然后把消息递交给 与routing key完全匹配的队列
- C1：消费者，其所在队列指定了需要routing key 为 error 的消息
- C2：消费者，其所在队列指定了需要routing key 为 info、error、warning 的消息

 #### 定义消息生产者
 ````
     /**
      * direct 广播消息模型发送消息
      *
      * @throws InterruptedException
      */
     @Test
     public void direct() throws InterruptedException {
         for (int i = 0; i < 10; i++) {
             //direct消息模型是所有队列都能接收到的，所以没有 routeKey，即 为空
             amqpTemplate.convertAndSend("niici.direct.exchange", "delete", "删除成功");
             amqpTemplate.convertAndSend("niici.direct.exchange", "insert", "新增成功");
             amqpTemplate.convertAndSend("niici.direct.exchange", "update", "修改成功");
             Thread.sleep(5000);
         }
     }
````

#### 定义消息消费者
 
 ````
    /**
      * direct 消息类型监听
      * @param msg
      * @throws Exception
      */
     @RabbitListener(bindings = @QueueBinding(
             value = @Queue(value = "niici.create.direct.queue", durable = "true"),
             // 交换机默认的是 direct 类型，默认持久化 为 true，所以不用设置
             exchange = @Exchange(value = "niici.direct.exchange", ignoreDeclarationExceptions = "true",type = ExchangeTypes.DIRECT),
             // 指定路由规则
             key = "insert")
     )
     public void directListener(String msg)throws Exception{
         if (StringUtils.isEmpty(msg)){
             return;
         }
         log.info("DirectListener1 listen 接收到消息：" + msg);
     }
````

### 1.3.8 订阅模型 - Topic

#### 说明

`Topic`类型的`Exchange`与`Direct`相比，都是可以根据`RoutingKey`把消息路由到不同的队列。只不过`Topic`类型`Exchange`可以让队列在绑定`Routing key` 的时候使用通配符！



`Routingkey` 一般都是有一个或多个单词组成，多个单词之间以”.”分割，例如： `item.insert`

 通配符规则：

​         `#`：匹配一个或多个词

​         `*`：匹配不多不少恰好1个词

举例：

​         `audit.#`：能够匹配`audit.irs.corporate` 或者 `audit.irs`

​         `audit.*`：只能匹配`audit.irs`

图示：

 ![1527088518574](rabbitmq/assets/1527088518574.png)

解释：

- 红色Queue：绑定的是`usa.#` ，因此凡是以 `usa.`开头的`routing key` 都会被匹配到
- 黄色Queue：绑定的是`#.news` ，因此凡是以 `.news`结尾的 `routing key` 都会被匹配

 #### 定义消息生产者
 
 ````
    /**
     * topic 消息模型发送消息
     *
     * @throws InterruptedException
     */
    @Test
    public void topic() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            //广播消息模型是所有队列都能接收到的，所以没有 routeKey，即 为空
            amqpTemplate.convertAndSend("niici.topic.exchange", "user.delete", "user 删除成功");
            amqpTemplate.convertAndSend("niici.topic.exchange", "student.delete", "student 删除成功");
            amqpTemplate.convertAndSend("niici.direct.exchange","niici.insert","新增成功");
            amqpTemplate.convertAndSend("niici.direct.exchange","niici.update","修改成功");
            Thread.sleep(5000);
        }
    }
````

 #### 定义消息消费者

````
    /**
     * topic 消息类型监听
     * @param msg
     * @throws Exception
     */
    @RabbitListener(
            bindings = @QueueBinding(
            value = @Queue(value = "niici.create.topic.queue", durable = "true"),
            exchange = @Exchange(value = "niici.topic.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"user.#"})
    )
    public void topicListener1(String msg )throws Exception{
        if (StringUtils.isEmpty(msg)){
            return;
        }
        log.info("TopicListener1 listen 接收到消息：" + msg);
    }

    /**
     * topic 消息类型监听
     * @param msg
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "niici.create.topic.queue", durable = "true"),
            exchange = @Exchange(value = "niici.topic.exchange", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"student.#"})
    )
    public void topicListener2(String msg )throws Exception{
        if (StringUtils.isEmpty(msg)){
            return;
        }
        log.info("TopicListener2 listen 接收到消息：" + msg);
    }
 ````

#### 如何手动ACK？
#### 说明

`手动ACK`需要在application.yml中配置ack模式为手动, 并配置消息发送失败, 放回队列。
````
listener:
      direct:
        acknowledge-mode: manual
 # 消息发送失败时，返回到队列
publisher-returns: true
````

Code:
````
@RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "niici.topic.student.queue", durable = "true"),
                    exchange = @Exchange(
                            value = "niici.topic.exchange",
                            ignoreDeclarationExceptions = "true",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = {"student.#"}))
    @RabbitHandler
    public void topicListenAck(String msg, Channel channel, Message message) throws IOException {
        // 消息在队列中对应的索引
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            /**
             * 无异常确认消息
             * channel.basicAck(long deliveryTag, boolean multiple);
             * deliveryTag: 取出来的消息在队列中的索引
             * multiple: true表示一次性的将小于deliveryTag的值进行ack
             * 如果当前deliveryTag为5, 则确认5及5之前的消息, 一般为false
             */
            channel.basicAck(deliveryTag, false);
            System.out.println("topic接收到消息：" + msg);
        } catch (IOException e) {
            /**
             * 异常拒收消息
             * basicNack(long deliveryTag, boolean multiple, boolean requeue)
             * requeue: true为将消息返回到队列, 并重新发送给消费者
             *          false则丢弃消息
             */
            channel.basicNack(deliveryTag, false , true);
        }
    }
````


#### 延迟队列
#### 说明

`延迟队列`其实是死信队列在消息超时时的场景。 通过给消息设置超时时间推送到队列的方式来实现。

队列创建参数：x-参数, 如x-message-ttl --- 参数都有哪些？

手动设置消息超时时间带来的问题：消息堆积

Rabbitmq只会检查第一个消息是否过期, 如果第一个消息超时时间较长，第二个消息超时时间较短，则不能实现第二个消息先执行超时，导致消息堆积。

#### 解决方案

使用插件方式来实现延迟队列：rabbitmq_delayed_message_exchange-3.8.0.ez

下载地址：https://www.rabbitmq.com/community-plugins.html

使用方法：

- 将插件拷贝到 `/usr/lib/rabbitmq/lib/rabbitmq_server-3.8.8/plugins/` 目录
- `rabbitmq-plugins enable rabbitmq_delayed_message_exchange`
- 重启rabbitmq服务

重启完成后, 在rabbitmq的交换机页面, 可以看到交换机类型新增了延迟交换机类型

<font color="red">生产者可以直接推送消息到延迟交换机, 由队列通过路由key绑定的方式来实现延迟队列, 无需再指定死信队列</font>

![img_1.png](img_1.png)


````
[root@node1 ~]# rabbitmq-plugins enable rabbitmq_delayed_message_exchange
Enabling plugins on node rabbit@node1:
rabbitmq_delayed_message_exchange
The following plugins have been configured:
  rabbitmq_delayed_message_exchange
  rabbitmq_management
  rabbitmq_management_agent
  rabbitmq_web_dispatch
Applying plugin configuration to rabbit@node1...
The following plugins have been enabled:
  rabbitmq_delayed_message_exchange

started 1 plugins.
````


Code:

- 声明队列、交换机、绑定关系
````java
    /**
     * 配置延迟交换机
     * @return
     */
    @Bean
    public CustomExchange delayExchange() {
        // CustomExchange 允许自定义交换机类型
        HashMap<String, Object> args = new HashMap<>();
        // 指定延迟队列的类型
        args.put("x-delayed-type", "topic");
        return new CustomExchange("niici.delay.exchange", "x-delayed-message", true, false, args);
    }

    /**
     * 定义一个延迟队列
     * @return
     */
    @Bean
    public Queue delayQueue() {
        return new Queue("niici.delay.queue");
    }

    @Bean
    public Binding delayQueueBind(@Qualifier("delayQueue") Queue queue, @Qualifier("delayExchange") Exchange exchange) {
        // 将队列绑定到指定的交换机上, 并指定路由key
        return BindingBuilder.bind(queue).to(exchange).with("delay.#").noargs();
    }
````

- 生产者
```java
    /**
     * 基于插件的延迟队列测试
     *
     * @throws InterruptedException
     */
    @Test
    public void delay() throws InterruptedException {
        /**
         * 创建两条延迟消息，一条设置5s超时时间，一条设置1s，测试是否1s的消息先被监听到
         */
        amqpTemplate.convertAndSend("niici.delay.exchange", "delay.five", "超时时间5s的消息", message -> {
            message.getMessageProperties().setDelay(5000);
            return message;
        });

        amqpTemplate.convertAndSend("niici.delay.exchange", "delay.one", "超时时间1s的消息", message -> {
            message.getMessageProperties().setDelay(1000);
            return message;
        });
        // 消息发送完成后, 等待10s, 让监听器去监听, 在控制台打印结果
        Thread.sleep(10000);
    }
```

- 消费者
````java
    /**
     * 基于插件的延迟队列监听
     * @param msg
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "niici.delay.queue", durable = "true", ignoreDeclarationExceptions = "true"),
                    exchange = @Exchange(
                            value = "niici.delay.exchange",
                            ignoreDeclarationExceptions = "true",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = {"delay.#"}))
    @RabbitHandler
    public void delayListen(String msg, Channel channel, Message message) throws IOException {
        // 消息在队列中对应的索引
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            System.out.println("基于插件的延迟队列监听器到消息: " + msg);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            channel.basicNack(deliveryTag, false, true);
        }
    }
````

- 结果
  
  基于插件的延迟队列监听器到消息: 超时时间1s的消息
  
  基于插件的延迟队列监听器到消息: 超时时间5s的消息

### <font color="red">幂等性</font>


#### 解决方案：

MQ消费者的幂等性的解决一般使用全局ID或者写一个唯一标识比如时间戳、UUID
或者可按自己的规则生成一个全局唯一id，每次消费消息时用该id先判断消息是否已消费过。

#### 消费端的幂等性保障

在海量订单生成的业务高峰期, 生产者可能会重复发送消息, 这时消费端就需要实现幂等性，
这就意味着消息永远不能被消费多次，即使收到了一样的消息。

<font color="red">业界主流的幂等性有两种操作：
- 唯一ID + 指纹码机制, 利用数据库主键去重
- 利用redis的原子性去实现
  </font>


#### 利用Redis的原子性实现

利用redis执行setnx命令, 天然具备幂等性, 从而实现不重复消费。


### <font color="red">集群搭建</font>

#### 环境准备
已启动三个节点的rabbitmq-server

#### 搭建流程
- 修改三个节点的hosts文件
````shell
    vim /etc/hosts
    192.168.18.164 node1
    192.168.18.165 node2
    192.168.18.166 node3
````
- 确认每个节点的cookie文件相同
```shell
    scp /var/lib/rabbitmq/.erlang.cookie root@node2:/var/lib/rabbitmq/.erlang.cookie
    scp /var/lib/rabbitmq/.erlang.cookie root@node3:/var/lib/rabbitmq/.erlang.cookie
```
- 启动 RabbitMQ 服务,顺带启动 Erlang 虚拟机和 RbbitMQ 应用服务(在三台节点上分别执行以
  下命令)
```shell
    rabbitmq-server -detached
```
- 在节点2执行
```shell
    rabbitmqctl stop_app (rabbitmqctl stop 会将 Erlang 虚拟机关闭, rabbitmqctl stop_app 只关闭 RabbitMQ 服务)
    rabbitmqctl reset
    rabbitmqctl join_cluster rabbit@node1
    rabbitmqctl start_app (只启动应用服务)
```
- 在节点3执行
```shell
    rabbitmqctl stop_app
    rabbitmqctl reset
    rabbitmqctl join_cluster rabbit@node1
    rabbitmqctl start_app
```
- 查看集群状态(例子供参考)
```shell
    rabbitmqctl cluster_status
    [root@node3 ~]# rabbitmqctl cluster_status
    Cluster status of node rabbit@node3 ...
    Basics
    
    Cluster name: rabbit@node1
    
    Disk Nodes
    
    rabbit@node1
    rabbit@node2
    rabbit@node3
    
    Running Nodes
    
    rabbit@node1
    rabbit@node2
    rabbit@node3
    
    Versions
    
    rabbit@node1: RabbitMQ 3.8.8 on Erlang 21.3
    rabbit@node2: RabbitMQ 3.8.8 on Erlang 21.3
    rabbit@node3: RabbitMQ 3.8.8 on Erlang 21.3
    
    Maintenance status
    
    Node: rabbit@node1, status: not under maintenance
    Node: rabbit@node2, status: not under maintenance
    Node: rabbit@node3, status: not under maintenance
    
    Alarms
    
    (none)
    
    Network Partitions
    
    (none)
    
    Listeners
    
    Node: rabbit@node1, interface: [::], port: 15672, protocol: http, purpose: HTTP API
    Node: rabbit@node1, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
    Node: rabbit@node1, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
    Node: rabbit@node2, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
    Node: rabbit@node2, interface: [::], port: 15672, protocol: http, purpose: HTTP API
    Node: rabbit@node2, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
    Node: rabbit@node3, interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
    Node: rabbit@node3, interface: [::], port: 15672, protocol: http, purpose: HTTP API
    Node: rabbit@node3, interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
    
    Feature flags
    
    Flag: drop_unroutable_metric, state: enabled
    Flag: empty_basic_get_metric, state: enabled
    Flag: implicit_default_bindings, state: enabled
    Flag: maintenance_mode_status, state: enabled
    Flag: quorum_queue, state: enabled
    Flag: virtual_host_metadata, state: enabled
```
- 重新设置用户
```shell
    rabbitmqctl add_user admin admin
    rabbitmqctl set_user_tags admin administrator
    rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
```
- 解除集群节点
```shell
    rabbitmqctl stop_app
    rabbitmqctl reset
    rabbitmqctl start_app
    rabbitmqctl cluster_status
    rabbitmqctl forget_cluster_node rabbit@node2(node1 机器上执行)
    rabbitmqctl forget_cluster_node rabbit@node3(node1 机器上执行)
```

### <font color="red">镜像队列</font>
引入镜像队列(Mirror Queue)的机制，可以将队列镜像到集群中的其他 Broker 节点之上，如果集群中
的一个节点失效了，队列能自动地切换到镜像中的另一个节点上以保证服务的可用性。


- 命令行配置方式
```shell
    rabbitmqctl set_policy [-p Vhost] Name Pattern Definition [Priority]
    
    -p Vhost: 可选参数, 针对指定vhost下的queue进行设置
    Name: policy的名称
    Pattern: queue的匹配模式(正则表达式)
    Definition: 镜像定义, 包括三个部分ha-mode, ha-params, ha-sync-mode
        ha-mode: 指明镜像队列的模式, 有效值为 all/exactly/nodes
            all: 表示在集群中所有的节点上进行镜像
            exactly: 表示在指定个数的节点上进行镜像, 节点的个数由ha-params指定
            nodes: 表示在指定的节点上进行镜像, 节点名称通过ha-params指定
        ha-params: ha-mode模式需要用到的参数
        ha-sync-mode: 进行队列中消息的同步方式, 有效值为automatic和manual
        priority: 可选参数, policy的优先级        
```

```shell
    rabbitmqctl set_policy --priority 0 --apply-to queues mirror_queue "niici.*" '{"ha-mode":"exactly","ha-params":3,"ha-sync-mode":"automatic"}'
```

#### 执行结果
![img_2.png](img_2.png)
![img_3.png](img_3.png)

### 高可用负载均衡

### Haproxy + keepalive 实现高可用负载均衡

#### Haproxy 实现负载均衡

Haproxy下载地址：
````http request
    https://src.fedoraproject.org/repo/pkgs/haproxy/ --本次使用2.5.0版本
````

安装流程：
-   安装gcc编译环境
```shell
    yum -y install make gcc gcc-c++ openssl-devel
```
-   解压源码包
```shell
    tar -zxvf haproxy-2.5.0.tar.gz -C /opt/module/
```
-   查看内核版本
```shell
    uname -r
```
-   make && make install
```shell
    cd haproxy-2.5.0
    make TARGET=linux3100
    make install
```
-   将可执行二进制文件拷贝到/usr/sbin目录
```shell
    cp -rf /opt/module/haproxy-2.5.0/haproxy /usr/sbin
```
-   检查版本
```shell
    [root@node1 haproxy-2.5.0]# haproxy -version
    HAProxy version 2.5.0-f2e0833 2021/11/23 - https://haproxy.org/
```
-   创建配置文件(两个节点)
```shell
    vim /etc/haproxy/haproxy.cfg
    
    global
      #日志输出配置，所有日志都记录在本机，通过local0输出
      log 127.0.0.1 local0 info
      #最大连接数
      maxconn 10240
      #以守护进程方式运行
      daemon
    
    defaults
      #应用全局的日志配置
      log global
      mode http
      #超时配置
      timeout connect 5000
      timeout client 5000
      timeout server 5000
      timeout check 2000
    
    listen http_front #haproxy的客户页面(bind 对应的ip需要修改)
      bind 192.168.18.165:8888
      mode http
      option httplog
      stats uri /haproxy
      stats auth admin:123456
      stats refresh 5s
      stats enable
    
    listen haproxy #负载均衡的名字
      bind 0.0.0.0:5666 #对外提供的虚拟的端口
      option tcplog
      mode tcp
      #轮询算法
      balance roundrobin
      server rabbit1 192.168.18.164:5672 check inter 5000 rise 2 fall 2
      server rabbit2 192.168.18.165:5672 check inter 5000 rise 2 fall 2
      server rabbit3 192.168.18.166:5672 check inter 5000 rise 2 fall 2
```
-   启动haproxy
```shell
    haproxy -f /etc/haproxy/haproxy.cfg
    lsof -i:8888 # 查看8888端口是否已被监听
```
-   访问haproxy
```shell
    http://192.168.18.165:8888/haproxy
```
![img_4.png](img_4.png)

#### keepalived 实现高可用

keepalived下载地址：
````http request
    https://www.keepalived.org/download.html -- 本次使用2.2.4版本(也可以使用yum安装)
````

安装流程：
-   安装gcc编译环境
```shell
    yum -y install make gcc gcc-c++ openssl-devel
```
-   解压源码包
```shell
    tar -zxvf keepalived-2.2.4.tar.gz -C /usr/local/src
```
-   构建可执行二进制文件
```shell
    cd /usr/local/src/keepalived-2.2.4/
    # 配置安装目录
    ./configure --prefix=/usr/local/keepalived
    make && make install
```
-   修改配置文件 -- /usr/local/keepalived/etc/keepalived/keepalived.conf
```shell
    global_defs {
       notification_email {
         acassen@firewall.loc
         failover@firewall.loc
         sysadmin@firewall.loc
       }
       notification_email_from Alexandre.Cassen@firewall.loc
       smtp_server 192.168.200.1
       smtp_connect_timeout 30
       router_id LVS_DEVEL
       vrrp_skip_check_adv_addr
       vrrp_strict
       vrrp_garp_interval 0
       vrrp_gna_interval 0
    }

    vrrp_script chk_haproxy {
        script "/etc/keepalived/haproxy_check.sh"
        interval 2
        weight -20
    }

    vrrp_instance VI_1 {
        state MASTER/BACKUP #主备分别为master和backup
        interface ens33
        virtual_router_id 52
        priority 100
        advert_int 1
        nopreempt
        authentication {
            auth_type PASS
            auth_pass 1111
        }
        track_script {
            chk_haproxy
        }
        virtual_ipaddress {
            192.168.18.167
        }
    }

```

-   将keepalived配置成linux系统服务
```shell
    mkdir -p /etc/keepalived
    # 拷贝配置文件
    cp /usr/local/keepalived/etc/keepalived/keepalived.conf /etc/keepalived/
    # 拷贝keepalived脚本
    cp /usr/local/src/keepalived-2.2.4/keepalived/etc/init.d/keepalived /etc/init.d/
    cp /usr/local/keepalived/etc/sysconfig/keepalived /etc/sysconfig/     
    # 配置开机自启动
    chkconfig keepalived on
```

-   启动服务
```shell
    systemctl start keepalived
```

-   查看状态
```shell
    ps aux | grep keepalived
    [root@node1 init.d]# ps aux | grep keepalived
    root      20667  0.0  0.0  44112   892 ?        Ss   22:55   0:00 /usr/local/keepalived/sbin/keepalived -D
    root      20668  0.0  0.0  44112  1320 ?        S    22:55   0:00 /usr/local/keepalived/sbin/keepalived -D
    root      20983  0.0  0.0 112824   992 pts/0    S+   23:01   0:00 grep --color=auto keepalived
```


### consul 安装

keepalived下载地址：
````http request
    https://www.consul.io/downloads
````

安装流程：
-   解压源码包
```shell
    unzip consul_1.11.4_linux_amd64.zip
```
-   执行二进制文件
```shell
    ./consul agent -dev -ui -client=0.0.0.0
```


### CAP理论
```
    C：Consistency -- 强一致性
    A：Availability -- 可用性
    P：Partition tolerance -- 分区容错性
    
    现阶段系统都是需要保证P, 所以一般分为CP、AP。
```
- CAP理论的核心是：一个分布式系统不可能同时很好的满足一致性，可用性和分区容错性这三个需求。
  因此，根据CAP原理将NoSql数据库分成了满足CA原则、满足CP原则和满足AP原则三大类：
  
-   CA - 单点集群, 满足一致性、可用性的系统, 通常在可拓展性上不太强。
-   CP - 满足一致性, 分区容错性的系统, 通常性能不是特别高。
-   AP - 满足可用性, 分区容错性的系统、通常可能对一致性要求低一些。


### Hystrix

用于处理分布式系统的延迟和容错的开源库，在分布式系统中，许多依赖不可避免的会调用失败，比如超时、异常等，
hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，以提供分布式系统的弹性。

- 服务降级 -- fallback
```http request
  当某个服务单元发生故障后，通过熔断器的故障监控（类似于保险丝），向调用方返回一个符合预期的，
可处理的备选响应（Fallback），而不是长时间等待或者抛出调用方法无法处理的异常，导致线程长时间
、不必要地占用，避免服务雪崩。

  如：服务器忙，请稍后再试，不让客户端等待立刻返回一个友好提示，fallback
```
- 哪些情况下触发降级  -- 一般服务降级都是做在客户端(服务调用端)
```
  1. 程序运行异常
  2. 超时
  3. 服务熔断触发服务降级
  4. 线程池/信号量打满也会导致服务降级
```

- 服务熔断 -- break
```
  类比保险丝，达到最大服务访问后，直接拒绝访问，拉闸限电，然后调用服务降级的方法并返回友好提示。
  服务的降级 -> 进而熔断 -> 恢复调用链路
  
  熔断机制：
  熔断机制是应对雪崩效应的一种微服务链路保护机制, 当扇出链路的某个微服务出错不可用或者响应时间太长时,
  会进行服务的降级, 进而熔断该节点微服务的调用, 快速返回错误的响应信息。
  当检测到该节点微服务调用响应正常后, 恢复调用链路。
  
  1. 微服务调用失败, 会触发降级, 而降级会调用fallback方法.
  2. 无论何如, 降级的流程一定是先执行正常方法再调用fallback方法.
  3. 假如指定时间内降级次数过多, 默认是5s内20次调用失败, 则触发熔断, 熔断注解是@HystrixCommand.
  4. 所谓"熔断后服务不可用", 其实是跳过了正常方法的调用, 直接调用fallback方法.
  
  降级是一种思想, 熔断是对降级的一种实现。
  
  熔断类型：
  1. 熔断打开：请求不在进行当前服务的调用, 内部设置时钟一般为MTTR(平均故障处理时间), 当打开时长达到所设时钟时长则进行半熔断状态;
  2. 熔断半开：部分请求根据规则调用当前服务, 如果请求成功且符合规则则认为当前服务恢复正常, 关闭熔断;
  3. 熔断关闭：熔断关闭, 不会对服务进行熔断;
```
- 服务限流 -- flowlimit
```
  秒杀高并发等操作，对同一时间的大量请求，一秒钟N个，有序处理。
```

###Gateway
```
是在Spring生态系统之上构建的API网关服务, 基于Spring5, Spring Boot 2和Project Reactor等技术。
Gateway旨在提供一种简单而优先的方式, 来对Api进行路由, 以及提供一些强大的过滤器功能, 例如：熔断、限流、重试等。

SpringCloud Gateway使用了Webflux中的Reactor-netty响应式编程组件, 底层使用了Netty通讯框架。
```

####Gateway具有的特性

- 动态路由: 能够匹配任何请求属性;
- 可以对路由指定Predicate(断言) 和 Filter(过滤器);
- 集成hystrix的断路器;
- 集成SpringCloud的服务发现;
- 请求限流;
- 支持路径重写;


####<font color="red">三大核心概念</font>
- Route(路由)
```
路由是构建网关的基本模块, 由ID、目标URI, 一系列的断言和过滤器组成, 如果断言为true则匹配该路由
```
- Predicate(断言)
```
参考的是Java8的java.util.function.Predicate
可以匹配HTTP请求中的所有内容(例如请求头或请求参数), 如果请求与断言相匹配则进行路由
```
- Filter(过滤)
```
指的是Spring框架中GatewayFilter实例, 使用过滤器, 可以在请求被路由前或者之后对请求进行修改
```

Gateway核心逻辑: 路由转发 + 执行过滤器链

####动态路由
默认情况下Gateway会根据注册中心的服务列表,

以注册中心上微服务名为路径创建动态路由进行转发, 从而实现动态路由的功能

#### Predicate 和 Filters
```http request
官方文档：
https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#configuring-route-predicate-factories-and-gateway-filter-factories
```
