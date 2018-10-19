package com.example.image.detection.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.image.detection.exception.FileStorageException;
import com.example.image.detection.exception.MyFileNotFoundException;
import com.example.image.detection.property.FileStorageProperties;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public String fileAsBase64FullPath( String fullPath ) {
		try {
	    		byte[] fileContent = FileUtils.readFileToByteArray(new File(fullPath));
	    		String encodedString = Base64.getEncoder().encodeToString(fileContent);
	    		return encodedString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String fileAsBase64( String fileName ) {
    		try {
    			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
    			
        		byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath.toAbsolutePath().toString()));
        		String encodedString = Base64.getEncoder().encodeToString(fileContent);
        		return encodedString;
   		} catch (Exception e) {
			// TODO: handle exception
   			e.printStackTrace();
		}
    		return null;
    }
    
    public void writeBase64File(String fileName, String encodedFile) {
    		try {
    			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
    			byte[] data = Base64.getDecoder().decode(encodedFile);
    			FileUtils.writeByteArrayToFile(filePath.toFile(), data);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}   	
    }

    public String getFullPath(String fileName) {
    		Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
    		return filePath.toString();
    }
    
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
