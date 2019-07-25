package com.example.es.client.demo.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.example.es.client.demo.entity.EsObject;
import com.example.es.client.demo.entity.User;
import com.example.es.client.demo.result.ResultData;
import com.example.es.client.demo.service.UserService;
import com.example.es.client.demo.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UserServiceImpl:实现类
 *
 * @author zhangxiaoxiang
 * @date: 2019/07/24
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    /**
     * Java高级别REST客户端
     */
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ResultData resultData;
    @Autowired
    private MapUtil esResponseUtil;

    /**
     * 新增一个User
     * map形式
     *
     * @param user
     * @return
     */
    @Override
    public User addUser(User user) {
        try {
            log.warn("方式1--------------------------------------------------------------------------------");
            //Json字符串作为数据源
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("userId","001");
            jsonMap.put("name", user.getName());
            jsonMap.put("age", user.getAge());
            jsonMap.put("birthday", new Date());
            //注意source要的是map
            IndexRequest indexRequest = new IndexRequest("user").id("1").source(jsonMap);
            //同步执行
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            client.close();
            //还可以进一步处理(后面的if 判断哪些代码,这里简洁形式)
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 新增一个User
     * 直接传json字符串
     *
     * @param user
     * @return
     */
    @Override
    public User addUser2(User user) {

        try {
            IndexRequest indexRequest = new IndexRequest("user");
            //直接使用用户主键作为index的哈哈
            indexRequest.id(user.getUserId());
            Object o = JSON.toJSON(user);
            System.out.println(o.toString());
            //或者下面的形式
            // String jsonString = "{" +
            //         "\"user\":\"kimchy\"," +
            //         "\"postDate\":\"2013-01-30\"," +
            //         "\"message\":\"trying out Elasticsearch\"" +
            //         "}";
            indexRequest.source(o.toString(), XContentType.JSON);
            //同步执行
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            client.close();
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
    }


    /**
     * 新增一个User
     * 以映射形式提供的文档源，该映射将自动转换为JSON格式
     *
     * @param user
     * @return
     */
    @Override
    public User addUser3(User user) {
        log.warn("方式2--------------------------------------------------------------------------------");
        //以映射形式提供的文档源，该映射将自动转换为JSON格式
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.field("name", user.getName());
                builder.field("age", user.getAge());
                builder.field("birth_day", user.getBirthday());
            }
            builder.endObject();
            IndexRequest indexRequest2 = new IndexRequest("user").id("3").source(builder);
            //同步执行
            IndexResponse indexResponse = client.index(indexRequest2, RequestOptions.DEFAULT);
            client.close();
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("处理(如果需要)第一次创建文档的情况");

            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("处理(如果需要)将文档重写为已经存在的情况");
            }
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                System.out.println("处理成功碎片的数量少于总碎片的情况");
            }
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure :
                        shardInfo.getFailures()) {
                    String reason = failure.reason();
                    System.out.println("处理潜在的故障");
                }
            }
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 新增一个User
     * 文档源作为XContentBuilder对象提供，Elasticsearch内置助手生成JSON内容
     *
     * @param user
     * @return
     */
    @Override
    public User addUser4(User user) {
        log.warn("方式3--------------------------------------------------------------------------------");
        try {
            //文档源作为XContentBuilder对象提供，Elasticsearch内置助手生成JSON内容
            IndexRequest indexRequest = new IndexRequest("user")
                    .id("4")
                    .source("name", user.getName(),
                            "birth_day", user.getBirthday(),
                            "age", user.getAge());
            //同步执行
            //当以下列方式执行索引请求时，客户端在继续执行代码之前等待返回索引响应:
            IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("处理(如果需要)第一次创建文档的情况");

            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("处理(如果需要)将文档重写为已经存在的情况");
            }
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                System.out.println("处理成功碎片的数量少于总碎片的情况");
            }
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure :
                        shardInfo.getFailures()) {
                    String reason = failure.reason();
                    System.out.println("处理潜在的故障");
                }
            }
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取(查询)用户
     *
     * @param user
     * @return
     */
    @Override
    public GetResponse getUser(User user) {
        EsObject<Object> esObject=new EsObject<>();
        try {
            //GetRequest()方法第一个参数是索引的名字,第二个参数是文档的id
            GetRequest getRequest = new GetRequest("user", "1");
            //查询特定字段,包含和不包含的字段 //查询的字段
            String[] includes = new String[]{"name", "birthday", "age"};
            //不查询的字段
            String[] excludes = new String[]{};
            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
            getRequest.fetchSourceContext(fetchSourceContext);
            //为特定的存储字段配置检索(要求字段在映射中单独存储)
            getRequest.storedFields("name");
            GetResponse getResponse = null;
            getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            System.out.println("查询结果:  "+getResponse);
            //给自己封装的复制
            esObject.setSource(getResponse.getSource());
            String index = getResponse.getIndex();
            String id = getResponse.getId();
            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                //将结果封装成String   或者map类型  或者bytes类型
                String sourceAsString = getResponse.getSourceAsString();
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();

                // 获取结果集中某个字段的数据
                String message = (String) getResponse.getSource().get("name");
                System.out.println("获取name结果:  "+message);
            } else {

                // 处理没有找到文档的场景。注意，虽然返回的响应有404状态代码，但是返回的是有效的GetResponse，
                // 而不是抛出异常。这样的响应不包含任何源文档，并且它的isExists方法返回     }
            }
            return getResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }

    /**
     * 删除用户
     *
     * @param user
     * @return
     */
    @Override
    public GetResponse delUser(User user) {

        try {
            //同步删除 索引   以及id
            DeleteRequest request = new DeleteRequest("user", user.getUserId());
            DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);

            //异步删除
            //client.deleteAsync(request, RequestOptions.DEFAULT, listener);


            String index = deleteResponse.getIndex();
            String id = deleteResponse.getId();
            long version = deleteResponse.getVersion();
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
            // 写入应该去的碎片总数 !=  写入成功的碎片总数
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                System.out.println("shardInfo.getTotal() != shardInfo.getSuccessful()");
            }
            //The total number of replication failures 复制失败的总数
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    String reason = failure.reason();
                }
            }
            System.out.println("删除成功!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
