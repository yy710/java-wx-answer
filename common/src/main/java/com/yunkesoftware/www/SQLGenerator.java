package com.yunkesoftware.www;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.Collections;
import java.util.Map;

/**
 * MybatisPlus代码生成器
 */
public class SQLGenerator {
    private static final String packageName = "com.yunkesoftware.www.web"; // 文件路径
    private static final String property = "D:\\code\\city-walk-java\\web\\";
    private static final Map<String, Object> urli = Collections.singletonMap("urli", "wx");
    // 表名多个用逗号分隔
    public static String[] table = {"category"};
    // 作者
    private static final String authorName = "yk";
    // table前缀
    private static final String prefix = "yk_";


    private static final String path = "/src/main/java";
    private static final String url = "jdbc:mysql://192.168.1.129:3306/city-walk?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
    private static final String username = "root";
    private static final String password = "123456";

    public static void main(String[] args) {
        //String property = System.getProperty("user.dir");
        FastAutoGenerator.create(url, username, password)
                //全局配置
                .globalConfig(builder -> {
                    builder.author(authorName) // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            //.dateType(DateType.ONLY_DATE) //设置事件策略
                            .commentDate("yyyy-MM-dd") // 注释日期
                            .outputDir(property + path); // 指定输出目录
                })
                //包配置
                .packageConfig(builder -> {
                    builder.parent(packageName) // 设置父包名
                            .moduleName("") // 设置父包模块名
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .mapper("mapper")
                            .xml("xml")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, property + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                }).strategyConfig(builder -> {
                    builder.addInclude(table) // 设置需要生成的表名
                            .addTablePrefix(prefix).serviceBuilder()//开始设置服务层
                            .formatServiceFileName("%sService")//这里整体为 %s（代表表名）+ 后面的Service
                            .formatServiceImplFileName("%sServiceImpl")
                            .entityBuilder()
                            .naming(NamingStrategy.underline_to_camel)//生成符合驼峰命名
                            .enableChainModel()//支持链式书写
                            .enableLombok()//支持lombok
                            .logicDeletePropertyName("deleted")//指出逻辑删除
                            .enableTableFieldAnnotation()//提供字段注解
                            .controllerBuilder()
                            .formatFileName("%sController")
                            .enableRestStyle()//生成@restcontroller 风格
                            .mapperBuilder()
                            .enableBaseResultMap()
                            .enableFileOverride()
                            .enableBaseColumnList()
                            .superClass(BaseMapper.class)//继承
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper");
                }).injectionConfig(i -> {
                    i.customMap(urli);
                })
                .execute();

    }
}

