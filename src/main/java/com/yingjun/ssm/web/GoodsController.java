package com.yingjun.ssm.web;

import com.yingjun.ssm.dto.BaseResult;
import com.yingjun.ssm.entity.Goods;
import com.yingjun.ssm.enums.ResultEnum;
import com.yingjun.ssm.exception.BizException;
import com.yingjun.ssm.service.GoodsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GoodsService goodsService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model, Integer offset, Integer limit) {
        LOG.info("invoke----------/goods/list");
        offset = offset == null ? 0 : offset;//默认便宜0
        limit = limit == null ? 50 : limit;//默认展示50条
        List<Goods> list = goodsService.getGoodsList(offset, limit);
        model.addAttribute("goodslist", list);
        return "goodslist";
    }

    /*requestmapping里的{goodsId}是个占位符，可以匹配路径/buy,并把*的值赋值给goodsId，@valid中Goods类型自动把goodsId获取到。
    @valid 自动校验的是jsp传来的参数，占位符也是一个参数。如果想使用占位符这个参数就需要@PathVariable("goodsId") Long goodsId这个语句*/
    @RequestMapping(value = "/{goodsId}/buy", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public BaseResult<Object> buy( @RequestParam HashMap<String, Object> map, @CookieValue(value = "userPhone", required = false) Long userPhone,
        /*@PathVariable("goodsId") Long goodsId*/ @Valid Goods goods, BindingResult result) {
    	System.out.println("goods : " + goods);
    	for(String str : map.keySet()){
    		System.out.println("str : " + str);
    	}
        LOG.info("invoke----------/" + goods.getGoodsId() + "/buy userPhone:" + userPhone);
        if (userPhone == null) {
            return new BaseResult<Object>(false, ResultEnum.INVALID_USER.getMsg());
        }
        //Valid 参数验证(这里注释掉，采用AOP的方式验证,见BindingResultAop.java)
        //if (result.hasErrors()) {
        //    String errorInfo = "[" + result.getFieldError().getField() + "]" + result.getFieldError().getDefaultMessage();
        //    return new BaseResult<Object>(false, errorInfo);
        //}
        try {
            goodsService.buyGoods(userPhone, goods.getGoodsId(), false);
        } catch (BizException e) {
            return new BaseResult<Object>(false, e.getMessage());
        } catch (Exception e) {
            return new BaseResult<Object>(false, ResultEnum.INNER_ERROR.getMsg());
        }
        return new BaseResult<Object>(true, null);
    }
}
