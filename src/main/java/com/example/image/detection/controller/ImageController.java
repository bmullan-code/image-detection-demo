package com.example.image.detection.controller;

import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.image.detection.queue.QueueMessage;
import com.example.image.detection.queue.QueueMessageListener;
import com.example.image.detection.queue.QueueMessageRepository;

@RestController
public class ImageController {
	
	private static final Logger log = LoggerFactory.getLogger(ImageController.class);
	
    @Autowired private QueueMessageRepository repository;

	@GetMapping("/detectImage/{id}") 
//	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
    public ResponseEntity<byte[]> getDetectImage(@PathVariable long id) {
    		log.info("detectImage id:"+id);
    		ResponseEntity<byte[]> responseEntity = null;
    		HttpHeaders headers = new HttpHeaders();
    		byte[] data = null;
    		// get the row from repository
    		Optional<QueueMessage> msg = repository.findById(id);
    		
    		if (msg.isPresent()) {
    			log.info("image present");
    			QueueMessage image = msg.get();
    			log.info("image id:"+image.getGuid());
    			System.out.println("Detected File len:"+image.getDetectedFile().length());
    			final byte[] innerData = data;
    			data = Base64.getDecoder().decode(image.getDetectedFile());
    			headers.setCacheControl(CacheControl.noCache().getHeaderValue());
    			headers.setContentType(MediaType.IMAGE_JPEG);
    			responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);   			
    		}
    		return responseEntity;
    }
		
}
