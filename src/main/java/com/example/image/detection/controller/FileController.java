package com.example.image.detection.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.image.detection.FileDemoApplication;
import com.example.image.detection.payload.UploadFileResponse;
import com.example.image.detection.queue.QueueMessage;
import com.example.image.detection.service.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	private final AtomicLong counter = new AtomicLong();
	private static final Logger log = LoggerFactory.getLogger(FileController.class);
	
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RabbitAdmin rabbitAdmin;
    
    @GetMapping("/queueInfo") 
    public String getQueueInfo() {
    		Gson gsonObj = new Gson();
    		Properties props = rabbitAdmin.getQueueProperties(FileDemoApplication.QUEUE_OUT_NAME);
    		String strJson =  gsonObj.toJson(props);    		
    		return strJson;
    }

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        
        // convert the file to base64 for inclusion in a rabbit mq message.
        String encodedFile = fileStorageService.fileAsBase64(fileName);
        log.info("Encoded file length ..." + encodedFile.length());
        final QueueMessage message = new QueueMessage(
        		counter.incrementAndGet(),
        		fileName, 
        		new Random().nextInt(50), 
        		false,
        		encodedFile
    		);
        log.info("Sending message..." + message.getId());
        rabbitTemplate.convertAndSend(FileDemoApplication.EXCHANGE_NAME, FileDemoApplication.ROUTING_IN_KEY, message);
        
        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
