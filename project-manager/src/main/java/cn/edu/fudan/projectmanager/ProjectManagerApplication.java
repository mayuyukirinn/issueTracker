package cn.edu.fudan.projectmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@ServletComponentScan//扫描Filter
@MapperScan("cn.edu.fudan.projectmanager.mapper")
@EnableTransactionManagement//开启事务支持
public class ProjectManagerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ProjectManagerApplication.class, args);
    }
}
