package cn.edu.fudan.cloneservice.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface CloneScanService {

    void cloneMessageListener(ConsumerRecord<String, String> consumerRecord);
}
