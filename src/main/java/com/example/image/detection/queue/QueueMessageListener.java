package com.example.image.detection.queue;

import org.aspectj.weaver.ast.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.image.detection.ImageDetectionDemoApplication;
import com.example.image.detection.Process.ProcessRunner;
import com.example.image.detection.service.FileStorageService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("consumer")
@Service
public class QueueMessageListener {

	private static final Logger log = LoggerFactory.getLogger(QueueMessageListener.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private QueueMessageRepository repository;

    // ./darknet detect cfg/yolov3-tiny.cfg yolov3-tiny.weights /home/vcap/app/uploads/IMG_1696.jpg
    // /home/vcap/app/BOOT-INF/classes/detector
    // Loading weights from yolov3-tiny.weights...Done!
    // /home/vcap/app/uploads/IMG_1696.jpg: Predicted in 3.172089 seconds.
    
    @RabbitListener(queues = ImageDetectionDemoApplication.QUEUE_IN_NAME)
    public void receiveMessage(final QueueMessage message) {
    		String encodedFile = message.getEncodedFile() != null ? message.getEncodedFile() : null;
    		String fileName = message.getText() != null ? message.getText() : null;
    		int l = (encodedFile != null) ? encodedFile.length() : 0;
        log.info("Received message:" + message.getId() + " with file len:" + l);
        try {
        		log.info("Writing file:"+fileName);
        		fileStorageService.writeBase64File(fileName, encodedFile);
        		String fullPath = fileStorageService.getFullPath(fileName);
        		ProcessRunner pr = new ProcessRunner(fullPath);
        		pr.run();
        		log.info("Output:"+pr.getOutput());
        		log.info("Error:"+pr.getError());
        		TimeUnit.SECONDS.sleep(1);
        		
        		String detectedFile = fileStorageService.fileAsBase64FullPath(pr.baseDir+"/"+"predictions.jpg");
        		log.info("Detected file size:"+detectedFile.length());
        		message.setDetectedFile(detectedFile);
            
        		rabbitTemplate.convertAndSend(
            		ImageDetectionDemoApplication.EXCHANGE_NAME, 
            		ImageDetectionDemoApplication.ROUTING_OUT_KEY,
            		message
        		);
        		
            // save it
        		log.info("Saving message to repository...");
            Object o = repository.save(message);
            log.info("Saved object..."+o.toString());
            
        } catch(java.lang.InterruptedException ie) {
        		log.info("Interrupted");
        }
    }

}
