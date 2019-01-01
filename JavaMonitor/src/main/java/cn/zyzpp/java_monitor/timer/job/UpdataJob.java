package cn.zyzpp.java_monitor.timer.job;

import cn.zyzpp.java_monitor.core.order.Jps;
import cn.zyzpp.java_monitor.core.order.Jstack;
import cn.zyzpp.java_monitor.core.order.Jstat;
import cn.zyzpp.java_monitor.core.entity.JpsEntity;
import cn.zyzpp.java_monitor.core.entity.JstackEntity;
import cn.zyzpp.java_monitor.core.entity.KVEntity;
import cn.zyzpp.java_monitor.h2.service.ClassService;
import cn.zyzpp.java_monitor.h2.service.GcService;
import cn.zyzpp.java_monitor.h2.service.ThreadService;
import cn.zyzpp.java_monitor.timer.util.TimerUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.List;
import java.util.Map;

/**
 * Create by yster@foxmail.com 2018/11/11 0011 15:25
 */
public class UpdataJob extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private GcService gcService;
    @Autowired
    private ClassService classService;
    @Autowired
    private ThreadService threadService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.debug("Regularly updated data...");
        String date = TimerUtil.time();
        try {
            Map<String, JpsEntity> jps = Jps.jps();
            Object[] s = jps.keySet().toArray();
            for (Object o : s) {
                String name = o.toString();
                JstackEntity jstatk = Jstack.jstack(name);
                List<KVEntity> jstatClass = Jstat.jstatClass(name);
                List<KVEntity> kvEntities = Jstat.jstatGc(name);
                //写入线程信息
                threadService.write(name,date,jstatk);
                //写入类加载信息
                classService.write(name,date,jstatClass);
                //写入堆内存信息
                gcService.write(name,date,kvEntities);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
