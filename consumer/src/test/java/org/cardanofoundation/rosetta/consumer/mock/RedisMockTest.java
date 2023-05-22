//package org.cardanofoundation.rosetta.consumer.mock;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Disabled;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//
//
//
//@Disabled
//@Profile("test-integration")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//class RedisMockTest {
//
//  static RedisServer redisServer;
//
//  @Value("${spring.redis.port}")
//  private int port;
//
//  @Value("${spring.redis.host}")
//  private String host;
//
//  @Autowired
//  private RedisTemplate<String, Object> redisTemplate;
//
//  @PostConstruct
//  public void postConstruct() {
//    redisServer = RedisServer.builder()
//        .bind(host)
//        .port(port).build();
//    redisServer.start();
//  }
//
//  @Test
//   void testSendAndReceive() {
//    String key = "key";
//    Object value = new String("Hi");
//    redisTemplate.opsForValue().set(key, value);
//    Assertions.assertNotNull(redisTemplate.opsForValue().get(key));
//  }
//
//  @PreDestroy
//  public void destroy() {
//    redisServer.stop();
//  }
//}