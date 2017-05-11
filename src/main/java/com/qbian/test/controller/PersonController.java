package com.qbian.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qbian.common.dto.ResponseDto;
import com.qbian.common.es.PersonSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Qbian on 2017/5/11.
 */
@RestController
public class PersonController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonSearch personSearch;

    /**
     * 提交数据
     * @param body 请求体
     * @return
     */
    @PostMapping("/person")
    public ResponseDto submit(@RequestBody String body) {
        LOG.info("请求数据：" + body);

        JSONObject requestJson = JSON.parseObject(body);

        String name = requestJson.getString("name");
        String sex = requestJson.getString("sex");
        String interest = requestJson.getString("interest");

        personSearch.submit(name, sex, interest);

        return new ResponseDto();
    }

    /**
     * 检索数据
     * @param request HttpServletRequest
     * @return 查询结果
     */
    @GetMapping("/person/search")
    public ResponseDto<JSONArray> search(HttpServletRequest request) {
        Integer pageNo = Integer.parseInt(request.getParameter("pageNo"));
        Integer pageSize = Integer.parseInt(request.getParameter("pageSize"));
        String sex = request.getParameter("sex");
        String name = request.getParameter("name");
        String interest = request.getParameter("interest");

        pageNo = pageNo <= 0 ? 0 : pageNo - 1;

        LOG.info("[收到查询]pageNo:" + pageNo + ",pageSize:" + pageSize + ",sex:" + sex + ",name:" + name + ",interest:" + interest);

        JSONArray result = personSearch.search(name, sex, interest, pageNo, pageSize);

        return new ResponseDto(result);
    }



}
