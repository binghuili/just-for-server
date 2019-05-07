package com.paopao.doSomething;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;


/**
 * @author libinghui
 * @date 2018/11/28 13:40
 */
public class DingDingNotifier extends AbstractEventNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(DingDingNotifier.class);

    public DingDingNotifier(InstanceRepository repository) {
        super(repository);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            if (event instanceof InstanceStatusChangedEvent) {
                LOGGER.info("Instance {} ({}) is {}", instance.getRegistration().getName(), event.getInstance(),
                        ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());
                String s = "https://oapi.dingtalk.com/robot/send?access_token=c6bf4bf16cb926d53ab7b33525c4e7bae922a974b77763414d9bb8a8499a80f2";
                HttpClient httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(s);
                httppost.addHeader("Content-Type", "application/json; charset=utf-8");

                String textMsg = "Instance " + instance.getRegistration().getName() + "(" + event.getInstance() + ")" + " is " + ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
                StringEntity se = new StringEntity(textMsg, "utf-8");
                httppost.setEntity(se);

                try {
                    HttpResponse response = httpclient.execute(httppost);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String result = EntityUtils.toString(response.getEntity(), "utf-8");
                        LOGGER.info("result={}", result);
                    }
                } catch (IOException e) {
                    LOGGER.info("钉钉异常!");
                }

            } else {
                LOGGER.info("Instance {} ({}) {}", instance.getRegistration().getName(), event.getInstance(),
                        event.getType());
            }
        });
    }

}
