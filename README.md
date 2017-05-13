# elasticsearch-demo
> # 功能列表
> 一、用spring boot搭建的后端框架
>
> 二、页面渲染在后端，用的是thymeleaf模版引擎
>
> 三、前端使用angularJs实现数据视图的双向绑定，并且用的angular自带的resource模块请求restful接口
>
> 四、提供了提交数据的页面和相关接口
>
> 五、提供了多条件查询的页面和相关接口
>
> 六、提供了查询结果匹配高亮显示，分页查询等功能

**项目目录一览**

![项目目录一览](http://upload-images.jianshu.io/upload_images/3690542-5933232ecabc51da.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


# 一、用spring boot搭建的后端框架
idea创建spring boot项目很方便的，这里我就不多说了。

# 二、页面渲染在后端，用的是thymeleaf模版引擎
thymeleaf模版的引入，pox.xml中添加如下依赖
```
<!-- thymeleaf -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```
页面类型为.html文件，html文档的<html>标签内添加``` xmlns:th="http://www.thymeleaf.org"```，如下所示：
```
<html lang="zh-CN" xmlns:th="http://www.thymeleaf.org">
```

# 三、前端使用angularJs实现数据视图的双向绑定，并且用的angular自带的resource模块请求restful接口
因为页面渲染在服务端，所以angular请求的模板视图需要做相应的处理，后端controller内添加如下代码：
```
 @GetMapping(value = {"/fragments/add-data", "/fragments/search-data"})
public String indexIncludePage(HttpServletRequest request) {

    String path = request.getServletPath();

    return path.substring(0, path.length() - 5);
}
```
以上表示前端请求的这两个```/fragments/add-data,/fragments/search-data```视图经服务端渲染后返回给前端调用者。
以下为angular的resource模块的restful风格接口调用：
```
/** 自定义 Person 服务 */
.factory('Person', function ($resource) {
    var ctx = window.qbian._ctx;
    return $resource(ctx + '/person', {}, {
        submit: {
            method: 'POST' ,
            url: ctx + '/person' ,
            params : {
                name: '@name' ,
                sex : '@sex' ,
                interest : '@interest'
            }
        } ,
        query: {
            method: 'GET' ,
            url: ctx + '/person/search' ,
            params : {
                pageNo: '@pageNo',
                pageSize: '@pageSize' ,
                sex: '@sex' ,
                name: '@name' ,
                interest: '@interest'
            }
        }
    });

})
```

# 四、提供了提交数据的页面和相关接口
angular的controller内添加如下代码：
```
$scope.initPageSize = 9; // 初始化小于10，下一页按钮不可用
$scope.personV = {
    name : '',
    sex : '',
    interest : '',
    pageNo : 1
};
```
以上是查询的初始化参数和分页要用到的一个参数。页面初始化时无数据，所以下一页按钮不可用。不添加任何查询条件时默认参数全为空。
以下为html页面内容：
```
<!--查询参数-->
<input ng-model="personV.name" type="text" class="form-control" placeholder="Name" />
 <select ng-model="personV.sex" class="form-control" id="sex">
    <option>男</option>
    <option>女</option>
</select>
<input ng-model="personV.interest" type="text" class="form-control" placeholder="Interest" />
<!--分页代码如下-->
<ul class="pager">
    <li><button ng-disabled="personV.pageNo &lt; 2" ng-click="prevPage()" type="button" class="btn btn-sm btn-default">上一页</button></li>
    <span>当前第（<span style="color: #00be67;font-weight: 600;" ng-bind="personV.pageNo"></span>）页</span>
    <li><button ng-disabled="initPageSize &lt; 10" ng-click="nextPage()" type="button" class="btn btn-sm btn-default">下一页</button></li>
</ul>
```
# 五、提供了多条件查询的页面和相关接口
四的内容就是多条件查询相关。

# 六、提供了查询结果匹配高亮显示，分页查询等功能
查询结果匹配高亮部分代码如下：
```
/**
 * 检索匹配关键字高亮
 * @param queryBuilder 查询引擎
 * @param type 文档类型
 * @param highlightFieldList 高亮清单
 * @param pageNo 页码
 * @param pageSize 当页显示数据量
 * @return 查询结果
 */
public JSONArray searchHighlight(QueryBuilder queryBuilder, String type, List<String> highlightFieldList
        , int pageNo, int pageSize) {
    StopWatch clock = new StopWatch();
    clock.start();

    // 设置高亮显示
    HighlightBuilder highlightBuilder = new HighlightBuilder().requireFieldMatch(true);
    if(highlightFieldList != null) {
        for(String field : highlightFieldList) {
            highlightBuilder.field(field, 10240);
        }
    }
    /** 以下两行就是高亮相关代码 */
    highlightBuilder.preTags("<span style=\"color:red\">");
    highlightBuilder.postTags("</span>");

    SearchResponse searchResponse = client.prepareSearch(INDEX_TYPE).setTypes(type)
            .setQuery(queryBuilder).highlighter(highlightBuilder)
            .addSort("date", SortOrder.DESC)
            .setFrom(pageNo * pageSize).setSize(pageSize)
            .setExplain(true).execute().actionGet();
    SearchHit[] hits = searchResponse.getHits().getHits();

    clock.stop();
    LOG.info("searchHighlight: {} ms", clock.getTotalTimeMillis());

    // 封装查询结果
    JSONArray result = new JSONArray();
    if(hits != null && hits.length > 0) {
        for(SearchHit hit : hits) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            JSONObject data = new JSONObject();
            for(Entry<String, Object> entry : hit.getSource().entrySet()) {
                // 保存高亮字段
                if(highlightFields != null && highlightFields.containsKey(entry.getKey())) {
                    HighlightField titleField = highlightFields.get(entry.getKey());
                    Text[] fragments = titleField.fragments();
                    StringBuilder sb = new StringBuilder();
                    for(Text text : fragments) {
                        sb.append(text);
                    }
                    data.put(entry.getKey(), sb.toString());
                } else {
                    data.put(entry.getKey(), entry.getValue());
                }

            }
            result.add(data);
        }
    }

    return result;
}
```
 好了，说了这么多还是演示以下吧

# 七、功能演示
因为采用的是elasticsearch集群，所以我就开启了两台elasticsearch服务器，集群的详细搭建在我上一篇文章内有详细介绍。我现在启动的两台es服务器的ip分别是：192.168.1.113对应节点名称是node－1，192。168.1.129对应节点名称是node－2，master节点是node－1，如下图：

![集群状态](http://upload-images.jianshu.io/upload_images/3690542-abcd6192d0637d07.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
我们项目内的es相关配置在application-es.yml配置文件内，详细配置如下：
```
#=========================================
#================= elasticSearch config
#=========================================
es:
  ips: 192.168.1.113,192.168.1.129
  port: 9300
  cluster: test
```
然后启动我们的项目：

![启动项目](http://upload-images.jianshu.io/upload_images/3690542-fdb2900fafac1e29.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
以上表示启动成功，然后我们再访问首页：localhost:8180/es


![index页面](http://upload-images.jianshu.io/upload_images/3690542-5dab8544a0289ca6.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
我们可以在这里提交一些数据，然后去搜索数据页面查询，这里我已经提交了一些数据了，就直接去查询了。
1、默认无条件查询：

![无条件查询](http://upload-images.jianshu.io/upload_images/3690542-b005a5ee276455d7.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
查询结果如下：

![无条件查询结果](http://upload-images.jianshu.io/upload_images/3690542-80643c49df78da23.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
可以看到我们的分页的下一页是可以点击的，因为返回的数据量刚好是我们默认请求的数据量10条，所以我们认为它还是存在下一页的。
下一页按钮点击后：

![无条件查询第二页数据](http://upload-images.jianshu.io/upload_images/3690542-8124c74b29ef8ce8.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
因为该页返回的数据量小于我们请求的10条，所以我们认为这已经是最后一页了，下一页按钮不可用。
2、有条件查询
查询爱“打篮球”的“男生”：

![爱打篮球的男生查询结果](http://upload-images.jianshu.io/upload_images/3690542-ae643cb5fc8b77b7.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
查询“男生”，爱好一个“球”：

![“男生”，爱好“球”](http://upload-images.jianshu.io/upload_images/3690542-34e0e12675c43627.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


以上就是elasticsearch的demo演示及其讲解，下载下来就可用测试使用（前提是你也安装了elasticsearch,安装，不是集群的也可用）
