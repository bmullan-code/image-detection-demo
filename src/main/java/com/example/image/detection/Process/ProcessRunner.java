package com.example.image.detection.Process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// ./darknet detect cfg/yolov3-tiny.cfg yolov3-tiny.weights /home/vcap/app/uploads/IMG_1696.jpg
// /home/vcap/app/BOOT-INF/classes/detector
// Loading weights from yolov3-tiny.weights...Done!
// /home/vcap/app/uploads/IMG_1696.jpg: Predicted in 3.172089 seconds.


public class ProcessRunner {

	private static final Logger log = LoggerFactory.getLogger(ProcessRunner.class);

	public final String baseDir = "/home/vcap/app/BOOT-INF/classes/detector"; 
	private final String[] args = {
		"/home/vcap/app/BOOT-INF/classes/detector/darknet",
		"detect",
		"/home/vcap/app/BOOT-INF/classes/detector/cfg/yolov3-tiny.cfg",
		"/home/vcap/app/BOOT-INF/classes/detector/yolov3-tiny.weights"
	};
	private String output;
	private String error;
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	private List fullArgs = null;

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public ProcessRunner( String filePath) {
		List args1 = new ArrayList<>();
		for( String s : args) {
			args1.add(s);
		}
		args1.add(filePath);
		this.fullArgs = args1;
	}
	
	public void run() {
		ProcessBuilder pb = new ProcessBuilder(this.fullArgs);
		pb.directory(new File(this.baseDir));
		try {
			Process process = pb.start();
			int x = process.waitFor();
			log.info("Process completed with status:"+x);
			this.setOutput(output(process.getInputStream()));
			this.setError(output(process.getErrorStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 private String output(InputStream inputStream) throws IOException {
		 StringBuilder sb = new StringBuilder();
		 BufferedReader br = null;
		 try {
			 br = new BufferedReader(new InputStreamReader(inputStream));
			 String line = null;
			 while ((line = br.readLine()) != null) {
				 sb.append(line + System.getProperty("line.separator"));
			 }
		 }
		 finally {
			 br.close();
		 }
		 return sb.toString();
	 }
}
