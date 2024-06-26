
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 前台
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/qiantai")
public class QiantaiController {
    private static final Logger logger = LoggerFactory.getLogger(QiantaiController.class);

    private static final String TABLE_NAME = "qiantai";

    @Autowired
    private QiantaiService qiantaiService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private ChangdiService changdiService;//场地信息
    @Autowired
    private ChangdiCollectionService changdiCollectionService;//场地收藏
    @Autowired
    private ChangdiLiuyanService changdiLiuyanService;//场地留言
    @Autowired
    private ChangdiYuyueService changdiYuyueService;//场地预约申请
    @Autowired
    private DianzhangService dianzhangService;//店长
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//交流论坛
    @Autowired
    private JianshenkechengService jianshenkechengService;//健身课程
    @Autowired
    private JianshenkechengCollectionService jianshenkechengCollectionService;//课程收藏
    @Autowired
    private JianshenkechengLiuyanService jianshenkechengLiuyanService;//课程留言
    @Autowired
    private JianshenkechengOrderService jianshenkechengOrderService;//课程订单
    @Autowired
    private JianshenqichaiService jianshenqichaiService;//健身器材
    @Autowired
    private JianshenqichaiChuruInoutService jianshenqichaiChuruInoutService;//出入库
    @Autowired
    private JianshenqichaiChuruInoutListService jianshenqichaiChuruInoutListService;//出入库详情
    @Autowired
    private JiaolianService jiaolianService;//教练
    @Autowired
    private NewsService newsService;//公告通知
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private ZhuchangService zhuchangService;//驻场人员
    @Autowired
    private ZifeiService zifeiService;//资费
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("店长".equals(role))
            params.put("dianzhangId",request.getSession().getAttribute("userId"));
        else if("教练".equals(role))
            params.put("jiaolianId",request.getSession().getAttribute("userId"));
        else if("前台".equals(role))
            params.put("qiantaiId",request.getSession().getAttribute("userId"));
        else if("驻场人员".equals(role))
            params.put("zhuchangId",request.getSession().getAttribute("userId"));
        params.put("dataDeleteStart",1);params.put("dataDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = qiantaiService.queryPage(params);

        //字典表数据转换
        List<QiantaiView> list =(List<QiantaiView>)page.getList();
        for(QiantaiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        QiantaiEntity qiantai = qiantaiService.selectById(id);
        if(qiantai !=null){
            //entity转view
            QiantaiView view = new QiantaiView();
            BeanUtils.copyProperties( qiantai , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody QiantaiEntity qiantai, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,qiantai:{}",this.getClass().getName(),qiantai.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<QiantaiEntity> queryWrapper = new EntityWrapper<QiantaiEntity>()
            .eq("username", qiantai.getUsername())
            .or()
            .eq("qiantai_phone", qiantai.getQiantaiPhone())
            .eq("data_delete", 1)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        QiantaiEntity qiantaiEntity = qiantaiService.selectOne(queryWrapper);
        if(qiantaiEntity==null){
            qiantai.setDataDelete(1);
            qiantai.setInsertTime(new Date());
            qiantai.setCreateTime(new Date());
            qiantai.setPassword("123456");
            qiantaiService.insert(qiantai);
            return R.ok();
        }else {
            return R.error(511,"账户或者前台手机号已经被使用");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody QiantaiEntity qiantai, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,qiantai:{}",this.getClass().getName(),qiantai.toString());
        QiantaiEntity oldQiantaiEntity = qiantaiService.selectById(qiantai.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        if("".equals(qiantai.getQiantaiPhoto()) || "null".equals(qiantai.getQiantaiPhoto())){
                qiantai.setQiantaiPhoto(null);
        }
        if("".equals(qiantai.getQiantaiContent()) || "null".equals(qiantai.getQiantaiContent())){
                qiantai.setQiantaiContent(null);
        }

            qiantaiService.updateById(qiantai);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<QiantaiEntity> oldQiantaiList =qiantaiService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<QiantaiEntity> list = new ArrayList<>();
        for(Integer id:ids){
            QiantaiEntity qiantaiEntity = new QiantaiEntity();
            qiantaiEntity.setId(id);
            qiantaiEntity.setDataDelete(2);
            list.add(qiantaiEntity);
        }
        if(list != null && list.size() >0){
            qiantaiService.updateBatchById(list);
        }

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<QiantaiEntity> qiantaiList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            QiantaiEntity qiantaiEntity = new QiantaiEntity();
//                            qiantaiEntity.setUsername(data.get(0));                    //账户 要改的
//                            qiantaiEntity.setPassword("123456");//密码
//                            qiantaiEntity.setQiantaiName(data.get(0));                    //前台名称 要改的
//                            qiantaiEntity.setQiantaiPhone(data.get(0));                    //前台手机号 要改的
//                            qiantaiEntity.setQiantaiPhoto("");//详情和图片
//                            qiantaiEntity.setSexTypes(Integer.valueOf(data.get(0)));   //性别 要改的
//                            qiantaiEntity.setQiantaiEmail(data.get(0));                    //前台邮箱 要改的
//                            qiantaiEntity.setQiantaiNewMoney(data.get(0));                    //工薪 要改的
//                            qiantaiEntity.setQiantaiContent("");//详情和图片
//                            qiantaiEntity.setDataDelete(1);//逻辑删除字段
//                            qiantaiEntity.setInsertTime(date);//时间
//                            qiantaiEntity.setCreateTime(date);//时间
                            qiantaiList.add(qiantaiEntity);


                            //把要查询是否重复的字段放入map中
                                //账户
                                if(seachFields.containsKey("username")){
                                    List<String> username = seachFields.get("username");
                                    username.add(data.get(0));//要改的
                                }else{
                                    List<String> username = new ArrayList<>();
                                    username.add(data.get(0));//要改的
                                    seachFields.put("username",username);
                                }
                                //前台手机号
                                if(seachFields.containsKey("qiantaiPhone")){
                                    List<String> qiantaiPhone = seachFields.get("qiantaiPhone");
                                    qiantaiPhone.add(data.get(0));//要改的
                                }else{
                                    List<String> qiantaiPhone = new ArrayList<>();
                                    qiantaiPhone.add(data.get(0));//要改的
                                    seachFields.put("qiantaiPhone",qiantaiPhone);
                                }
                        }

                        //查询是否重复
                         //账户
                        List<QiantaiEntity> qiantaiEntities_username = qiantaiService.selectList(new EntityWrapper<QiantaiEntity>().in("username", seachFields.get("username")).eq("data_delete", 1));
                        if(qiantaiEntities_username.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(QiantaiEntity s:qiantaiEntities_username){
                                repeatFields.add(s.getUsername());
                            }
                            return R.error(511,"数据库的该表中的 [账户] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                         //前台手机号
                        List<QiantaiEntity> qiantaiEntities_qiantaiPhone = qiantaiService.selectList(new EntityWrapper<QiantaiEntity>().in("qiantai_phone", seachFields.get("qiantaiPhone")).eq("data_delete", 1));
                        if(qiantaiEntities_qiantaiPhone.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(QiantaiEntity s:qiantaiEntities_qiantaiPhone){
                                repeatFields.add(s.getQiantaiPhone());
                            }
                            return R.error(511,"数据库的该表中的 [前台手机号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        qiantaiService.insertBatch(qiantaiList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }

    /**
    * 登录
    */
    @IgnoreAuth
    @RequestMapping(value = "/login")
    public R login(String username, String password, String captcha, HttpServletRequest request) {
        QiantaiEntity qiantai = qiantaiService.selectOne(new EntityWrapper<QiantaiEntity>().eq("username", username));
        if(qiantai==null || !qiantai.getPassword().equals(password))
            return R.error("账号或密码不正确");
        else if(qiantai.getDataDelete() != 1)
            return R.error("账户已被删除");
        String token = tokenService.generateToken(qiantai.getId(),username, "qiantai", "前台");
        R r = R.ok();
        r.put("token", token);
        r.put("role","前台");
        r.put("username",qiantai.getQiantaiName());
        r.put("tableName","qiantai");
        r.put("userId",qiantai.getId());
        return r;
    }

    /**
    * 注册
    */
    @IgnoreAuth
    @PostMapping(value = "/register")
    public R register(@RequestBody QiantaiEntity qiantai, HttpServletRequest request) {
//    	ValidatorUtils.validateEntity(user);
        Wrapper<QiantaiEntity> queryWrapper = new EntityWrapper<QiantaiEntity>()
            .eq("username", qiantai.getUsername())
            .or()
            .eq("qiantai_phone", qiantai.getQiantaiPhone())
            .andNew()
            .eq("data_delete", 1)
            ;
        QiantaiEntity qiantaiEntity = qiantaiService.selectOne(queryWrapper);
        if(qiantaiEntity != null)
            return R.error("账户或者前台手机号已经被使用");
        qiantai.setQiantaiNewMoney(0.0);
        qiantai.setDataDelete(1);
        qiantai.setInsertTime(new Date());
        qiantai.setCreateTime(new Date());
        qiantaiService.insert(qiantai);

        return R.ok();
    }

    /**
     * 重置密码
     */
    @GetMapping(value = "/resetPassword")
    public R resetPassword(Integer  id, HttpServletRequest request) {
        QiantaiEntity qiantai = qiantaiService.selectById(id);
        qiantai.setPassword("123456");
        qiantaiService.updateById(qiantai);
        return R.ok();
    }

	/**
	 * 修改密码
	 */
	@GetMapping(value = "/updatePassword")
	public R updatePassword(String  oldPassword, String  newPassword, HttpServletRequest request) {
        QiantaiEntity qiantai = qiantaiService.selectById((Integer)request.getSession().getAttribute("userId"));
		if(newPassword == null){
			return R.error("新密码不能为空") ;
		}
		if(!oldPassword.equals(qiantai.getPassword())){
			return R.error("原密码输入错误");
		}
		if(newPassword.equals(qiantai.getPassword())){
			return R.error("新密码不能和原密码一致") ;
		}
        qiantai.setPassword(newPassword);
		qiantaiService.updateById(qiantai);
		return R.ok();
	}



    /**
     * 忘记密码
     */
    @IgnoreAuth
    @RequestMapping(value = "/resetPass")
    public R resetPass(String username, HttpServletRequest request) {
        QiantaiEntity qiantai = qiantaiService.selectOne(new EntityWrapper<QiantaiEntity>().eq("username", username));
        if(qiantai!=null){
            qiantai.setPassword("123456");
            qiantaiService.updateById(qiantai);
            return R.ok();
        }else{
           return R.error("账号不存在");
        }
    }


    /**
    * 获取用户的session用户信息
    */
    @RequestMapping("/session")
    public R getCurrQiantai(HttpServletRequest request){
        Integer id = (Integer)request.getSession().getAttribute("userId");
        QiantaiEntity qiantai = qiantaiService.selectById(id);
        if(qiantai !=null){
            //entity转view
            QiantaiView view = new QiantaiView();
            BeanUtils.copyProperties( qiantai , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }
    }


    /**
    * 退出
    */
    @GetMapping(value = "logout")
    public R logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return R.ok("退出成功");
    }



    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = qiantaiService.queryPage(params);

        //字典表数据转换
        List<QiantaiView> list =(List<QiantaiView>)page.getList();
        for(QiantaiView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Integer id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        QiantaiEntity qiantai = qiantaiService.selectById(id);
            if(qiantai !=null){


                //entity转view
                QiantaiView view = new QiantaiView();
                BeanUtils.copyProperties( qiantai , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody QiantaiEntity qiantai, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,qiantai:{}",this.getClass().getName(),qiantai.toString());
        Wrapper<QiantaiEntity> queryWrapper = new EntityWrapper<QiantaiEntity>()
            .eq("username", qiantai.getUsername())
            .or()
            .eq("qiantai_phone", qiantai.getQiantaiPhone())
            .andNew()
            .eq("data_delete", 1)
//            .notIn("qiantai_types", new Integer[]{102})
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        QiantaiEntity qiantaiEntity = qiantaiService.selectOne(queryWrapper);
        if(qiantaiEntity==null){
            qiantai.setDataDelete(1);
            qiantai.setInsertTime(new Date());
            qiantai.setCreateTime(new Date());
            qiantai.setPassword("123456");
        qiantaiService.insert(qiantai);

            return R.ok();
        }else {
            return R.error(511,"账户或者前台手机号已经被使用");
        }
    }

}

